package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.LcaFileType
import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.*
import ch.kleis.lcaplugin.language.psi.type.enums.AdditiveOperationType
import ch.kleis.lcaplugin.language.psi.type.enums.CoreExpressionType
import ch.kleis.lcaplugin.language.psi.type.enums.MultiplicativeOperationType
import ch.kleis.lcaplugin.language.psi.type.quantity.*
import ch.kleis.lcaplugin.language.psi.type.unit.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope

class LcaLangAbstractParser {
    fun lcaPackage(pkgName: String, project: Project): Package {
        val psiManager = PsiManager.getInstance(project)
        val files = FileTypeIndex
            .getFiles(LcaFileType.INSTANCE, GlobalSearchScope.projectScope(project))
            .mapNotNull { psiManager.findFile(it) }
            .map { it as LcaFile }
            .filter { it.getPackage().name!! == pkgName }
        if (files.isEmpty()) throw NoSuchElementException(pkgName)

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

        val definitions = Prelude.units
            .plus(globals)
            .plus(units)
            .plus(products)
            .plus(processes)
            .plus(systems)
        return Package(definitions)
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
        val exchanges = psiProcess.getExchanges().map { exchange(it) }
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

    private fun exchange(psiExchange: PsiExchange): Expression {
        return EExchange(
            quantity(psiExchange.getQuantity()),
            variable(psiExchange.getProduct().name!!),
        )
    }

    private fun block(psiBlock: PsiBlock): Expression {
        return EBlock(
            psiBlock.getExchanges().map { exchange(it) },
            psiBlock.getPolarity(),
        )
    }

    private fun product(psiProduct: PsiProduct): Expression {
        val dimension = Dimension.of(psiProduct.getDimensionField().getValue())
        return EProduct(
            psiProduct.getUid()?.name!!,
            dimension,
            psiProduct.getReferenceUnitField()?.let { unit(it.getValue()) }
                ?: EUnit("ref_unit($dimension)", 1.0, dimension),
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
