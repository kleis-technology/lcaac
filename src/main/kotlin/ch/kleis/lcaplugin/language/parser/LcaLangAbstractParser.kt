package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.*
import ch.kleis.lcaplugin.language.psi.type.enums.AdditiveOperationType
import ch.kleis.lcaplugin.language.psi.type.enums.CoreExpressionType
import ch.kleis.lcaplugin.language.psi.type.enums.MultiplicativeOperationType
import ch.kleis.lcaplugin.language.psi.type.quantity.*
import ch.kleis.lcaplugin.language.psi.type.unit.*

class LcaLangAbstractParser(
    private val findFilesOf: (String) -> List<LcaFile>,
) {
    private var productCount: Int = 0

    fun collect(pkgName: String): Pair<Package, List<Package>> {
        val visited = HashSet<String>()
        val pkg = Prelude.packages[pkgName] ?: mkPackage(pkgName)
        visited.add(pkgName)
        val dependencies = pkg.imports
            .flatMap {
                val deps = collectDependencies(it.pkgName, visited)
                visited.addAll(deps.map { dep -> dep.name })
                deps
            }
        return Pair(pkg, dependencies)
    }

    private fun collectDependencies(pkgName: String, visited: HashSet<String>): List<Package> {
        if (visited.contains(pkgName)) {
            return emptyList()
        }
        val pkg = Prelude.packages[pkgName] ?: mkPackage(pkgName)
        if (pkg.imports.isEmpty()) {
            return listOf(pkg)
        }
        val dependencies = pkg.imports
            .flatMap {
                val deps = collectDependencies(it.pkgName, visited)
                visited.addAll(deps.map { dep -> dep.name })
                deps
            }
        return dependencies.plus(pkg)
    }

    private fun mkPackage(pkgName: String): Package {
        val files = findFilesOf(pkgName)
        val globals = files
            .flatMap { it.getLocalAssignments() }
            .associate { Pair(it.getUid().name!!, coreExpression(it.getCoreExpression())) }

        val products = files
            .flatMap { it.getProducts() }
            .filter { it.getUid() != null }
            .associate { Pair(it.getUid()?.name!!, product(it)) }

        val processes = files
            .flatMap { it.getProcesses() }
            .filter { it.getUid() != null }
            .associate { Pair(it.getUid()?.name!!, process(it)) }

        val systems = files
            .flatMap { it.getSystems() }
            .filter { it.getUid() != null }
            .associate { Pair(it.getUid()?.name!!, system(it)) }

        val units = files
            .flatMap { it.getUnitLiterals() }
            .filter { it.getUid() != null }
            .associate { Pair(it.getUid()?.name!!, unitLiteral(it)) }

        val definitions = globals
            .plus(units)
            .plus(products)
            .plus(processes)
            .plus(systems)

        val imports = files
            .flatMap { it.getImports() }
            .map {
                when (it.getImportType()) {
                    ImportType.SYMBOL -> ImportSymbol(it.getPackageName(), it.getSymbol()!!)
                    ImportType.WILDCARD -> ImportWildCard(it.getPackageName())
                }
            }

        return Package(
            pkgName,
            imports,
            Environment.of(definitions)
        )
    }


    private fun unitLiteral(psiUnitLiteral: PsiUnitLiteral): Expression {
        return EUnit(
            psiUnitLiteral.getSymbolField().getValue(),
            psiUnitLiteral.getScaleField().getValue(),
            Dimension.of(psiUnitLiteral.getDimensionField().getValue()),
        )
    }

    private fun system(psiSystem: PsiSystem): Expression {
        val locals = locals(psiSystem.getLocalAssignments())
        val params = params(psiSystem.getParameters())
        val subSystems = psiSystem.getSystems().map { system(it) }
        val processes = psiSystem.getProcesses().map { process(it) }
        val includes = psiSystem.getIncludes().map { include(it) }

        var result: Expression = ESystem(
            processes
                .plus(subSystems)
                .plus(includes)
        )
        if (locals.isNotEmpty()) {
            result = ELet(locals, result)
        }
        if (params.isNotEmpty()) {
            result = ETemplate(params, result)
        }
        return result
    }

    private fun include(psiInclude: PsiInclude): Expression {
        val template = EVar(psiInclude.name!!)
        val arguments = psiInclude.getArgumentAssignments()
            .associate { Pair(it.getUid().name!!, coreExpression(it.getCoreExpression())) }
        return EInstance(template, arguments)
    }

    private fun locals(assignments: Collection<PsiAssignment>): Map<String, Expression> {
        return assignments
            .associate { Pair(it.getUid().name!!, coreExpression(it.getCoreExpression())) }
    }

    private fun params(parameters: Collection<PsiParameter>): Map<String, Expression?> {
        return parameters
            .associate {
                Pair(
                    it.getUid().name!!,
                    it.getCoreExpression()?.let { e -> coreExpression(e) }
                )
            }
    }

    private fun process(psiProcess: PsiProcess): Expression {
        val locals = locals(psiProcess.getLocalAssignments())
        val params = params(psiProcess.getParameters())
        val blocks = psiProcess.getBlocks().map { block(it) }
        val exchanges = psiProcess.getExchanges().map { exchange(it, Polarity.POSITIVE) }
        val includes = psiProcess.getIncludes().map { include(it) }

        var result: Expression = EProcess(
            exchanges
                .plus(blocks)
                .plus(includes)
        )
        if (locals.isNotEmpty()) {
            result = ELet(locals, result)
        }
        if (params.isNotEmpty()) {
            result = ETemplate(params, result)
        }
        return result
    }

    private fun exchange(
        psiExchange: PsiExchange,
        polarity: Polarity,
    ): Expression {
        return when (polarity) {
            Polarity.POSITIVE -> EExchange(
                quantity(psiExchange.getQuantity()),
                variable(psiExchange.getProduct().name!!),
            )

            Polarity.NEGATIVE -> EExchange(
                ENeg(quantity(psiExchange.getQuantity())),
                variable(psiExchange.getProduct().name!!),
            )
        }
    }

    private fun block(psiBlock: PsiBlock): Expression {
        return EBlock(
            psiBlock.getExchanges().map { exchange(it, psiBlock.getPolarity()) },
        )
    }

    private fun product(psiProduct: PsiProduct): Expression {
        val unit = unit(psiProduct.getReferenceUnitField().getValue())
        val name = psiProduct.getUid()?.name ?: run {
            val result = "product_$productCount"
            productCount += 1
            result
        }
        return EProduct(
            name,
            unit
        )
    }

    private fun quantity(quantity: PsiQuantity): Expression {
        val term = qTerm(quantity.getTerm())
        return when (quantity.getOperationType()) {
            AdditiveOperationType.ADD -> EAdd(
                term, quantity(quantity.getNext()!!)
            )

            AdditiveOperationType.SUB -> ESub(
                term, quantity(quantity.getNext()!!)
            )

            null -> term
        }
    }

    private fun qTerm(term: PsiQuantityTerm): Expression {
        val factor = qFactor(term.getFactor())
        return when (term.getOperationType()) {
            MultiplicativeOperationType.MUL -> EMul(
                factor, qTerm(term.getNext()!!)
            )

            MultiplicativeOperationType.DIV -> EDiv(
                factor, qTerm(term.getNext()!!)
            )

            null -> factor
        }
    }

    private fun qFactor(factor: PsiQuantityFactor): Expression {
        val primitive = qPrimitive(factor.getPrimitive())
        return factor.getExponent()?.let { EPow(primitive, it) }
            ?: primitive
    }

    private fun qPrimitive(primitive: PsiQuantityPrimitive): Expression {
        return when (primitive.getType()) {
            QuantityPrimitiveType.LITERAL -> EQuantity(
                primitive.getAmount()!!,
                unit(primitive.getUnit()!!),
            )

            QuantityPrimitiveType.PAREN -> quantity(primitive.getQuantityInParen()!!)
            QuantityPrimitiveType.VARIABLE -> variable(primitive.getVariable()!!)
        }
    }

    private fun unit(unit: PsiUnit): Expression {
        val factor = uFactor(unit.getFactor())
        return when (unit.getOperationType()) {
            MultiplicativeOperationType.MUL -> EMul(
                factor, unit(unit.getNext()!!)
            )

            MultiplicativeOperationType.DIV -> EDiv(
                factor, unit(unit.getNext()!!),
            )

            null -> factor
        }
    }

    private fun uFactor(factor: PsiUnitFactor): Expression {
        val primitive = uPrimitive(factor.getPrimitive())
        return factor.getExponent()?.let { EPow(primitive, it) }
            ?: primitive
    }

    private fun uPrimitive(primitive: PsiUnitPrimitive): Expression {
        return when (primitive.getType()) {
            UnitPrimitiveType.LITERAL -> unitLiteral(primitive.asLiteral()!!)
            UnitPrimitiveType.PAREN -> unit(primitive.asUnitInParen()!!)
            UnitPrimitiveType.VARIABLE -> variable(primitive.asVariable()!!)
        }
    }

    private fun coreExpression(coreExpression: PsiCoreExpression): Expression {
        return when (coreExpression.getExpressionType()) {
            CoreExpressionType.SYSTEM -> system(coreExpression.asSystem())
            CoreExpressionType.PROCESS -> process(coreExpression.asProcess())
            CoreExpressionType.PRODUCT -> product(coreExpression.asProduct())
            CoreExpressionType.UNIT -> unit(coreExpression.asUnit())
            CoreExpressionType.QUANTITY -> quantity(coreExpression.asQuantity())
            CoreExpressionType.VARIABLE -> variable(coreExpression.asVariable())
            null -> throw IllegalStateException("unknown core expression type")
        }
    }

    private fun variable(psiVariable: PsiVariable): Expression {
        return EVar(psiVariable.name!!)
    }

    private fun variable(name: String): Expression {
        return EVar(name)
    }
}
