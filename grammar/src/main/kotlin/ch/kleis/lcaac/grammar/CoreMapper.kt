@file:Suppress("MemberVisibilityCanBePrivate")

package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.*
import ch.kleis.lcaac.core.math.QuantityOperations
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import org.antlr.v4.runtime.tree.TerminalNode
import java.lang.Double.parseDouble

class CoreMapper<Q>(
    private val ops: QuantityOperations<Q>,
) {
    fun process(
        ctx: LcaLangParser.ProcessDefinitionContext,
        globals: DataRegister<Q> = DataRegister.empty(),
        dataSources: Register<DataSourceKey, EDataSource<Q>>,
    ): EProcessTemplate<Q> {
        val name = ctx.name.innerText()
        val labels = ctx.labels().flatMap { it.label_assignment() }.associate { it.labelRef().innerText() to EStringLiteral<Q>(it.STRING_LITERAL().innerText()) }
        val annotations = ctx.annotation().mapNotNull { ProcessAnnotation.fromValue(it.text) }.toSet()

        if(annotations.size != annotations.toSet().size) {
            val annotationTexts = ctx.annotation().mapNotNull { it.text }
            throw EvaluatorException("Duplicate annotations found for process $name: ${annotationTexts}.")
        }

        val locals = ctx.variables().flatMap { it.assignment() }.associate { assignment(it) }
        val params = ctx.params().flatMap { it.assignment() }.associate { assignment(it) }
        val symbolTable = SymbolTable(
            data = try {
                DataRegister(globals.plus(params.mapKeys { DataKey(it.key) }).plus(locals.mapKeys { DataKey(it.key) }))
            } catch (e: RegisterException) {
                throw EvaluatorException("Conflict between local variable(s) ${e.duplicates} and a global definition.")
            },
            dataSources = dataSources,
        )
        val products = ctx.block_products().flatMap { it.technoProductExchange() }.map { technoProductExchange(it, symbolTable) }
        val inputs = ctx.block_inputs().flatMap { it.technoInputExchange() }.map { technoInputExchange(it) }
        val emissions = ctx.block_emissions().flatMap { it.bioExchange() }.map { bioExchange(it, symbolTable, SubstanceType.EMISSION) }
        val resources = ctx.block_resources().flatMap { it.bioExchange() }.map { bioExchange(it, symbolTable, SubstanceType.RESOURCE) }
        val landUse = ctx.block_land_use().flatMap { it.bioExchange() }.map { bioExchange(it, symbolTable, SubstanceType.LAND_USE) }
        val biosphere = emissions.plus(resources).plus(landUse)
        val impacts = ctx.block_impacts().flatMap { it.impactExchange() }.map { impactExchange(it) }
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
            annotations
        )
    }

    fun assignment(ctx: LcaLangParser.AssignmentContext): Pair<String, DataExpression<Q>> {
        return when (ctx.sep.text) {
            ctx.EQUAL()?.innerText() -> ctx.dataRef().innerText() to dataExpression(ctx.dataExpression())
            else -> throw IllegalStateException("parsing error: invalid assignment '${ctx.text}'")
        }
    }

    fun dataSource(ctx: LcaLangParser.DataSourceExpressionContext): DataSourceExpression<Q> {
        val ref = ctx.dataSourceRef().innerText()
        return ctx.rowFilter()?.let { rowFilter ->
            val filter = rowFilter.rowSelector().associate { rowSelector ->
                rowSelector.columnRef().innerText() to dataExpression(rowSelector.dataExpression())
            }
            EFilter(EDataSourceRef(ref), filter)
        } ?: EDataSourceRef(ref)
    }

    fun impactExchange(ctx: LcaLangParser.ImpactExchangeContext): ImpactBlock<Q> {
        return when (ctx) {
            is LcaLangParser.ImpactEntryContext -> EImpactBlockEntry(EImpact(
                dataExpression(ctx.quantity),
                indicatorSpec(ctx.indicator),
            ))

            is LcaLangParser.ImpactBlockForEachContext -> {
                val rowRef = ctx.dataRef().innerText()
                val dataSource = dataSource(ctx.dataSourceExpression())
                val body = ctx.impactExchange().map { this.impactExchange(it) }
                val locals = ctx.variables().flatMap { it.assignment() }.associate { assignment(it) }
                EImpactBlockForEach(rowRef, dataSource, locals, body)
            }

            else -> throw IllegalStateException("parsing error: expecting an impact exchange context")
        }
    }

    fun indicatorSpec(ctx: LcaLangParser.IndicatorRefContext): EIndicatorSpec<Q> {
        return EIndicatorSpec(name = ctx.innerText())
    }

    fun bioExchange(ctx: LcaLangParser.BioExchangeContext, symbolTable: SymbolTable<Q>, type: SubstanceType): BioBlock<Q> {
        return when (ctx) {
            is LcaLangParser.BioEntryContext -> {
                val quantity = dataExpression(ctx.quantity)
                EBioBlockEntry(EBioExchange(
                    quantity,
                    substanceSpec(ctx.substance, type, quantity, symbolTable),
                ))
            }

            is LcaLangParser.BioBlockForEachContext -> {
                val rowRef = ctx.dataRef().innerText()
                val dataSource = dataSource(ctx.dataSourceExpression())
                val body = ctx.bioExchange().map { this.bioExchange(it, symbolTable, type) }
                val locals = ctx.variables().flatMap { it.assignment() }.associate { assignment(it) }
                EBioBlockForEach(rowRef, dataSource, locals, body)
            }

            else -> throw IllegalStateException("parsing error: expecting a bio exchange context")
        }
    }

    fun substanceSpec(ctx: LcaLangParser.SubstanceSpecContext, type: SubstanceType, quantity: DataExpression<Q>, symbolTable: SymbolTable<Q>): ESubstanceSpec<Q> {
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


    fun technoInputExchange(ctx: LcaLangParser.TechnoInputExchangeContext): TechnoBlock<Q> {
        return when (ctx) {
            is LcaLangParser.TechnoEntryContext -> ETechnoBlockEntry(ETechnoExchange(
                dataExpression(ctx.quantity),
                inputProductSpec(ctx.product),
            ))

            is LcaLangParser.TechnoBlockForEachContext -> {
                val rowRef = ctx.dataRef().innerText()
                val dataSource = dataSource(ctx.dataSourceExpression())
                val body = ctx.technoInputExchange().map { this.technoInputExchange(it) }
                val locals = ctx.variables().flatMap { it.assignment() }.associate { assignment(it) }
                ETechnoBlockForEach(rowRef, dataSource, locals, body)
            }

            else -> throw IllegalStateException("parsing error: expecting a techno input exchange context")
        }
    }

    fun inputProductSpec(ctx: LcaLangParser.InputProductSpecContext): EProductSpec<Q> {
        return EProductSpec(name = ctx.productRef().innerText(), fromProcess = ctx.processTemplateSpec()?.let { fromProcess(it) })
    }

    fun fromProcess(ctx: LcaLangParser.ProcessTemplateSpecContext): FromProcess<Q> {
        val arguments = ctx.comma_sep_arguments()?.argument() ?: emptyList()
        val labelSelectors = ctx.matchLabels()?.comma_sep_label_selectors()?.labelSelector() ?: emptyList()
        return FromProcess(
            name = ctx.processRef().text,
            matchLabels = MatchLabels(labelSelectors.associate { selector -> selector.labelRef().text to dataExpression(selector.dataExpression()) }),
            arguments = arguments.associate { argument -> argument.parameterRef().text to dataExpression(argument.dataExpression()) },
        )
    }

    fun technoProductExchange(ctx: LcaLangParser.TechnoProductExchangeContext, symbolTable: SymbolTable<Q>): ETechnoExchange<Q> {
        val quantity = dataExpression(ctx.quantity)
        return ETechnoExchange(quantity = quantity, product = outputProductSpec(ctx.product, quantity, symbolTable), allocation = ctx.product.allocateField()?.let { allocation(it) })
    }

    fun allocation(ctx: LcaLangParser.AllocateFieldContext): DataExpression<Q> {
        return dataExpression(ctx.dataExpression())
    }

    fun outputProductSpec(ctx: LcaLangParser.OutputProductSpecContext, quantity: DataExpression<Q>, symbolTable: SymbolTable<Q>): EProductSpec<Q> {
        return EProductSpec(name = ctx.productRef().text, referenceUnit = EUnitOf(EQuantityClosure(symbolTable, quantity)))
    }

    fun LcaLangParser.UnitDefinitionContext.type(): UnitDefinitionType {
        return this.aliasForField()?.let { UnitDefinitionType.ALIAS } ?: UnitDefinitionType.LITERAL
    }

    fun dimension(ctx: LcaLangParser.DimFieldContext): Dimension {
        return Dimension.of(ctx.STRING_LITERAL().innerText())
    }

    fun substanceCharacterization(ctx: LcaLangParser.SubstanceDefinitionContext): ESubstanceCharacterization<Q> {
        val substanceSpec = substanceSpec(ctx)
        val quantity = dataExpression(ctx.referenceUnitField().dataExpression())
        val referenceExchange = EBioExchange(quantity, substanceSpec)
        val impacts = ctx.block_impacts().flatMap { it.impactExchange() }.map { impactExchange(it) }
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
            is LcaLangParser.RecordGroupContext -> {
                when (ctx.op.text) {
                    ctx.LOOKUP()?.innerText() -> {
                        val dataSource = dataSource(ctx.dataSourceExpression())
                        EFirstRecordOf(dataSource)
                    }

                    ctx.DEFAULT_RECORD()?.innerText() -> {
                        val dataSource = dataSource(ctx.dataSourceExpression())
                        EDefaultRecordOf(dataSource)
                    }

                    else -> throw IllegalStateException("parsing error: invalid primitive '${ctx.op.text}'")
                }
            }

            is LcaLangParser.ColGroupContext -> {
                when (ctx.op.text) {
                    ctx.SUM().innerText() -> {
                        val dataSource = dataSource(ctx.dataSourceExpression())
                        val columns = ctx.columnRef().map { it.innerText() }
                        ESumProduct(dataSource, columns)
                    }

                    else -> throw IllegalStateException("parsing error: invalid primitive '${ctx.op.text}'")
                }
            }

            is LcaLangParser.AddGroupContext -> {
                val left = dataExpression(ctx.left)
                val right = dataExpression(ctx.right)
                when (ctx.op.text) {
                    ctx.PLUS()?.innerText() -> EQuantityAdd(left, right)
                    ctx.MINUS()?.innerText() -> EQuantitySub(left, right)
                    else -> throw IllegalStateException(String.format("Error while parsing operation %s %s %s", ctx.left.text, ctx.op.text, ctx.right.text))
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
                        else -> throw IllegalStateException(String.format("Error while parsing operation %s %s %s", ctx.left.text, ctx.op.text, ctx.right.text))
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
                } ?: ctx.slice()?.let {
                    val ref = EDataRef<Q>(ctx.dataRef().innerText())
                    val columnRef = ctx.slice().columnRef().innerText()
                    ERecordEntry(ref, columnRef)
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
        return this.uid().innerText()
    }

    fun LcaLangParser.DimFieldContext.innerText(): String {
        return this.STRING_LITERAL().innerText()
    }

    fun LcaLangParser.DataRefContext.innerText(): String {
        return this.uid().innerText()
    }

    fun LcaLangParser.ColumnRefContext.innerText(): String {
        return this.uid().innerText()
    }

    fun LcaLangParser.UidContext.innerText(): String {
        return this.ID()?.innerText()
            ?: throw LoaderException("parsing error: invalid uid: ${this.text}")
    }

    fun LcaLangParser.DataSourceRefContext.innerText(): String {
        return this.uid().innerText()
    }

    fun LcaLangParser.ProcessRefContext.innerText(): String {
        return this.uid().innerText()
    }

    fun LcaLangParser.LabelRefContext.innerText(): String {
        return this.uid().innerText()
    }

    fun LcaLangParser.IndicatorRefContext.innerText(): String {
        return this.uid().innerText()
    }

    fun LcaLangParser.ProductRefContext.innerText(): String {
        return this.uid().innerText()
    }

    fun LcaLangParser.ProcessDefinitionContext.buildUniqueKey(): ProcessKey {
        val labels = this.labels()?.flatMap { it.label_assignment() }?.associate { it.labelRef().innerText() to it.STRING_LITERAL().innerText() }
            ?: return ProcessKey(this.name.innerText())
        return ProcessKey(this.name.innerText(), labels)
    }

    fun LcaLangParser.SubstanceDefinitionContext.buildUniqueKey(): SubstanceKey {
        val name = this.substanceRef().innerText()
        val compartment = this.compartmentField().STRING_LITERAL().innerText()
        val type = SubstanceType.of(this.typeField().children[2].text)
        val subCompartment = this.subCompartmentField()?.STRING_LITERAL()?.innerText()
        return SubstanceKey(name, type, compartment, subCompartment)
    }

    fun dataSourceDefinition(ctx: LcaLangParser.DataSourceDefinitionContext): EDataSource<Q> {
        val name = ctx.dataSourceRef().uid().ID().innerText()
        val location = ctx.locationField().firstOrNull()?.STRING_LITERAL()?.innerText()
        val schemaBlock = ctx.schema().firstOrNull() ?: throw LoaderException("missing schema in datasource $name")
        val schema = schemaBlock.columnDefinition().associate { column ->
            val key = column.columnRef().innerText()
            val value = dataExpression(column.dataExpression())
            key to value
        }
        val options = ctx.block_meta().flatMap { it.meta_assignment() }
            .associate { assignment ->
                val key = assignment.STRING_LITERAL(0).innerText()
                val value = assignment.STRING_LITERAL(1).innerText()
                key to value
            }
        return EDataSource(
            config = DataSourceConfig.completeWithDefaults(
                DataSourceConfig(
                    name = name,
                    location = location,
                    options = options,
                )
            ),
            schema = schema,
        )
    }
}
