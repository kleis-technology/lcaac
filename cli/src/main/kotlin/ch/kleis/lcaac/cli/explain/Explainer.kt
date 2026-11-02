package ch.kleis.lcaac.cli.explain

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorKeys
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import kotlin.math.abs
import kotlin.math.max

private typealias N = BasicNumber

private const val MAX_LINE_LENGTH = 110
private const val SUPERSCRIPTS = "⁰¹²³⁴⁵⁶⁷⁸⁹⁻"

/**
 * Builds a human-readable audit of a process evaluation:
 * - for each process, how its parameters, variables and exchange amounts are derived,
 *   down to data source records;
 * - for each product, how its impact per unit is obtained from its exchanges,
 *   checked against the result of the matrix solver.
 */
class Explainer(
    private val symbolTable: SymbolTable<N>,
    private val sourceOps: DataSourceOperations<N>,
    private val config: LcaacConfig = LcaacConfig(),
    private val fmt: NumberFormat = NumberFormat(),
    private val targetNames: Set<String> = emptySet(),
    private val maxDepth: Int? = null,
    private val maxRows: Int = 20,
) {
    private val ops = BasicOperations
    private val globalScope = Scope.global(symbolTable, sourceOps)
    private val plain = ExpressionPrinter(fmt)

    fun explain(template: EProcessTemplate<N>, arguments: Map<String, DataExpression<N>>): String {
        val trace = Evaluator(symbolTable, ops, sourceOps).trace(template, arguments)
        val system = trace.getSystemValue()
        val entryPoint = trace.getEntryPoint()
        val analysis = ContributionAnalysisProgram(system, entryPoint).run()
        val targets = analysis.getControllablePorts().getElements()
            .filter { targetNames.isEmpty() || targetNames.contains(it.getShortName()) }
            .sortedBy { it.getShortName() }
        return Report(system, entryPoint, analysis, targets).render()
    }

    private inner class Report(
        private val system: SystemValue<N>,
        private val entryPoint: ProcessValue<N>,
        private val analysis: ContributionAnalysis<N, BasicMatrix>,
        private val targets: List<MatrixColumnIndex<N>>,
    ) {
        private val out = StringBuilder()
        private val suppliers = system.productToProcessMap
        private val numbering = LinkedHashMap<ProcessValue<N>, Int>()
        private val usedBy = HashMap<ProcessValue<N>, MutableSet<Int>>()
        private val usedGlobals = ArrayList<String>()

        fun render(): String {
            numberProcesses(entryPoint, 0)
            numbering.forEach { (process, n) ->
                process.inputs.mapNotNull { suppliers[it.product] }
                    .forEach { usedBy.getOrPut(it) { sortedSetOf() }.add(n) }
            }
            renderSummary()
            numbering.forEach { (process, n) -> renderProcess(process, n) }
            renderSubstances()
            renderGlobals()
            return out.toString()
        }

        private fun numberProcesses(process: ProcessValue<N>, depth: Int) {
            if (numbering.containsKey(process)) return
            if (maxDepth != null && depth > maxDepth) return
            numbering[process] = numbering.size + 1
            process.inputs
                .mapNotNull { suppliers[it.product] }
                .distinct()
                .forEach { numberProcesses(it, depth + 1) }
        }

        /*
            Summary
         */

        private fun renderSummary() {
            heading("RESULT")
            if (targets.isEmpty()) {
                line(2, "(no impact to explain: the system has no indicator, substance or product end point" +
                    (if (targetNames.isNotEmpty()) " matching ${targetNames.sorted()}" else "") + ")")
            }
            entryPoint.products.forEach { exchange ->
                val product = exchange.product
                line(2, "${fmt.quantity(exchange.quantity.amount.value, exchange.quantity.unit)} ${product.name}" +
                    " (reference product of [1] ${entryPoint.name}):")
                val amount = toReferenceUnit(exchange.quantity, product)
                val rows = targets.map { target ->
                    val perUnit = factor(product, target)
                    listOf(
                        target.getShortName(),
                        "= ${fmt.quantity(amount * perUnit, target.referenceUnit())}",
                        "(${fmt.format(perUnit)} ${perUnitSymbol(target, product)})",
                    )
                }
                table(4, rows)
            }
            out.appendLine()
            heading("HOW TO READ THIS")
            listOf(
                "Each process section shows its parameters and variables, then its exchanges,",
                "each derived step by step: formula, formula with values, value. Data source",
                "lookups and sums show the records they used.",
                "",
                "The contribution table of a process computes the impact per unit of its product:",
                "  sum over exchanges of (amount x impact per unit of that exchange) / product amount.",
                "The impact per unit of an input product comes from the process that supplies it,",
                "shown as [n]. Each per-unit result is checked against the matrix solver.",
            ).forEach { line(2, it) }
        }

        /*
            Process
         */

        private fun renderProcess(process: ProcessValue<N>, n: Int) {
            out.appendLine()
            out.appendLine("=".repeat(80))
            val labels = if (process.labels.isEmpty()) ""
            else process.labels.entries.joinToString(", ", " (", ")") { "${it.key} = \"${it.value.s}\"" }
            out.appendLine("[$n] ${process.name}$labels")
            line(2, if (process == entryPoint) "entry point"
            else "used by: ${usedBy[process]?.joinToString(", ") { "[$it]" } ?: "-"}")
            out.appendLine("=".repeat(80))

            val template = symbolTable.getTemplate(process.name, process.labels.mapValues { it.value.s })
            if (template == null) {
                line(2, "(process template not found: derivations not available)")
            } else {
                renderDerivations(process, template)
            }
            renderContributions(process)
        }

        private fun renderDerivations(process: ProcessValue<N>, template: EProcessTemplate<N>) {
            val arguments = process.products.firstOrNull()?.product?.fromProcessRef?.arguments
                ?.mapValues { EMapper.toDataExpression(it.value) }
                ?: template.params
            val scope = try {
                globalScope
                    .push(arguments, Origin.PARAMETER)
                    .push(template.locals, Origin.VARIABLE)
                    .push(template.body.labels, Origin.LABEL)
            } catch (e: Exception) {
                line(2, "(cannot rebuild the process scope: ${e.message})")
                return
            }

            if (template.params.isNotEmpty()) {
                section("parameters")
                template.params.forEach { (name, default) ->
                    val value = arguments[name]?.let { valueOf(it, globalScope) } ?: "<missing>"
                    val defaultValue = valueOf(default, globalScope)
                    val defaultFormula = plain.print(default)
                    val origin = when {
                        value == defaultValue && defaultFormula == defaultValue -> "(default)"
                        value == defaultValue -> "(default: $defaultFormula)"
                        defaultFormula == defaultValue -> "(set by caller; default: $defaultValue)"
                        else -> "(set by caller; default: $defaultFormula = $defaultValue)"
                    }
                    val simplified = arguments[name]?.let { simplifiedValueOf(it, globalScope) }?.let { " (= $it)" } ?: ""
                    line(4, "$name = $value$simplified   $origin")
                    trackGlobals(default, globalScope)
                }
            }

            if (template.body.labels.isNotEmpty()) {
                section("labels")
                template.body.labels.forEach { (name, value) -> line(4, "$name = \"${value.value}\"") }
            }

            if (template.locals.isNotEmpty()) {
                section("variables")
                orderByDependencies(template.locals).forEach {
                    renderDefinition(it, template.locals.getValue(it), scope, 4)
                }
            }

            if (template.annotations.contains(ProcessAnnotation.CACHED)) {
                section("exchanges")
                line(4, "@cached process: its supply chain is solved separately and appears here")
                line(4, "as aggregated exchanges. Run explain on '${process.name}' itself to see inside.")
                return
            }

            val body = template.body
            section("products")
            body.products.forEach { exchange ->
                val allocation = exchange.allocation?.let { " allocate ${plain.print(it)}" } ?: ""
                line(4, "${plain.print(exchange.quantity)} ${exchange.product.name}$allocation")
                renderAmount("amount", exchange.quantity, scope, 6)
                exchange.allocation?.let { renderAmount("allocate", it, scope, 6) }
            }

            if (body.inputs.isNotEmpty()) {
                section("inputs")
                var index = 0
                renderBlocks(body.inputs, scope, 4) { exchange, entryScope, indent ->
                    renderInput(process, index++, exchange, entryScope, indent)
                }
            }

            if (body.biosphere.isNotEmpty()) {
                section("biosphere")
                renderBlocks(body.biosphere, scope, 4) { exchange, entryScope, indent ->
                    val spec = exchange.substance
                    val compartments = listOfNotNull(spec.compartment, spec.subCompartment)
                    val where = if (compartments.isEmpty()) "" else compartments.joinToString(", ", "(", ")")
                    val type = spec.type?.let { " [$it]" } ?: ""
                    line(indent, "${plain.print(exchange.quantity)} ${spec.name}$where$type")
                    renderAmount("amount", exchange.quantity, entryScope, indent + 2)
                }
            }

            if (body.impacts.isNotEmpty()) {
                section("impacts")
                renderBlocks(body.impacts, scope, 4) { exchange, entryScope, indent ->
                    line(indent, "${plain.print(exchange.quantity)} ${exchange.indicator.name}")
                    renderAmount("amount", exchange.quantity, entryScope, indent + 2)
                }
            }
        }

        private fun renderInput(
            process: ProcessValue<N>,
            index: Int,
            exchange: ETechnoExchange<N>,
            scope: Scope,
            indent: Int,
        ) {
            val from = exchange.product.fromProcess
            val fromText = from?.let { ref ->
                val args = if (ref.arguments.isEmpty()) ""
                else ref.arguments.entries.joinToString(", ", "(", ")") { "${it.key} = ${plain.print(it.value)}" }
                val labels = if (ref.matchLabels.elements.isEmpty()) ""
                else ref.matchLabels.elements.entries
                    .joinToString(", ", " match (", ")") { "${it.key} = ${plain.print(it.value)}" }
                " from ${ref.name}$args$labels"
            } ?: ""
            line(indent, "${plain.print(exchange.quantity)} ${exchange.product.name}$fromText")
            renderAmount("amount", exchange.quantity, scope, indent + 2)
            from?.matchLabels?.elements?.forEach { (name, value) -> renderAmount(name, value, scope, indent + 2) }
            from?.arguments?.forEach { (name, value) -> renderAmount(name, value, scope, indent + 2) }

            val evaluated = process.inputs.getOrNull(index)
                ?.takeIf { it.product.name == exchange.product.name }
            val supplier = evaluated?.let { suppliers[it.product] }
            line(indent + 2, when {
                evaluated == null -> "supplier: unknown"
                supplier == null -> "supplier: none (end point of the system)"
                numbering[supplier] != null -> "supplier: [${numbering[supplier]}] ${supplier.name}"
                else -> "supplier: ${supplier.name} (not expanded, beyond --depth)"
            })
        }

        private fun <E> renderBlocks(
            blocks: List<BlockExpression<E, N>>,
            scope: Scope,
            indent: Int,
            renderEntry: (E, Scope, Int) -> Unit,
        ) {
            blocks.forEach { block ->
                when (block) {
                    is EBlockEntry -> renderEntry(block.entry, scope, indent)
                    is EBlockForEach -> renderForEach(block, scope, indent, renderEntry)
                }
            }
        }

        private fun <E> renderForEach(
            block: EBlockForEach<E, N>,
            scope: Scope,
            indent: Int,
            renderEntry: (E, Scope, Int) -> Unit,
        ) {
            val formula = plain.printDataSource(block.dataSource)
            line(indent, "for_each ${block.rowRef} from $formula")
            val records = try {
                val dataSource = scope.reducer.reduceDataSource(block.dataSource)
                val records = sourceOps.getAll(scope.reducer.evalDataSource(block.dataSource))
                    .map { scope.reducer.reduce(it) }
                    .toList()
                line(indent + 2, "source: ${describe(dataSource)}: ${records.size} row(s)")
                records
            } catch (e: Exception) {
                line(indent + 2, "<error: ${e.message}>")
                return
            }
            records.forEachIndexed { i, record ->
                renderValue(indent + 2, "row #${i + 1}: ${block.rowRef} = ", record)
                val rowScope = try {
                    scope.push(mapOf(block.rowRef to record), Origin.ROW).push(block.locals, Origin.VARIABLE)
                } catch (e: Exception) {
                    line(indent + 4, "<error: ${e.message}>")
                    return@forEachIndexed
                }
                orderByDependencies(block.locals).forEach {
                    renderDefinition(it, block.locals.getValue(it), rowScope, indent + 4)
                }
                renderBlocks(block.body, rowScope, indent + 4, renderEntry)
            }
        }

        /*
            Contributions
         */

        private fun renderContributions(process: ProcessValue<N>) {
            if (targets.isEmpty()) return
            val totalAllocation = process.products.sumOf { allocationOf(it) }
            process.products.forEach { productExchange ->
                val product = productExchange.product
                val allocated = analysis.findOwnerOf(product) ?: return@forEach
                val productAmount = toReferenceUnit(productExchange.quantity, product)
                val productText = "${fmt.quantity(productExchange.quantity.amount.value, productExchange.quantity.unit)} ${product.name}"
                section("contributions, for $productText")
                if (process.products.size > 1) {
                    val share = allocationOf(productExchange) / totalAllocation
                    line(4, "allocated share: ${fmt.format(share * 100)}% (amounts below are already multiplied by it)")
                }
                val exchanges: List<ExchangeValue<N>> = allocated.inputs + allocated.biosphere + allocated.impacts
                val unaffected = targets.filterNot { target ->
                    renderContributionTable(target, product, productAmount, exchanges)
                }
                if (unaffected.isNotEmpty()) {
                    line(4, "no contribution to: ${unaffected.joinToString(", ") { it.getShortName() }}" +
                        "   [ok: matches solver]")
                }
            }
        }

        /** Returns false, without printing anything, when nothing contributes to [target]. */
        private fun renderContributionTable(
            target: MatrixColumnIndex<N>,
            product: ProductValue<N>,
            productAmount: Double,
            exchanges: List<ExchangeValue<N>>,
        ): Boolean {
            val targetUnit = target.referenceUnit()
            val rows = exchanges.map { exchange ->
                val port = exchange.port()
                val amount = toReferenceUnit(exchange.quantity(), port)
                val factor = factor(port, target)
                Triple(exchange, factor, amount * factor)
            }
            val shown = rows.filter { it.third != 0.0 }
            val total = rows.sumOf { it.third }
            val perUnit = total / productAmount
            val solver = factor(product, target)

            if (shown.isEmpty() && checkMark(perUnit, solver).contains("[ok")) return false
            line(4, "${target.getShortName()}:")
            if (shown.isEmpty()) {
                line(6, "no exchange contributes" + checkMark(perUnit, solver))
                return true
            }
            val tableRows = shown.map { (exchange, factor, contribution) ->
                val port = exchange.port()
                val quantity = exchange.quantity()
                val converted = if (quantity.unit.symbol.toString() != port.referenceUnit().symbol.toString())
                    " (= ${fmt.quantity(toReferenceUnit(quantity, port), port.referenceUnit())})" else ""
                listOf(
                    exchangeLabel(exchange),
                    fmt.quantity(quantity.amount.value, quantity.unit) + converted,
                    if (port == target) "x 1" else "x ${fmt.format(factor)} ${perUnitSymbol(target, port)}",
                    "= ${fmt.quantity(contribution, targetUnit)}",
                )
            }
            val productUnit = product.referenceUnit()
            val tail = listOf(
                listOf("total", "", "", "= ${fmt.quantity(total, targetUnit)}"),
                listOf(
                    "per ${productUnit.symbol} ${product.name}",
                    "/ ${fmt.quantity(productAmount, productUnit)}",
                    "",
                    "= ${fmt.format(perUnit)} ${perUnitSymbol(target, product)}" + checkMark(perUnit, solver),
                ),
            )
            table(6, tableRows + tail)
            val hidden = rows.size - shown.size
            if (hidden > 0) line(6, "($hidden exchange(s) with no contribution not shown)")
            return true
        }

        private fun exchangeLabel(exchange: ExchangeValue<N>): String = when (exchange) {
            is TechnoExchangeValue -> {
                val supplier = suppliers[exchange.product]
                val n = supplier?.let { numbering[it] }
                when {
                    n != null -> "${exchange.product.name} [$n]"
                    supplier != null -> "${exchange.product.name} (${supplier.name}, not expanded)"
                    else -> "${exchange.product.name} (end point)"
                }
            }
            is BioExchangeValue -> exchange.substance.getDisplayName()
            is ImpactValue -> exchange.indicator.name
        }

        private fun checkMark(value: Double, solver: Double): String {
            val tolerance = 1e-6 * max(abs(value), abs(solver)) + 1e-12
            return if (abs(value - solver) <= tolerance) "   [ok: matches solver]"
            else "   [MISMATCH: solver gives ${fmt.format(solver)}]"
        }

        /*
            Substances and globals
         */

        private fun renderSubstances() {
            if (system.substanceCharacterizations.isEmpty()) return
            out.appendLine()
            heading("SUBSTANCE CHARACTERIZATIONS")
            system.substanceCharacterizations
                .sortedBy { it.referenceExchange.substance.getDisplayName() }
                .forEach { sc ->
                    val reference = sc.referenceExchange
                    line(2, "${fmt.quantity(reference.quantity.amount.value, reference.quantity.unit)} " +
                        reference.substance.getDisplayName())
                    sc.impacts.forEach { impact ->
                        line(4, "-> ${fmt.quantity(impact.quantity.amount.value, impact.quantity.unit)} ${impact.indicator.name}")
                    }
                }
        }

        private fun renderGlobals() {
            if (usedGlobals.isEmpty()) return
            out.appendLine()
            heading("GLOBAL PARAMETERS AND VARIABLES USED")
            var i = 0
            while (i < usedGlobals.size) {
                val name = usedGlobals[i++]
                val key = DataKey(name)
                val (expression, kind) = symbolTable.globalParameters[key]?.let { it to "global parameter" }
                    ?: symbolTable.globalVariables[key]?.let { it to "global variable" }
                    ?: continue
                line(2, "($kind)")
                renderDefinition(name, expression, globalScope, 2)
            }
        }

        /*
            Definitions
         */

        private fun renderAmount(name: String, expression: DataExpression<N>, scope: Scope, indent: Int) {
            val hasValues = referencesOf(expression).any { scope.originOf(it).let { o -> o != null && o != Origin.UNIT } }
            if (!hasValues && dataSourcePrimitivesOf(expression).isEmpty()) return
            renderDefinition(name, expression, scope, indent)
        }

        private fun renderDefinition(name: String, expression: DataExpression<N>, scope: Scope, indent: Int) {
            trackGlobals(expression, scope)
            val formula = plain.print(expression)
            val substituted = substituting(scope).print(expression)
            val value = valueOf(expression, scope)
            val steps = mutableListOf(formula)
            if (substituted != formula) steps.add(substituted)
            if (value != substituted && value != formula) steps.add(value)
            val simplified = simplifiedValueOf(expression, scope)?.let { "  (= $it)" } ?: ""
            val pad = " ".repeat(name.length + 1)

            val oneLine = "$name = ${steps.joinToString(" = ")}$simplified"
            if (indent + oneLine.length <= MAX_LINE_LENGTH) {
                line(indent, oneLine)
            } else {
                val record = reduceOrNull(expression, scope) as? ERecord
                steps.forEachIndexed { i, step ->
                    val prefix = if (i == 0) "$name = " else "$pad= "
                    if (i == steps.lastIndex && step == value && record != null) {
                        renderValue(indent, prefix, record)
                    } else {
                        line(indent, prefix + step + if (i == steps.lastIndex) simplified else "")
                    }
                }
            }
            dataSourcePrimitivesOf(expression).forEach { primitive ->
                describePrimitive(primitive, scope).forEach { line(indent + pad.length + 2, it) }
            }
        }

        /** Prints `prefix + value`, with one entry per line when a record is too wide for one line. */
        private fun renderValue(indent: Int, prefix: String, value: DataExpression<N>) {
            val text = valueText(value)
            if (value !is ERecord || indent + prefix.length + text.length <= MAX_LINE_LENGTH) {
                line(indent, prefix + text)
                return
            }
            line(indent, "$prefix{")
            value.entries.forEach { (key, entry) -> line(indent + prefix.length + 2, "$key: ${valueText(entry)}") }
            line(indent + prefix.length, "}")
        }

        private fun trackGlobals(expression: DataExpression<N>, scope: Scope) {
            referencesOf(expression)
                .filter { scope.originOf(it)?.isGlobal() == true }
                .filter { !usedGlobals.contains(it) }
                .forEach { usedGlobals.add(it) }
        }

        private fun describePrimitive(primitive: DataExpression<N>, scope: Scope): List<String> = try {
            when (primitive) {
                is EFirstRecordOf -> listOf("source: ${describe(scope.reducer.reduceDataSource(primitive.dataSource))}")
                is EDefaultRecordOf -> {
                    val dataSource = scope.reducer.reduceDataSource(primitive.dataSource)
                    listOf("source: default values of the schema of ${dataSource.config.name}")
                }
                is ESumProduct -> describeSum(primitive, scope)
                else -> emptyList()
            }
        } catch (e: Exception) {
            listOf("source: <error: ${e.message}>")
        }

        private fun describeSum(primitive: ESumProduct<N>, scope: Scope): List<String> {
            val dataSource = scope.reducer.reduceDataSource(primitive.dataSource)
            val records = sourceOps.getAll(scope.reducer.evalDataSource(primitive.dataSource)).toList()
            val primaryKey = dataSource.config.primaryKey
            val lines = mutableListOf("source: ${describe(dataSource)}: ${records.size} row(s) summed")
            records.take(maxRows).forEachIndexed { i, record ->
                // Identify the row by its primary key, or else by its text columns not already in the filter
                val key = primaryKey?.let { record.entries[it] }
                    ?.let { "$primaryKey = ${valueOf(it, scope)}" }
                    ?: record.entries
                        .filter { it.value is EStringLiteral && !dataSource.filter.containsKey(it.key) }
                        .entries.joinToString(", ") { "${it.key} = ${valueText(it.value)}" }
                        .ifEmpty { "row #${i + 1}" }
                val columns = primitive.columns.map { record.entries[it] ?: EStringLiteral("<missing $it>") }
                val factors = columns.joinToString(" x ") { valueOf(it, scope) }
                val product = if (columns.size > 1) " = ${valueOf(columns.reduce { a, b -> EQuantityMul(a, b) }, scope)}" else ""
                lines.add("  $key: $factors$product")
            }
            if (records.size > maxRows) lines.add("  ... ${records.size - maxRows} more row(s), see --max-rows")
            return lines
        }

        private fun describe(dataSource: EDataSource<N>): String {
            val dataSourceConfig = dataSource.config
            val connector = dataSourceConfig.connector ?: CsvConnectorKeys.CSV_CONNECTOR_NAME
            val directory = config.getConnector(connector)?.options?.get(CsvConnectorKeys.CSV_CONNECTOR_KEY_DIRECTORY)
            val location = listOfNotNull(directory, dataSourceConfig.location ?: "${dataSourceConfig.name}.csv")
                .joinToString("/")
            val where = if (connector == CsvConnectorKeys.CSV_CONNECTOR_NAME) location else "$connector: $location"
            val filter = if (dataSource.filter.isEmpty()) ""
            else dataSource.filter.entries.joinToString(", ", " where ") { "${it.key} = ${valueText(it.value)}" }
            return "${dataSourceConfig.name} ($where)$filter"
        }

        /** Impact of one reference unit of [port] on [target], as computed by the matrix solver. */
        private fun factor(port: MatrixColumnIndex<N>, target: MatrixColumnIndex<N>): Double = try {
            analysis.getImpactFactors().characterizationFactor(port, target).amount.value
        } catch (e: Exception) {
            Double.NaN
        }

        /*
            Output
         */

        private fun line(indent: Int, text: String) {
            out.appendLine(" ".repeat(indent) + text)
        }

        private fun section(title: String) {
            out.appendLine()
            out.appendLine("  $title:")
        }

        private fun heading(title: String) {
            out.appendLine(title)
        }

        private fun table(indent: Int, rows: List<List<String>>) {
            if (rows.isEmpty()) return
            val widths = rows.first().indices.map { col -> rows.maxOf { it.getOrElse(col) { "" }.length } }
            rows.forEach { row ->
                line(indent, row.mapIndexed { i, cell -> cell.padEnd(widths[i]) }.joinToString("  ").trimEnd())
            }
        }
    }

    /*
        Helpers
     */

    private fun substituting(scope: Scope) = ExpressionPrinter(fmt) { expression ->
        when {
            expression is EDataRef && isValue(scope, expression.name) -> valueOf(expression, scope)
            expression is ERecordEntry && expression.record.let { it is EDataRef && isValue(scope, it.name) } ->
                valueOf(expression, scope)
            else -> null
        }
    }

    private fun isValue(scope: Scope, name: String): Boolean =
        scope.originOf(name).let { it != null && it != Origin.UNIT }

    private fun valueOf(expression: DataExpression<N>, scope: Scope): String = try {
        valueText(scope.reducer.reduce(expression))
    } catch (e: Exception) {
        "<error: ${e.message}>"
    }

    private fun reduceOrNull(expression: DataExpression<N>, scope: Scope): DataExpression<N>? = try {
        scope.reducer.reduce(expression)
    } catch (e: Exception) {
        null
    }

    /**
     * Quantities computed from several units get composite units such as `kWh.u².percent⁻¹`
     * or `hour.GiB.u⁻²`. This gives the value again without the dimensionless factors
     * (`8.25 kWh`, `768 hour.GiB`), which is easier to check by hand.
     */
    private fun simplifiedValueOf(expression: DataExpression<N>, scope: Scope): String? {
        val reduced = reduceOrNull(expression, scope) as? EQuantityScale ?: return null
        val unit = reduced.base as? EUnitLiteral ?: return null
        val symbol = unit.symbol.toString()
        if (!symbol.contains('.') && !symbol.contains('⁻')) return null

        val components = symbol.split('.').map { component ->
            val name = component.trimEnd { it in SUPERSCRIPTS }
            val exponent = component.substring(name.length)
                .map { SUPERSCRIPTS.indexOf(it).let { i -> if (i == 10) '-' else '0' + i } }
                .joinToString("").ifEmpty { "1" }
                .toDoubleOrNull() ?: return null
            val literal = unitsBySymbol[name] ?: return null
            literal to exponent
        }
        val dimensional = components.filter { it.first.dimension != Dimension.None }
        fun unitValue(literal: EUnitLiteral<N>) = UnitValue<N>(literal.symbol, literal.scale, literal.dimension)
        val simple = if (dimensional.isEmpty()) unitsBySymbol["u"]?.let { unitValue(it) } ?: return null
        else dimensional.map { (literal, exponent) -> unitValue(literal).pow(exponent) }.reduce { a, b -> a.times(b) }
        if (simple.dimension != unit.dimension || simple.symbol.toString() == symbol) return null
        return "${fmt.format(reduced.scale.value * unit.scale / simple.scale)} ${simple.symbol}"
    }

    /** All units known to the model (prelude and user-defined), by symbol. */
    private val unitsBySymbol: Map<String, EUnitLiteral<N>> by lazy {
        symbolTable.globalVariables.toMap()
            .filter { it.value is EUnitLiteral || it.value is EUnitAlias }
            .mapNotNull { (key, _) ->
                (reduceOrNull(EDataRef(key.name), globalScope) as? EQuantityScale)?.let { q ->
                    (q.base as? EUnitLiteral)?.let { it.copy(scale = it.scale * q.scale.value) }
                }
            }
            .associateBy { it.symbol.toString() }
    }

    private fun valueText(reduced: DataExpression<N>): String = when (reduced) {
        is EQuantityScale -> (reduced.base as? EUnitLiteral)
            ?.let { "${fmt.format(reduced.scale.value)} ${it.symbol}".trim() }
            ?: plain.print(reduced)
        is EStringLiteral -> "\"${reduced.value}\""
        is ERecord -> reduced.entries.entries.joinToString(", ", "{", "}") { "${it.key}: ${valueText(it.value)}" }
        else -> plain.print(reduced)
    }

    private fun orderByDependencies(definitions: Map<String, DataExpression<N>>): List<String> {
        val result = LinkedHashSet<String>()
        val visiting = HashSet<String>()
        fun visit(name: String) {
            if (result.contains(name) || !visiting.add(name)) return
            referencesOf(definitions.getValue(name))
                .filter { definitions.containsKey(it) }
                .forEach { visit(it) }
            result.add(name)
        }
        definitions.keys.forEach { visit(it) }
        return result.toList()
    }

    private fun toReferenceUnit(quantity: QuantityValue<N>, port: MatrixColumnIndex<N>): Double =
        quantity.amount.value * quantity.unit.scale / port.referenceUnit().scale

    private fun allocationOf(exchange: TechnoExchangeValue<N>): Double =
        exchange.allocation?.let { it.amount.value * it.unit.scale } ?: 1.0

    private fun perUnitSymbol(target: MatrixColumnIndex<N>, port: MatrixColumnIndex<N>): String =
        "${target.referenceUnit().symbol}/${port.referenceUnit().symbol}"

}
