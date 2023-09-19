@file:Suppress("MemberVisibilityCanBePrivate")

package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.Register
import ch.kleis.lcaac.core.lang.RegisterException
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.QuantityOperations
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import org.antlr.v4.runtime.tree.TerminalNode
import java.lang.Double.parseDouble

class CoreMapper<Q>(
    private val ops: QuantityOperations<Q>,
) {
    fun process(
        ctx: LcaLangParser.ProcessDefinitionContext,
        globals: Register<DataExpression<Q>> = Register.empty(),
    ): EProcessTemplate<Q> {
        val name = ctx.name.innerText()
        val labels = ctx.labels()
            .flatMap { it.label_assignment() }
            .associate { it.labelRef().innerText() to EStringLiteral<Q>(it.STRING_LITERAL().innerText()) }
        val locals = ctx.variables()
            .flatMap { it.assignment() }
            .associate { it.dataRef().innerText() to dataExpression(it.dataExpression()) }
        val params = ctx.params()
            .flatMap { it.assignment() }
            .associate { it.dataRef().innerText() to dataExpression(it.dataExpression()) }
        val symbolTable = SymbolTable(
            data = try {
                Register(globals.plus(params).plus(locals))
            } catch (e: RegisterException) {
                throw EvaluatorException("Conflict between local variable(s) ${e.duplicates} and a global definition.")
            },
        )
        val products = ctx.block_products()
            .flatMap { it.technoProductExchange() }
            .map { technoProductExchange(it, symbolTable) }
        val inputs = ctx.block_inputs()
            .flatMap { it.technoInputExchange() }
            .map { technoInputExchange(it) }
        val emissions = ctx.block_emissions()
            .flatMap { it.bioExchange() }
            .map { bioExchange(it, symbolTable, SubstanceType.EMISSION) }
        val resources = ctx.block_resources()
            .flatMap { it.bioExchange() }
            .map { bioExchange(it, symbolTable, SubstanceType.RESOURCE) }
        val landUse = ctx.block_land_use()
            .flatMap { it.bioExchange() }
            .map { bioExchange(it, symbolTable, SubstanceType.LAND_USE) }
        val biosphere = emissions.plus(resources).plus(landUse)
        val impacts = ctx.block_impacts()
            .flatMap { it.impactExchange() }
            .map { impactExchange(it) }
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

    fun impactExchange(ctx: LcaLangParser.ImpactExchangeContext): EImpact<Q> {
        return EImpact(
            dataExpression(ctx.quantity),
            indicatorSpec(ctx.indicator),
        )
    }

    fun indicatorSpec(ctx: LcaLangParser.IndicatorRefContext): EIndicatorSpec<Q> {
        return EIndicatorSpec(
            name = ctx.innerText()
        )
    }

    fun bioExchange(
        ctx: LcaLangParser.BioExchangeContext,
        symbolTable: SymbolTable<Q>,
        type: SubstanceType
    ): EBioExchange<Q> {
        val quantity = dataExpression(ctx.quantity)
        return EBioExchange(
            quantity,
            substanceSpec(ctx.substance, type, quantity, symbolTable)
        )
    }

    fun substanceSpec(
        ctx: LcaLangParser.SubstanceSpecContext,
        type: SubstanceType,
        quantity: DataExpression<Q>,
        symbolTable: SymbolTable<Q>
    ): ESubstanceSpec<Q> {
        return ESubstanceSpec(
            name = ctx.substanceRef().innerText(),
            compartment = ctx.compartmentField()?.STRING_LITERAL()?.innerText(),
            subCompartment = ctx.subCompartmentField()?.STRING_LITERAL()?.innerText(),
            type = type,
            referenceUnit = EUnitOf(EQuantityClosure(symbolTable, quantity)),
        )
    }

    fun substanceSpec(
        ctx: LcaLangParser.SubstanceDefinitionContext,
    ): ESubstanceSpec<Q> {
        return ESubstanceSpec(
            name = ctx.substanceRef().innerText(),
            displayName = ctx.nameField().STRING_LITERAL().innerText(),
            type = substanceTypeField(ctx.typeField()),
            compartment = ctx.compartmentField()?.STRING_LITERAL()?.innerText(),
            subCompartment = ctx.subCompartmentField()?.STRING_LITERAL()?.innerText(),
            referenceUnit = EUnitOf(dataExpression(ctx.referenceUnitField().dataExpression())),
        )
    }

    fun substanceTypeField(typeField: LcaLangParser.TypeFieldContext): SubstanceType {
        return SubstanceType.of(typeField.children[2].text.trim('"'))
    }


    fun technoInputExchange(ctx: LcaLangParser.TechnoInputExchangeContext): ETechnoExchange<Q> {
        return ETechnoExchange(
            dataExpression(ctx.quantity),
            inputProductSpec(ctx.product),
        )
    }

    fun inputProductSpec(ctx: LcaLangParser.InputProductSpecContext): EProductSpec<Q> {
        return EProductSpec(
            name = ctx.productRef().innerText(),
            fromProcess = ctx.processTemplateSpec()?.let { fromProcess(it) }
        )
    }

    fun fromProcess(ctx: LcaLangParser.ProcessTemplateSpecContext): FromProcess<Q> {
        val arguments = ctx
            .comma_sep_arguments()
            ?.argument()
            ?: emptyList()
        val labelSelectors = ctx
            .matchLabels()
            ?.comma_sep_label_selectors()
            ?.labelSelector()
            ?: emptyList()
        return FromProcess(
            name = ctx.processRef().text,
            matchLabels = MatchLabels(
                labelSelectors.associate { selector -> selector.labelRef().text to dataExpression(selector.dataExpression()) }
            ),
            arguments = arguments
                .associate { argument -> argument.parameterRef().text to dataExpression(argument.dataExpression()) },
        )
    }

    fun technoProductExchange(
        ctx: LcaLangParser.TechnoProductExchangeContext,
        symbolTable: SymbolTable<Q>
    ): ETechnoExchange<Q> {
        val quantity = dataExpression(ctx.quantity)
        return ETechnoExchange(
            quantity = quantity,
            product = outputProductSpec(ctx.product, quantity, symbolTable),
            allocation = ctx.product.allocateField()?.let { allocation(it) }
        )
    }

    fun allocation(ctx: LcaLangParser.AllocateFieldContext): DataExpression<Q> {
        return dataExpression(ctx.dataExpression())
    }

    fun outputProductSpec(
        ctx: LcaLangParser.OutputProductSpecContext,
        quantity: DataExpression<Q>,
        symbolTable: SymbolTable<Q>
    ): EProductSpec<Q> {
        return EProductSpec(
            name = ctx.productRef().text,
            referenceUnit = EUnitOf(EQuantityClosure(symbolTable, quantity))
        )
    }

    fun LcaLangParser.UnitDefinitionContext.type(): UnitDefinitionType {
        return this.aliasForField()?.let { UnitDefinitionType.ALIAS }
            ?: UnitDefinitionType.LITERAL
    }

    fun dimension(ctx: LcaLangParser.DimFieldContext): Dimension {
        return Dimension.of(ctx.STRING_LITERAL().innerText())
    }

    fun substanceCharacterization(ctx: LcaLangParser.SubstanceDefinitionContext): ESubstanceCharacterization<Q> {
        val substanceSpec = substanceSpec(ctx)
        val quantity = dataExpression(ctx.referenceUnitField().dataExpression())
        val referenceExchange = EBioExchange(quantity, substanceSpec)
        val impacts = ctx.block_impacts()
            .flatMap { it.impactExchange() }
            .map { impactExchange(it) }
        return ESubstanceCharacterization(
            referenceExchange,
            impacts,
        )
    }

    fun unitAlias(ctx: LcaLangParser.UnitDefinitionContext): EUnitAlias<Q> {
        return EUnitAlias(
            ctx.symbolField().STRING_LITERAL().innerText(),
            dataExpression(ctx.aliasForField().dataExpression()),
        )
    }

    fun unitLiteral(ctx: LcaLangParser.UnitDefinitionContext): EUnitLiteral<Q> {
        return EUnitLiteral(
            UnitSymbol.of(ctx.symbolField().STRING_LITERAL().innerText()),
            1.0,
            dimension(ctx.dimField()),
        )
    }

    fun dataExpression(ctx: LcaLangParser.DataExpressionContext): DataExpression<Q> {
        return when (ctx) {
            is LcaLangParser.AddGroupContext -> {
                val left = dataExpression(ctx.left)
                val right = dataExpression(ctx.right)
                when (ctx.op.text) {
                    ctx.PLUS()?.innerText() -> EQuantityAdd(left, right)
                    ctx.MINUS()?.innerText() -> EQuantitySub(left, right)
                    else -> throw IllegalStateException(
                        String.format(
                            "Error while parsing operation %s %s %s",
                            ctx.left.text,
                            ctx.op.text,
                            ctx.right.text
                        )
                    )
                }
            }

            is LcaLangParser.MulGroupContext -> {
                if (ctx.scale != null) {
                    EQuantityScale(
                        ops.pure(parseDouble(ctx.scale.text)),
                        dataExpression(ctx.base),
                    )
                } else {
                    val left = dataExpression(ctx.left)
                    val right = dataExpression(ctx.right)
                    when (ctx.op.text) {
                        ctx.STAR()?.innerText() -> EQuantityMul(left, right)
                        ctx.SLASH()?.innerText() -> EQuantityDiv(left, right)
                        else -> throw IllegalStateException(
                            String.format(
                                "Error while parsing operation %s %s %s",
                                ctx.left.text,
                                ctx.op.text,
                                ctx.right.text
                            )
                        )
                    }
                }
            }

            is LcaLangParser.ExponentialQuantityExpressionContext -> {
                val base = dataExpression(ctx.base)
                val exponent = parseDouble(ctx.exponent.text)
                EQuantityPow(base, exponent)
            }

            is LcaLangParser.BaseGroupContext -> {
                ctx.parenExpression()?.let {
                    dataExpression(it.dataExpression())
                } ?: ctx.stringExpression()?.let {
                    EStringLiteral(it.STRING_LITERAL().innerText())
                } ?: ctx.dataRef()?.let {
                    EDataRef(it.innerText())
                } ?: throw IllegalStateException()
            }

            else -> throw IllegalStateException()
        }
    }

    fun TerminalNode.innerText(): String {
        return this.text.trim('"')
    }

    fun LcaLangParser.SubstanceRefContext.innerText(): String {
        return this.uid().ID().innerText()
    }

    fun LcaLangParser.DimFieldContext.innerText(): String {
        return this.STRING_LITERAL().innerText()
    }

    fun LcaLangParser.DataRefContext.innerText(): String {
        return this.uid().ID().innerText()
    }

    fun LcaLangParser.ProcessRefContext.innerText(): String {
        return this.uid().ID().innerText()
    }

    fun LcaLangParser.LabelRefContext.innerText(): String {
        return this.uid().ID().innerText()
    }

    fun LcaLangParser.IndicatorRefContext.innerText(): String {
        return this.uid().ID().innerText()
    }

    fun LcaLangParser.ProductRefContext.innerText(): String {
        return this.uid().ID().innerText()
    }

    // TODO: Check other "buildUniqueKey"
    fun LcaLangParser.ProcessDefinitionContext.buildUniqueKey(): String {
        val labels = this.labels()
            ?.flatMap { it.label_assignment() }
            ?.associate { it.labelRef().innerText() to it.STRING_LITERAL().innerText() }
            ?: return this.name.innerText()
        return "${this.name.innerText()}$labels"
    }
}
