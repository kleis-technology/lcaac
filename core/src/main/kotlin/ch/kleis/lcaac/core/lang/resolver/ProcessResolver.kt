package ch.kleis.lcaac.core.lang.resolver

import ch.kleis.lcaac.core.assessment.AnalysisProgram
import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.step.CompleteTerminals
import ch.kleis.lcaac.core.lang.evaluator.step.Reduce
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.ProcessKey
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.Operations
import ch.kleis.lcaac.core.matrix.ImpactFactorMatrix
import kotlin.collections.plus

interface ProcessResolver<Q, M> {
    fun resolve(template: EProcessTemplate<Q>, spec: EProductSpec<Q>): EProcess<Q>
}

class CachedProcessResolver<Q, M>(
    val symbolTable: SymbolTable<Q>,
    val ops: Operations<Q, M>,
    val sourceOps: DataSourceOperations<Q>,
) : ProcessResolver<Q, M> {
    override fun resolve(template: EProcessTemplate<Q>, spec: EProductSpec<Q>): EProcess<Q> {
        val trace = getTrace(template, spec)
        val entryPoint = trace.getEntryPoint()
        val analysis = AnalysisProgram(trace.getSystemValue(), entryPoint, ops).run()

        val inputs = analysis.impactFactors.getInputs().map {
            ETechnoExchange(
                quantity = getInputQuantity(entryPoint.products, it, analysis.impactFactors, ops).toEQuantityScale(),
                product = EProductSpec(
                    it.name,
                    it.referenceUnit.toEUnitLiteral(),
                    it.fromProcessRef?.let { toFromProcess(it) }
                )
            )
        }

        val biosphere = analysis.impactFactors.getEmissions().map {
            EBioExchange(
                quantity = getInputQuantity(entryPoint.products, it, analysis.impactFactors, ops).toEQuantityScale(),
                substance = when (it) {
                    is FullyQualifiedSubstanceValue -> ESubstanceSpec(
                        name = it.getShortName(),
                        displayName = it.getDisplayName(),
                        type = it.type,
                        compartment = it.compartment,
                        subCompartment = it.subcompartment,
                        referenceUnit = it.referenceUnit.toEUnitLiteral()
                    )
                    is PartiallyQualifiedSubstanceValue -> ESubstanceSpec(
                        name = it.getShortName(),
                        displayName = it.getDisplayName(),
                        referenceUnit = it.referenceUnit.toEUnitLiteral()
                    )
                }
            )
        }

        val impacts = analysis.impactFactors.getImpacts().map {
            EImpact(
                quantity = getInputQuantity(entryPoint.products, it, analysis.impactFactors, ops).toEQuantityScale(),
                indicator = EIndicatorSpec(it.name, it.referenceUnit.toEUnitLiteral())
            )
        }

        return template.body.copy(
            products = entryPoint.products.map { toExpression(it)},
            inputs = inputs.map { ETechnoBlockEntry(it) },
            biosphere = biosphere.map { EBioBlockEntry(it) },
            impacts = impacts.map { EImpactBlockEntry(it)}
        )
    }

    private fun getTrace(template: EProcessTemplate<Q>, spec: EProductSpec<Q>): EvaluationTrace<Q> {
        val arguments = template.params.plus(spec.fromProcess?.arguments ?: emptyMap())
        val newAnnotations = template.annotations.filter { it != ProcessAnnotation.CACHED }.toSet()
        val newTemplate = template.copy(annotations = newAnnotations)

        val newSymbolTable = symbolTable.copy(processTemplates = symbolTable.processTemplates.override(
            ProcessKey(newTemplate.body.name, newTemplate.body.labels.mapValues { it.value.value }),
            newTemplate
        ))
        val evaluator = Evaluator(newSymbolTable, ops, sourceOps)

        return evaluator.trace(newTemplate, arguments)
    }

    private fun <Q> toExpression(value: DataValue<Q>): DataExpression<Q> {
        return when (value) {
            is QuantityValue -> value.toEQuantityScale()
            is RecordValue -> value.toERecord()
            is StringValue -> value.toEStringLiteral()
        }
    }

    private fun <Q> toExpression(value: TechnoExchangeValue<Q>): ETechnoExchange<Q> {
        return ETechnoExchange(
            quantity = value.quantity.toEQuantityScale(),
            product = EProductSpec(
                value.product.name,
                value.product.referenceUnit.toEUnitLiteral(),
                value.product.fromProcessRef?.let { toFromProcess(it) }
            ),
            allocation = value.allocation?.toEQuantityScale()
        )
    }

    private fun <Q> toFromProcess(value: FromProcessRefValue<Q>): FromProcess<Q> {
        val labels = MatchLabels(value.matchLabels.map { it.key to it.value.toEStringLiteral() }.toMap())
        val arguments: Map<String, DataExpression<Q>> = value.arguments.map { it.key to toExpression(it.value) }.toMap()
        return FromProcess(value.name, labels, arguments)
    }

    private fun <Q, M> getInputQuantity(products: List<TechnoExchangeValue<Q>>, input: MatrixColumnIndex<Q>, impactFactors: ImpactFactorMatrix<Q, M>, ops: Operations<Q, M>): QuantityValue<Q> {
        return  with(QuantityValueOperations(ops)) {
            products
                .map { impactFactors.characterizationFactor(it.port(), input) * it.quantity }
                .reduce { a, b -> a + b }
        }
    }
}

class BareProcessResolver<Q, M>(
    val symbolTable: SymbolTable<Q>,
    val ops: Operations<Q, M>,
    val sourceOps: DataSourceOperations<Q>,
) : ProcessResolver<Q, M> {
    private val reduceDataExpressions = Reduce(symbolTable, ops, sourceOps)
    private val completeTerminals = CompleteTerminals(ops)

    override fun resolve(template: EProcessTemplate<Q>, spec: EProductSpec<Q>): EProcess<Q> {
        val arguments = template.params.plus(spec.fromProcess?.arguments ?: emptyMap())
        val expression = EProcessTemplateApplication(template, arguments)
        return expression
            .let(reduceDataExpressions::apply)
            .let(completeTerminals::apply)
    }
}