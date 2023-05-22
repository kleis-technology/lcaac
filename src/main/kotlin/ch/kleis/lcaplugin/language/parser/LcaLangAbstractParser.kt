package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.RegisterException
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.PsiFromProcessConstraint
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.enums.MultiplicativeOperationType
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiBioExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiImpactExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.ref.PsiIndicatorRef
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProductRef
import ch.kleis.lcaplugin.language.psi.type.ref.PsiUnitRef
import ch.kleis.lcaplugin.language.psi.type.spec.PsiSubstanceSpec
import ch.kleis.lcaplugin.language.psi.type.unit.*
import ch.kleis.lcaplugin.psi.*

class LcaLangAbstractParser(
    private val files: Sequence<LcaFile>,
) {
    fun load(): SymbolTable {
        val unitDefinitions = files.flatMap { it.getUnitDefinitions() }
        val processDefinitions = files.flatMap { it.getProcesses() }
        val substanceDefinitions = files.flatMap { it.getSubstances() }
        val units = try {
            Register(Prelude.units)
                .plus(
                    unitDefinitions
                        .filter { it.getType() == UnitDefinitionType.LITERAL }
                        .map { Pair(it.getUnitRef().getUID().name, unitLiteral(it)) }
                        .asIterable()
                )
                .plus(
                    unitDefinitions
                        .filter { it.getType() == UnitDefinitionType.ALIAS }
                        .map { Pair(it.getUnitRef().getUID().name, unitAlias(it)) }
                        .asIterable()
                )
        } catch (e: RegisterException) {
            throw EvaluatorException("Duplicate unit ${e.duplicates} defined")
        }

        val dimensions: Register<Dimension> = try {
            Register(Prelude.primitiveDimensions)
                .plus(
                    unitDefinitions
                        .filter { it.getType() == UnitDefinitionType.LITERAL }
                        .map { it.getDimensionField().getValue() to Dimension.of(it.getDimensionField().getValue()) }
                        .asIterable()
                )
        } catch (e: RegisterException) {
            throw EvaluatorException("Duplicate reference units for dimensions ${e.duplicates}")
        }

        val substanceCharacterizations = try {
            Register.empty<ESubstanceCharacterization>()
                .plus(
                    substanceDefinitions
                        .map { Pair(it.buildUniqueKey(), substanceCharacterization(it)) }
                        .asIterable()
                )
        } catch (e: RegisterException) {
            throw EvaluatorException("Duplicate substance ${e.duplicates} defined")
        }

        val globals: Register<QuantityExpression> = try {
            Register(Prelude.unitQuantities)
                .plus(
                    unitDefinitions
                        .filter { it.getType() == UnitDefinitionType.LITERAL }
                        .map { it.getUnitRef().getUID().name to EQuantityLiteral(1.0, unitLiteral(it)) }
                        .asIterable()
                )
                .plus(
                    unitDefinitions
                        .filter { it.getType() == UnitDefinitionType.ALIAS }
                        .map { it.getUnitRef().getUID().name to EQuantityLiteral(1.0, unitAlias(it)) }
                        .asIterable()
                )
                .plus(
                    files
                        .flatMap { it.getGlobalAssignments() }
                        .map { it.first to quantityExpression(it.second) }
                        .asIterable()
                )
        } catch (e: RegisterException) {
            throw EvaluatorException("Duplicate global variable ${e.duplicates} defined")
        }

        val processTemplates = try {
            Register.empty<EProcessTemplate>()
                .plus(
                    processDefinitions
                        .map { Pair(it.getProcessTemplateRef().getUID().name, process(it, globals, units)) }
                        .asIterable()
                )
        } catch (e: RegisterException) {
            throw EvaluatorException("Duplicate process ${e.duplicates} defined")
        }

        return SymbolTable(
            quantities = globals,
            processTemplates = processTemplates,
            units = units,
            dimensions = dimensions,
            substanceCharacterizations = substanceCharacterizations,
        )
    }

    private fun unitLiteral(psiUnitDefinition: PsiUnitDefinition): UnitExpression {
        return EUnitLiteral(
            psiUnitDefinition.getSymbolField().getValue(),
            1.0,
            Dimension.of(psiUnitDefinition.getDimensionField().getValue())
        )
    }

    private fun unitAlias(psiUnitAlias: PsiUnitDefinition): UnitExpression {
        return EUnitAlias(
            psiUnitAlias.getSymbolField().getValue(),
            quantityExpression(psiUnitAlias.getAliasForField().getValue())
        )
    }

    private fun process(
        psiProcess: PsiProcess,
        globals: Register<QuantityExpression>,
        units: Register<UnitExpression>
    ): EProcessTemplate {
        val name = psiProcess.name
        val locals = psiProcess.getVariables().mapValues { quantityExpression(it.value) }
        val params = psiProcess.getParameters().mapValues { quantityExpression(it.value) }
        val symbolTable = SymbolTable(
            quantities = Register(globals.plus(params).plus(locals)),
            units = Register(units),
        )
        val products = generateTechnoProductExchanges(psiProcess, symbolTable)
        val inputs = psiProcess.getInputs().map { technoInputExchange(it) }
        val emissions = psiProcess.getEmissions().map { bioExchange(it, Polarity.POSITIVE, symbolTable) }
        val landUse = psiProcess.getLandUse().map { bioExchange(it, Polarity.POSITIVE, symbolTable) }
        val resources = psiProcess.getResources().map { bioExchange(it, Polarity.NEGATIVE, symbolTable) }
        val biosphere = emissions.plus(resources).plus(landUse)
        val body = EProcess(
            name = name,
            products = products,
            inputs = inputs,
            biosphere = biosphere,
        )
        return EProcessTemplate(
            params,
            locals,
            body,
        )
    }

    private fun generateTechnoProductExchanges(
        psiProcess: PsiProcess,
        symbolTable: SymbolTable,
    ): List<ETechnoExchange> {
        return psiProcess.getProducts().map { technoProductExchange(it, symbolTable) }
    }

    private fun substanceCharacterization(psiSubstance: PsiSubstance): ESubstanceCharacterization {
        val substanceSpec = substanceSpec(psiSubstance)
        val quantity = EQuantityLiteral(1.0, unit(psiSubstance.getReferenceUnitField().getValue()))
        val referenceExchange = EBioExchange(quantity, substanceSpec)
        val impacts = psiSubstance.getImpactExchanges().map { impact(it) }

        return ESubstanceCharacterization(
            referenceExchange,
            impacts,
        )
    }

    private fun substanceSpec(psiSubstance: PsiSubstance): ESubstanceSpec {
        return ESubstanceSpec(
            name = psiSubstance.getSubstanceRef().name,
            displayName = psiSubstance.getNameField().getValue(),
            type = SubstanceType.of(psiSubstance.getTypeField().getValue()),
            compartment = psiSubstance.getCompartmentField().getValue(),
            subCompartment = psiSubstance.getSubcompartmentField()?.getValue(),
            referenceUnit = unit(psiSubstance.getReferenceUnitField().getValue()),
        )
    }

    private fun substanceSpec(
        substanceSpec: PsiSubstanceSpec,
        quantity: QuantityExpression,
        symbolTable: SymbolTable
    ): ESubstanceSpec =
        ESubstanceSpec(
            name = substanceSpec.name,
            compartment = substanceSpec.getCompartmentField()?.getValue(),
            subCompartment = substanceSpec.getSubCompartmentField()?.getValue(),
            type = substanceSpec.getType(),
            referenceUnit = EUnitClosure(symbolTable, EUnitOf(quantity))
        )


    private fun impact(exchange: PsiImpactExchange): EImpact {
        return EImpact(
            quantityExpression(exchange.getQuantity()),
            indicatorSpec(exchange.getIndicatorRef()),
        )
    }

    private fun indicatorSpec(variable: PsiIndicatorRef): EIndicatorSpec {
        return EIndicatorSpec(
            variable.name
        )
    }

    private fun technoInputExchange(psiExchange: LcaTechnoInputExchange): ETechnoExchange {
        return ETechnoExchange(
            quantityExpression(psiExchange.quantityExpression),
            productSpec(psiExchange.productRef, psiExchange.fromProcessConstraint),
        )
    }

    private fun productSpec(
        psiProductRef: PsiProductRef,
        psiFromProcessConstraint: PsiFromProcessConstraint? = null,
    ): EProductSpec {
        return EProductSpec(
            psiProductRef.name,
            fromProcessRef = fromProcessRef(psiFromProcessConstraint),
        )
    }

    private fun fromProcessRef(psiFromProcessConstraint: PsiFromProcessConstraint?): FromProcessRef? {
        return psiFromProcessConstraint?.let {
            FromProcessRef(
                ref = it.getProcessTemplateRef().name,
                arguments = psiFromProcessConstraint.getArguments().mapValues { q -> quantityExpression(q.value) },
            )
        }
    }

    private fun technoProductExchange(
        psiExchange: PsiTechnoProductExchange,
        symbolTable: SymbolTable
    ): ETechnoExchange =
        ETechnoExchange(
            quantityExpression(psiExchange.getQuantity()),
            productSpec(psiExchange.getProductRef())
                .copy(
                    referenceUnit = EUnitClosure(
                        symbolTable,
                        EUnitOf(quantityExpression(psiExchange.getQuantity()))
                    )
                ),
            psiExchange.getAllocateField()?.let { allocation(it) }
                ?: EQuantityLiteral(100.0, Prelude.units["percent"]!!)
        )

    private fun allocation(element: LcaAllocateField): QuantityExpression {
        return quantityExpression(element.quantityExpression)
    }

    private fun bioExchange(psiExchange: PsiBioExchange, polarity: Polarity, symbolTable: SymbolTable): EBioExchange {
        return when (polarity) {
            Polarity.POSITIVE -> {
                val quantity = quantityExpression(psiExchange.getQuantity())
                EBioExchange(
                    quantity,
                    substanceSpec(psiExchange.getSubstanceSpec(), quantity, symbolTable)
                )
            }

            Polarity.NEGATIVE -> {
                val quantity = EQuantityNeg(quantityExpression(psiExchange.getQuantity()))
                EBioExchange(
                    quantity,
                    substanceSpec(psiExchange.getSubstanceSpec(), quantity, symbolTable)
                )
            }
        }
    }

    private fun quantityExpression(lcaQuantityExpression: LcaQuantityExpression): QuantityExpression {
        fun getBinaryBranches(expr: LcaBinaryOperatorExpression) = Pair(
            quantityExpression(expr.left),
            quantityExpression(expr.right!!)
        )

        return when (lcaQuantityExpression) {
            is LcaQuantityRef -> EQuantityRef(lcaQuantityExpression.name)

            is LcaScaleQuantityExpression -> EQuantityScale(
                lcaQuantityExpression.scale.text.toDouble(),
                quantityExpression(lcaQuantityExpression.quantityExpression!!)
            )

            is LcaParenQuantityExpression -> quantityExpression(lcaQuantityExpression.quantityExpression!!)

            is LcaExponentialQuantityExpression -> EQuantityPow(
                quantityExpression(lcaQuantityExpression.quantityExpression),
                lcaQuantityExpression.exponent.text.toDouble()
            )

            is LcaDivQuantityExpression -> getBinaryBranches(lcaQuantityExpression).let { (left, right) ->
                EQuantityDiv(left, right)
            }

            is LcaMulQuantityExpression -> getBinaryBranches(lcaQuantityExpression).let { (left, right) ->
                EQuantityMul(left, right)
            }

            is LcaAddQuantityExpression -> getBinaryBranches(lcaQuantityExpression).let { (left, right) ->
                EQuantityAdd(left, right)
            }

            is LcaSubQuantityExpression -> getBinaryBranches(lcaQuantityExpression).let { (left, right) ->
                EQuantitySub(left, right)
            }

            else -> throw EvaluatorException("Unknown quantity expression: $lcaQuantityExpression")
        }
    }

    private fun unit(unit: PsiUnit): UnitExpression {
        val factor = uFactor(unit.getFactor())
        return when (unit.getOperationType()) {
            MultiplicativeOperationType.MUL -> EUnitMul(
                factor, unit(unit.getNext()!!)
            )

            MultiplicativeOperationType.DIV -> EUnitDiv(
                factor, unit(unit.getNext()!!),
            )

            null -> factor
        }
    }

    private fun uFactor(factor: PsiUnitFactor): UnitExpression {
        val primitive = uPrimitive(factor.getPrimitive())
        return factor.getExponent()?.let { EUnitPow(primitive, it) }
            ?: primitive
    }

    private fun uPrimitive(primitive: PsiUnitPrimitive): UnitExpression {
        return when (primitive.getType()) {
            UnitPrimitiveType.DEFINITION -> unitLiteral(primitive.getDefinition())
            UnitPrimitiveType.PAREN -> unit(primitive.getUnitInParen())
            UnitPrimitiveType.VARIABLE -> unitRef(primitive.getRef())
        }
    }

    private fun unitRef(unitRef: PsiUnitRef): UnitExpression {
        return EUnitRef(unitRef.name)
    }
}

private enum class Polarity {
    POSITIVE, NEGATIVE
}
