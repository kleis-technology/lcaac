package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.RegisterException
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.math.QuantityOperations
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.ref.PsiIndicatorRef
import ch.kleis.lcaplugin.language.psi.type.spec.PsiSubstanceSpec
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import ch.kleis.lcaplugin.language.psi.type.unit.UnitDefinitionType
import ch.kleis.lcaplugin.psi.*

class LcaLangAbstractParser<Q>(
    private val files: Sequence<LcaFile>,
    private val ops: QuantityOperations<Q>,
) {
    fun load(): SymbolTable<Q> {
        val unitDefinitions = files.flatMap { it.getUnitDefinitions() }
        val processDefinitions = files.flatMap { it.getProcesses() }
        val substanceDefinitions = files.flatMap { it.getSubstances() }

        val dimensions: Register<Dimension> = try {
            Register.empty<Dimension>()
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
            Register.empty<ESubstanceCharacterization<Q>>()
                .plus(
                    substanceDefinitions
                        .map { Pair(it.buildUniqueKey(), substanceCharacterization(it)) }
                        .asIterable()
                )
        } catch (e: RegisterException) {
            throw EvaluatorException("Duplicate substance ${e.duplicates} defined")
        }

        val globals: Register<DataExpression<Q>> = try {
            Register.empty<DataExpression<Q>>()
                .plus(
                    unitDefinitions
                        .filter { it.getType() == UnitDefinitionType.LITERAL }
                        .map { it.getUnitRef().getUID().name to (unitLiteral(it)) }
                        .asIterable()
                )
                .plus(
                    unitDefinitions
                        .filter { it.getType() == UnitDefinitionType.ALIAS }
                        .map { it.getUnitRef().getUID().name to (unitAlias(it)) }
                        .asIterable()
                )
                .plus(
                    files
                        .flatMap { it.getGlobalAssignments() }
                        .map { it.first to this.parseDataExpression(it.second) }
                        .asIterable()
                )
        } catch (e: RegisterException) {
            throw EvaluatorException("Duplicate global variable ${e.duplicates} defined")
        }

        val processTemplates = try {
            Register.empty<EProcessTemplate<Q>>()
                .plus(
                    processDefinitions
                        .map { Pair(it.buildUniqueKey(), process(it, globals)) }
                        .asIterable()
                )
        } catch (e: RegisterException) {
            throw EvaluatorException("Duplicate process ${e.duplicates} defined")
        }

        return SymbolTable(
            data = globals,
            processTemplates = processTemplates,
            dimensions = dimensions,
            substanceCharacterizations = substanceCharacterizations,
        )
    }

    private fun unitLiteral(psiUnitDefinition: PsiUnitDefinition): DataExpression<Q> {
        return EUnitLiteral(
            UnitSymbol.of(psiUnitDefinition.getSymbolField().getValue()),
            1.0,
            Dimension.of(psiUnitDefinition.getDimensionField().getValue())
        )
    }

    private fun unitAlias(psiUnitAlias: PsiUnitDefinition): DataExpression<Q> {
        return EUnitAlias(
            psiUnitAlias.getSymbolField().getValue(),
            parseDataExpression(psiUnitAlias.getAliasForField().dataExpression)
        )
    }

    private fun process(
        psiProcess: PsiProcess,
        globals: Register<DataExpression<Q>>,
    ): EProcessTemplate<Q> {
        val name = psiProcess.name
        val labels = psiProcess.getLabels().mapValues { EStringLiteral<Q>(it.value) }
        val locals = psiProcess.getVariables().mapValues { parseDataExpression(it.value) }
        val params = psiProcess.getParameters().mapValues { parseDataExpression(it.value) }
        val symbolTable = SymbolTable(
            data = try {
                Register(globals.plus(params).plus(locals))
            } catch (e: RegisterException) {
                throw EvaluatorException("Conflict between local variable(s) ${e.duplicates} and a global definition.")
            },
        )
        val products = generateTechnoProductExchanges(psiProcess, symbolTable)
        val inputs = psiProcess.getInputs().map { technoInputExchange(it) }
        val emissions = psiProcess.getEmissions().map { bioExchange(it, symbolTable) }
        val landUse = psiProcess.getLandUse().map { bioExchange(it, symbolTable) }
        val resources = psiProcess.getResources().map { bioExchange(it, symbolTable) }
        val biosphere = emissions.plus(resources).plus(landUse)
        val impacts = psiProcess.getImpacts().map(::impact)
        val body = EProcess(
            name = name,
            labels = labels,
            products = products,
            inputs = inputs,
            biosphere = biosphere,
            impacts = impacts,
        )
        return EProcessTemplate(
            params,
            locals,
            body,
        )
    }

    private fun generateTechnoProductExchanges(
        psiProcess: PsiProcess,
        symbolTable: SymbolTable<Q>,
    ): List<ETechnoExchange<Q>> {
        return psiProcess.getProducts().map { technoProductExchange(it, symbolTable) }
    }

    private fun substanceCharacterization(psiSubstance: PsiSubstance): ESubstanceCharacterization<Q> {
        val substanceSpec = substanceSpec(psiSubstance)
        val quantity = parseDataExpression(psiSubstance.getReferenceUnitField().dataExpression)
        val referenceExchange = EBioExchange(quantity, substanceSpec)
        val impacts = psiSubstance.getImpactExchanges().map { impact(it) }

        return ESubstanceCharacterization(
            referenceExchange,
            impacts,
        )
    }

    private fun substanceSpec(psiSubstance: PsiSubstance): ESubstanceSpec<Q> {
        return ESubstanceSpec(
            name = psiSubstance.getSubstanceRef().name,
            displayName = psiSubstance.getNameField().getValue(),
            type = SubstanceType.of(psiSubstance.getTypeField().getValue()),
            compartment = psiSubstance.getCompartmentField().getValue(),
            subCompartment = psiSubstance.getSubcompartmentField()?.getValue(),
            referenceUnit = EUnitOf(parseDataExpression(psiSubstance.getReferenceUnitField().dataExpression)),
        )
    }

    private fun substanceSpec(
        substanceSpec: PsiSubstanceSpec,
        quantity: DataExpression<Q>,
        symbolTable: SymbolTable<Q>
    ): ESubstanceSpec<Q> =
        ESubstanceSpec(
            name = substanceSpec.name,
            compartment = substanceSpec.getCompartmentField()?.getValue(),
            subCompartment = substanceSpec.getSubCompartmentField()?.getValue(),
            type = substanceSpec.getType(),
            referenceUnit = EUnitOf(EQuantityClosure(symbolTable, quantity)),
        )


    private fun impact(exchange: LcaImpactExchange): EImpact<Q> {
        return EImpact(
            parseDataExpression(exchange.dataExpression),
            indicatorSpec(exchange.indicatorRef),
        )
    }

    private fun indicatorSpec(variable: PsiIndicatorRef): EIndicatorSpec<Q> {
        return EIndicatorSpec(
            variable.name
        )
    }

    private fun technoInputExchange(psiExchange: LcaTechnoInputExchange): ETechnoExchange<Q> {
        return ETechnoExchange(
            parseDataExpression(psiExchange.dataExpression),
            inputProductSpec(psiExchange.inputProductSpec),
        )
    }

    private fun outputProductSpec(
        outputProductSpec: LcaOutputProductSpec,
    ): EProductSpec<Q> {
        return EProductSpec(
            outputProductSpec.name
        )
    }

    private fun inputProductSpec(
        inputProductSpec: LcaInputProductSpec,
    ): EProductSpec<Q> {
        return EProductSpec(
            inputProductSpec.name,
            fromProcess = inputProductSpec.getProcessTemplateSpec()?.let { fromProcess(it) },
        )
    }

    private fun fromProcess(spec: LcaProcessTemplateSpec): FromProcess<Q> {
        val arguments = spec.argumentList
        val labelSelectors = spec.getMatchLabels()?.labelSelectorList ?: emptyList()
        return FromProcess(
            name = spec.name,
            matchLabels = MatchLabels(
                labelSelectors
                    .associate { selector -> selector.labelRef.name to parseDataExpression(selector.dataExpression) }
            ),
            arguments = arguments
                .associate { arg -> arg.parameterRef.name to parseDataExpression(arg.dataExpression) }
        )
    }

    private fun technoProductExchange(
        psiExchange: LcaTechnoProductExchange,
        symbolTable: SymbolTable<Q>,
    ): ETechnoExchange<Q> =
        ETechnoExchange(
            parseDataExpression(psiExchange.dataExpression),
            outputProductSpec(psiExchange.outputProductSpec)
                .copy(
                    referenceUnit = EUnitOf(
                        EQuantityClosure(
                            symbolTable,
                            parseDataExpression(psiExchange.dataExpression)
                        )
                    )
                ),
            psiExchange.outputProductSpec.allocateField?.let { allocation(it) }
                ?: EQuantityScale(
                    ops.pure(100.0),
                    EDataRef("percent")
                )
        )

    private fun allocation(element: LcaAllocateField): DataExpression<Q> {
        return parseDataExpression(element.dataExpression)
    }

    private fun bioExchange(psiExchange: LcaBioExchange, symbolTable: SymbolTable<Q>): EBioExchange<Q> {
        val quantity = parseDataExpression(psiExchange.dataExpression)
        return EBioExchange(
            quantity,
            substanceSpec(psiExchange.substanceSpec, quantity, symbolTable)
        )
    }

    private fun parseDataExpression(dataExpression: LcaDataExpression): DataExpression<Q> {
        fun getBinaryBranches(expr: LcaBinaryOperatorExpression) = Pair(
            parseDataExpression(expr.left),
            parseDataExpression(expr.right!!)
        )

        return when (dataExpression) {
            is LcaDataRef -> EDataRef(dataExpression.name)

            is LcaScaleQuantityExpression -> EQuantityScale(
                ops.pure(dataExpression.scale.text.toDouble()),
                parseDataExpression(dataExpression.dataExpression!!)
            )

            is LcaParenQuantityExpression -> parseDataExpression(dataExpression.dataExpression!!)

            is LcaExponentialQuantityExpression -> EQuantityPow(
                parseDataExpression(dataExpression.dataExpression),
                dataExpression.exponent.text.toDouble()
            )

            is LcaDivQuantityExpression -> getBinaryBranches(dataExpression).let { (left, right) ->
                EQuantityDiv(left, right)
            }

            is LcaMulQuantityExpression -> getBinaryBranches(dataExpression).let { (left, right) ->
                EQuantityMul(left, right)
            }

            is LcaAddQuantityExpression -> getBinaryBranches(dataExpression).let { (left, right) ->
                EQuantityAdd(left, right)
            }

            is LcaSubQuantityExpression -> getBinaryBranches(dataExpression).let { (left, right) ->
                EQuantitySub(left, right)
            }

            is LcaStringExpression -> EStringLiteral(dataExpression.text.trim('"'))
            else -> throw EvaluatorException("Unknown data expression: $dataExpression")
        }
    }
}
