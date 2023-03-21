package ch.kleis.lcaplugin.core.lang.evaluator

import arrow.core.tail
import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.resolver.ProcessResolver
import ch.kleis.lcaplugin.core.lang.resolver.ProductResolver

class Evaluator(
    private val symbolTable: SymbolTable,
) {

    fun eval(expression: TemplateExpression): SystemValue {
        val compiler = Compiler(symbolTable)
        val systemObject = compiler.compile(expression)
        val linker = Linker(systemObject)
        return linker.link()
    }

    internal class Compiler(
        private val symbolTable: SymbolTable,
    ) {
        private val reduceAndComplete = ReduceAndComplete(symbolTable)
        private val productResolver = ProductResolver(symbolTable)
        private val processResolver = ProcessResolver(symbolTable)
        private val completeDefaultArguments = CompleteDefaultArguments(processResolver)
        private val everyInputProduct =
            TemplateExpression.eProcessFinal.expression.eProcess.inputs compose
                    Every.list() compose
                    ETechnoExchange.product.eConstrainedProduct


        fun compile(expression: TemplateExpression): SystemObject {
            val state = SystemObject.empty()
            recursiveCompile(state, expression)
            return state
        }

        private fun recursiveCompile(
            state: SystemObject,
            expression: TemplateExpression,
        ) {
            // eval
            val e = completeDefaultArguments.apply(expression)
            val p = reduceAndComplete.apply(e)
            val v = p.toValue()

            // termination condition
            if (state.containsProcess(v)) {
                return
            }

            // add evaluated process
            state.addProcess(v)

            // add substance characterizations
            val everySubstance =
                TemplateExpression.eProcessFinal.expression.eProcess.biosphere compose
                        Every.list() compose
                        EBioExchange.substance.eSubstance
            everySubstance.getAll(p).forEach { substance ->
                symbolTable.getSubstanceCharacterization(substance.name)?.let {
                    val scv = reduceAndComplete.apply(it).toValue()
                    state.addSubstanceCharacterization(scv)
                }
            }

            // recursively visit process template instances
            everyInputProduct.getAll(p)
                .forEach {
                    val candidate = resolveAndCheckCandidates(it) ?: return
                    val template = candidate.second
                    val arguments = when (it.constraint) {
                        is FromProcessRef -> it.constraint.arguments
                        None -> emptyMap()
                    }
                    recursiveCompile(state, EInstance(template, arguments))
                }
        }


        private fun resolveAndCheckCandidates(product: EConstrainedProduct): Pair<String, TemplateExpression>? {
            val eProduct = when (product.product) {
                is EProduct -> product.product
                is EProductRef -> productResolver.resolve(product.product.name)
            } ?: throw EvaluatorException("unbound product ${product.product}")
            return when (product.constraint) {
                is FromProcessRef -> {
                    val processRef = product.constraint.ref
                    val candidates = processResolver.resolveByProductName(eProduct.name)
                    if (candidates.size > 1) {
                        val candidateNames = candidates.map { it.first }
                        throw EvaluatorException("more than one process produces '${eProduct.name}' : $candidateNames")
                    }
                    return candidates
                        .firstOrNull { it.first == processRef }
                        ?: throw EvaluatorException("no process '$processRef' providing '${eProduct.name}' found")
                }

                None -> {
                    val candidates = processResolver.resolveByProductName(eProduct.name)
                    if (candidates.size > 1) {
                        val candidateNames = candidates.map { it.first }
                        throw EvaluatorException("more than one process produces '${eProduct.name}' : $candidateNames")
                    }
                    candidates.firstOrNull()
                }
            }
        }
    }

    internal class CompleteDefaultArguments(
        private val processResolver: ProcessResolver
    ) {
        private val everyInputProduct = TemplateExpression.eProcessTemplate.body.eProcess.inputs compose
                Every.list() compose
                ETechnoExchange.product.eConstrainedProduct

        fun apply(expression: TemplateExpression): TemplateExpression {
            return everyInputProduct.modify(expression) {
                when (it.constraint) {
                    is FromProcessRef -> {
                        val process = processResolver.resolve(it.constraint.ref)
                            ?: throw EvaluatorException("unknown process ${it.constraint.ref}")
                        if (process !is EProcessTemplate) {
                            throw EvaluatorException("${it.constraint.ref} cannot be invoked")
                        }
                        val actualArguments = process.params.plus(it.constraint.arguments)
                        EConstrainedProduct(
                            it.product,
                            FromProcessRef(
                                it.constraint.ref,
                                actualArguments,
                            )
                        )
                    }

                    None -> it
                }
            }
        }
    }

    internal class SystemObject {
        private val processes: MutableSet<ProcessValue> = HashSet()
        private val substanceCharacterizations: MutableSet<SubstanceCharacterizationValue> = HashSet()

        companion object {
            fun empty(): SystemObject {
                return SystemObject()
            }
        }

        fun containsProcess(process: ProcessValue): Boolean {
            return processes.contains(process)
        }

        fun addProcess(process: ProcessValue) {
            processes.add(process)
        }

        fun addSubstanceCharacterization(substanceCharacterization: SubstanceCharacterizationValue) {
            substanceCharacterizations.add(substanceCharacterization)
        }

        fun getProcesses(): Set<ProcessValue> {
            return processes
        }

        fun getSubstanceCharacterizations(): Set<SubstanceCharacterizationValue> {
            return substanceCharacterizations
        }
    }

    internal class Linker(
        private val systemObject: SystemObject
    ) {
        private val everyInputProduct =
            ProcessValue.inputs compose
                    Every.list() compose TechnoExchangeValue.product

        fun link(): SystemValue {
            val processes = systemObject.getProcesses()
                .map { process ->
                    everyInputProduct.modify(process) { product ->
                        match(product) ?: product
                    }
                }
            val substanceCharacterizations = systemObject.getSubstanceCharacterizations().toList()
            return SystemValue(
                processes,
                substanceCharacterizations,
            )
        }

        private fun match(product: ProductValue): ProductValue? {
            val candidates = systemObject.getProcesses()
                .flatMap { it.products }
                .map { it.product }
                .filter { it.leq(product) }
                .let { minimal(it) }
            if (candidates.size > 1) {
                val defaultCandidate = candidates.firstOrNull { it.isDefault() }
                return defaultCandidate ?: throw EvaluatorException("more than two provided products match product")
            }
            return candidates.firstOrNull()
        }
    }
}

private fun ProductValue.leq(product: ProductValue): Boolean {
    return this.name == product.name
            && this.referenceUnit == product.referenceUnit
            && this.constraint.leq(product.constraint)
}

private fun ProductValue.isDefault(): Boolean {
    return when(this.constraint) {
        is FromProcessRefValue -> this.constraint.flag == ConstraintFlag.IS_DEFAULT
        NoneValue -> false
    }
}

private fun ConstraintValue.leq(constraint: ConstraintValue): Boolean {
    return when (this) {
        is FromProcessRefValue -> when (constraint) {
            is FromProcessRefValue -> this.name == constraint.name
                    && constraint.arguments.all { this.arguments[it.key] == it.value }

            NoneValue -> true
        }

        NoneValue -> constraint == NoneValue
    }
}

private fun minimal(products: List<ProductValue>): List<ProductValue> {
    if (products.isEmpty()) {
        return emptyList()
    }
    val head = products.first()
    val tail = minimal(products.tail())
    if (tail.any { it.leq(head) }) {
        return tail
    }
    return tail.filter { !head.leq(it) }.plus(head)
}
