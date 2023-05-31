package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.RegisterException
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.core.prelude.Prelude.Companion.units
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.ref.PsiIndicatorRef
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProductRef
import ch.kleis.lcaplugin.language.psi.type.spec.PsiSubstanceSpec
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import ch.kleis.lcaplugin.language.psi.type.unit.UnitDefinitionType
import ch.kleis.lcaplugin.psi.*

class LcaLangAbstractParser(
    private val files: Sequence<LcaFile>,
) {
    fun load(): SymbolTable {
        val unitDefinitions = files.flatMap { it.getUnitDefinitions() }
        val processDefinitions = files.flatMap { it.getProcesses() }
        val substanceDefinitions = files.flatMap { it.getSubstances() }

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
            Register(units)
                .plus(
                    unitDefinitions
                        .filter { it.getType() == UnitDefinitionType.LITERAL }
                        .map { it.getUnitRef().getUID().name to unitLiteral(it) }
                        .asIterable()
                )
                .plus(
                    unitDefinitions
                        .filter { it.getType() == UnitDefinitionType.ALIAS }
                        .map { it.getUnitRef().getUID().name to unitAlias(it) }
                        .asIterable()
                )
                .plus(
                    files
                        .flatMap { it.getGlobalAssignments() }
                        .map { it.first to parseQuantityExpression(it.second) }
                        .asIterable()
                )
        } catch (e: RegisterException) {
            throw EvaluatorException("Duplicate global variable ${e.duplicates} defined")
        }

        val processTemplates = try {
            Register.empty<EProcessTemplate>()
                .plus(
                    processDefinitions
                        .map { Pair(it.getProcessTemplateRef().getUID().name, process(it, globals)) }
                        .asIterable()
                )
        } catch (e: RegisterException) {
            throw EvaluatorException("Duplicate process ${e.duplicates} defined")
        }

        return SymbolTable(
            quantities = globals,
            processTemplates = processTemplates,
            dimensions = dimensions,
            substanceCharacterizations = substanceCharacterizations,
        )
    }

    private fun unitLiteral(psiUnitDefinition: PsiUnitDefinition): QuantityExpression {
        return EUnitLiteral(
            UnitSymbol.of(psiUnitDefinition.getSymbolField().getValue()),
            1.0,
            Dimension.of(psiUnitDefinition.getDimensionField().getValue())
        )
    }

    private fun unitAlias(psiUnitAlias: PsiUnitDefinition): QuantityExpression {
        return EUnitAlias(
            psiUnitAlias.getSymbolField().getValue(),
            parseQuantityExpression(psiUnitAlias.getAliasForField().quantityExpression)
        )
    }

    private fun process(
        psiProcess: PsiProcess,
        globals: Register<QuantityExpression>,
    ): EProcessTemplate {
        val name = psiProcess.name
        val locals = psiProcess.getVariables().mapValues { parseQuantityExpression(it.value) }
        val params = psiProcess.getParameters().mapValues { parseQuantityExpression(it.value) }
        val symbolTable = SymbolTable(
            quantities = try {
                Register(globals.plus(params).plus(locals))
            } catch (e: RegisterException) {
                throw EvaluatorException("Conflict between local variable(s) ${e.duplicates} and a global definition.")
            },
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
        val quantity = parseQuantityExpression(psiSubstance.getReferenceUnitField().quantityExpression)
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
            referenceUnit = EUnitOf(parseQuantityExpression(psiSubstance.getReferenceUnitField().quantityExpression)),
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
            referenceUnit = EUnitOf(EQuantityClosure(symbolTable, quantity)),
        )


    private fun impact(exchange: LcaImpactExchange): EImpact {
        return EImpact(
            parseQuantityExpression(exchange.quantityExpression),
            indicatorSpec(exchange.indicatorRef),
        )
    }

    private fun indicatorSpec(variable: PsiIndicatorRef): EIndicatorSpec {
        return EIndicatorSpec(
            variable.name
        )
    }

    private fun technoInputExchange(psiExchange: LcaTechnoInputExchange): ETechnoExchange {
        return ETechnoExchange(
            parseQuantityExpression(psiExchange.quantityExpression),
            productSpec(psiExchange.productRef, psiExchange.fromProcessConstraint),
        )
    }

    private fun productSpec(
        psiProductRef: PsiProductRef,
        psiFromProcessConstraint: LcaFromProcessConstraint? = null,
    ): EProductSpec {
        return EProductSpec(
            psiProductRef.name,
            fromProcessRef = fromProcessRef(psiFromProcessConstraint),
        )
    }

    private fun fromProcessRef(psiFromProcessConstraint: LcaFromProcessConstraint?): FromProcessRef? {
        return psiFromProcessConstraint?.let {
            FromProcessRef(
                ref = it.processTemplateRef!!.name,
                arguments = psiFromProcessConstraint
                    .argumentList
                    .associate { arg -> arg.parameterRef.name to parseQuantityExpression(arg.quantityExpression) }
            )
        }
    }

    private fun technoProductExchange(
        psiExchange: PsiTechnoProductExchange,
        symbolTable: SymbolTable
    ): ETechnoExchange =
        ETechnoExchange(
            parseQuantityExpression(psiExchange.getQuantity()),
            productSpec(psiExchange.getProductRef())
                .copy(
                    referenceUnit = EUnitOf(
                        EQuantityClosure(
                            symbolTable,
                            parseQuantityExpression(psiExchange.getQuantity())
                        )
                    )
                ),
            psiExchange.getAllocateField()?.let { allocation(it) }
                ?: EQuantityScale(100.0, units["percent"]!!)
        )

    private fun allocation(element: LcaAllocateField): QuantityExpression {
        return parseQuantityExpression(element.quantityExpression)
    }

    private fun bioExchange(psiExchange: LcaBioExchange, polarity: Polarity, symbolTable: SymbolTable): EBioExchange {
        return when (polarity) {
            Polarity.POSITIVE -> {
                val quantity = parseQuantityExpression(psiExchange.quantityExpression)
                EBioExchange(
                    quantity,
                    substanceSpec(psiExchange.substanceSpec, quantity, symbolTable)
                )
            }

            Polarity.NEGATIVE -> {
                val quantity = EQuantityScale(-1.0, parseQuantityExpression(psiExchange.quantityExpression))
                EBioExchange(
                    quantity,
                    substanceSpec(psiExchange.substanceSpec, quantity, symbolTable)
                )
            }
        }
    }

    private fun parseQuantityExpression(lcaQuantityExpression: LcaQuantityExpression): QuantityExpression {
        fun getBinaryBranches(expr: LcaBinaryOperatorExpression) = Pair(
            parseQuantityExpression(expr.left),
            parseQuantityExpression(expr.right!!)
        )

        return when (lcaQuantityExpression) {
            is LcaQuantityRef -> EQuantityRef(lcaQuantityExpression.name)

            is LcaScaleQuantityExpression -> EQuantityScale(
                lcaQuantityExpression.scale.text.toDouble(),
                parseQuantityExpression(lcaQuantityExpression.quantityExpression!!)
            )

            is LcaParenQuantityExpression -> parseQuantityExpression(lcaQuantityExpression.quantityExpression!!)

            is LcaExponentialQuantityExpression -> EQuantityPow(
                parseQuantityExpression(lcaQuantityExpression.quantityExpression),
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
}

private enum class Polarity {
    POSITIVE, NEGATIVE
}
