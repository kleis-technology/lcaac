package ch.kleis.lcaac.cli.explain

import ch.kleis.lcaac.core.explain.*
import ch.kleis.lcaac.core.lang.dimension.UnitSimplifier
import ch.kleis.lcaac.core.lang.evaluator.derivation.*
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.ProcessKey
import ch.kleis.lcaac.core.lang.source.BlockPath
import ch.kleis.lcaac.core.lang.source.Section
import ch.kleis.lcaac.core.lang.source.SourceLocation
import ch.kleis.lcaac.core.lang.source.SourceMap
import ch.kleis.lcaac.core.lang.source.SourcePath
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.DataExpressionPrinter

private typealias N = BasicNumber

private const val MAX_LINE_LENGTH = 110

/**
 * Renders an [Explanation] as text: the results, then each process with how its parameters, variables
 * and exchanges were evaluated, and the contribution tables of its products.
 */
class ExplanationRenderer(
    private val sourceMap: SourceMap,
    private val units: UnitSimplifier<N>,
    private val numbers: NumberFormat = NumberFormat(),
    /** The maximum number of rows listed for a data source read. */
    private val maxRows: Int = 20,
) {
    private val ops = BasicOperations
    private val plain = DataExpressionPrinter(ops, numbers::format)

    fun render(explanation: Explanation<N>): String = Output().apply {
        renderResults(explanation)
        explanation.processes.forEach { renderProcess(it) }
        renderSubstances(explanation)
        renderGlobals(explanation)
    }.toString()

    private inner class Output {
        private val out = StringBuilder()

        fun line(indent: Int, text: String) {
            out.appendLine((" ".repeat(indent) + text).trimEnd())
        }

        fun blank() {
            out.appendLine()
        }

        /** Lines of aligned columns. */
        fun table(indent: Int, rows: List<List<String>>) {
            if (rows.isEmpty()) return
            val widths = rows.first().indices.map { column -> rows.maxOf { it.getOrElse(column) { "" }.length } }
            rows.forEach { row -> line(indent, row.mapIndexed { i, cell -> cell.padEnd(widths[i]) }.joinToString("  ")) }
        }

        override fun toString(): String = out.toString()

        /*
            Results
         */

        fun renderResults(explanation: Explanation<N>) {
            line(0, "RESULT")
            if (explanation.targets.isEmpty()) line(2, "(no indicator, substance or product to explain)")
            explanation.results.forEach { result ->
                line(2, "${quantity(result.product.quantity)} ${result.product.product.name}, " +
                    "reference product of [1] ${explanation.entryPoint.process.name}:")
                table(4, result.impacts.map { impact ->
                    listOf(
                        impact.target.getShortName(),
                        amount(impact.amount, impact.target.referenceUnit()),
                        "(${perUnit(impact.perUnit, impact.target, result.product.product)})",
                    )
                })
            }
        }

        /*
            Process
         */

        fun renderProcess(explanation: ProcessExplanation<N>) {
            val process = explanation.process
            val source = sourceMap[SourcePath.Process(processKeyOf(process))]
            blank()
            line(0, "=".repeat(80))
            line(0, listOfNotNull("[${explanation.number}] ${processName(process)}", source?.toString()).joinToString("   "))
            val usedBy = explanation.usedBy.joinToString(", ") { "[$it]" }
            line(2, if (explanation.number == 1) listOfNotNull("entry point", usedBy.ifEmpty { null }?.let { "used by: $it" }).joinToString(", ")
                else "used by: $usedBy")
            if (explanation.loop.isNotEmpty()) {
                val others = explanation.loop.filter { it != explanation.number }
                line(2, if (others.isEmpty()) "loop: uses its own product; its impacts per unit solve its table below"
                    else "loop with ${others.joinToString(", ") { "[$it]" }}: their impacts per unit solve their tables together")
            }
            line(0, "=".repeat(80))

            val derivation = explanation.derivation
            if (derivation == null) {
                blank()
                line(2, "@cached: evaluated as a separate system, its supply chain is aggregated in its exchanges.")
                line(2, "Run explain on it to see inside.")
                renderAggregatedExchanges(explanation)
            } else {
                Body(explanation, derivation, source).render()
            }
            explanation.contributions.forEach { renderContributions(explanation, it) }
        }

        private fun renderAggregatedExchanges(explanation: ProcessExplanation<N>) {
            val process = explanation.process
            if (process.inputs.isNotEmpty()) {
                blank()
                line(2, "inputs:")
                process.inputs.forEachIndexed { i, input ->
                    line(4, "${quantity(input.quantity)} ${input.product.name}   ${supplier(explanation.suppliers[i])}")
                }
            }
            if (process.biosphere.isNotEmpty()) {
                blank()
                line(2, "biosphere:")
                process.biosphere.forEach { line(4, "${quantity(it.quantity)} ${it.substance.getDisplayName()}") }
            }
        }

        /** The parameters, variables and exchanges of a process, as written and as evaluated. */
        private inner class Body(
            private val explanation: ProcessExplanation<N>,
            private val derivation: ProcessDerivation<N>,
            private val source: SourceLocation?,
        ) {
            private val owner = derivation.process
            private val body = derivation.template.body

            fun render() {
                renderParameters()
                if (body.labels.isNotEmpty()) {
                    blank()
                    line(2, "labels:")
                    body.labels.forEach { (name, value) -> line(4, "$name = \"${value.value}\"") }
                }
                renderVariables()
                renderProducts()
                renderSection("inputs", Section.INPUTS, body.inputs)
                renderSection("biosphere", Section.BIOSPHERE, body.biosphere)
                renderSection("impacts", Section.IMPACTS, body.impacts)
            }

            private fun renderParameters() {
                if (derivation.parameters.isEmpty()) return
                blank()
                line(2, "parameters:")
                derivation.parameters.forEach { parameter ->
                    val value = parameter.value?.let { valueText(it) } ?: "<could not be evaluated>"
                    val default = parameter.default
                    val origin = when {
                        default == null -> "(set by caller)"
                        parameter.expression == default -> "(default)"
                        else -> "(set by caller; default: ${plain.print(default)})"
                    }
                    line(4, "${parameter.path.name} = $value   $origin${at(parameter.path)}")
                }
            }

            private fun renderVariables() {
                if (derivation.template.locals.isEmpty()) return
                blank()
                line(2, "variables:")
                derivation.template.locals.forEach { (name, expression) ->
                    val path = SourcePath.Variable(owner, name)
                    renderStep(4, name, expression, variable(path, emptyList()), emptyList(), path)
                }
            }

            private fun renderProducts() {
                blank()
                line(2, "products:")
                body.products.forEachIndexed { i, written ->
                    val origin = derivation.origins.products.getOrNull(i)
                    val allocation = written.allocation?.let { " allocate ${plain.print(it)}" } ?: ""
                    val share = explanation.contributions.firstOrNull { it.product.product.name == written.product.name }
                        ?.takeIf { explanation.process.products.size > 1 }
                        ?.let { "   share ${numbers.format(it.share * 100)}%" } ?: ""
                    line(4, "${plain.print(written.quantity)} ${written.product.name}$allocation$share" +
                        (origin?.let { at(it.path) } ?: ""))
                    explanation.process.products.getOrNull(i)?.let { renderAmount(6, written.quantity, it.quantity, emptyList()) }
                }
            }

            private fun <E> renderSection(title: String, section: Section, blocks: List<BlockExpression<E, N>>) {
                if (blocks.isEmpty()) return
                blank()
                line(2, "$title:")
                blocks.forEachIndexed { i, block -> renderBlock(4, block, BlockPath.of(section, i), emptyList()) }
            }

            private fun <E> renderBlock(indent: Int, block: BlockExpression<E, N>, path: BlockPath, rows: List<RowRef>) {
                val exchangePath = SourcePath.Exchange(owner, path)
                when (block) {
                    is EBlockEntry -> renderExchange(indent, block.entry as LcaExchangeExpression<N>, exchangePath, rows)
                    is EBlockForEach -> {
                        line(indent, "for_each ${block.rowRef} from ${plain.print(block.dataSource)}${at(exchangePath)}")
                        val read = derivation.forEach.firstOrNull { it.path == exchangePath && it.rows == rows }?.read
                        if (read == null) {
                            line(indent + 2, "(not evaluated)")
                            return
                        }
                        line(indent + 2, "read ${describe(read.dataSource)}: ${count(read.records.size, "row")}")
                        read.records.forEachIndexed { r, record ->
                            if (r >= maxRows) {
                                if (r == maxRows) line(indent + 2, "... ${count(read.records.size - maxRows, "more row")}, see --max-rows")
                                return@forEachIndexed
                            }
                            val rowRows = rows + RowRef(exchangePath, r)
                            renderValue(indent + 2, "row #${r + 1}: ${block.rowRef} = ", record)
                            block.locals.forEach { (name, expression) ->
                                val variablePath = SourcePath.Variable(owner, name, path)
                                renderStep(indent + 4, name, expression, variable(variablePath, rowRows), rowRows, variablePath)
                            }
                            block.body.forEachIndexed { i, child -> renderBlock(indent + 4, child, path.child(i), rowRows) }
                        }
                    }
                }
            }

            private fun renderExchange(indent: Int, written: LcaExchangeExpression<N>, path: SourcePath.Exchange, rows: List<RowRef>) {
                val origin = ExchangeOrigin(path, rows)
                val (index, evaluated) = evaluated(origin)
                val suffix = when (written) {
                    is ETechnoExchange -> {
                        val from = written.product.fromProcess?.let { ref ->
                            val arguments = if (ref.arguments.isEmpty()) ""
                            else ref.arguments.entries.joinToString(", ", "(", ")") { "${it.key} = ${plain.print(it.value)}" }
                            " from ${ref.name}$arguments"
                        } ?: ""
                        val supplier = index?.let { explanation.suppliers.getOrNull(it) }?.let { "   ${supplier(it)}" } ?: ""
                        "${written.product.name}$from$supplier"
                    }
                    is EBioExchange -> {
                        val compartments = listOfNotNull(written.substance.compartment, written.substance.subCompartment)
                        written.substance.name + if (compartments.isEmpty()) "" else compartments.joinToString(", ", "(", ")")
                    }
                    is EImpact -> written.indicator.name
                }
                line(indent, "${plain.print(written.quantity)} $suffix${if (rows.isEmpty()) at(path) else ""}")
                evaluated?.let { renderAmount(indent + 2, written.quantity, it.quantity(), rows) }
                derivation.exchangeReads[origin]?.forEach { renderRead(indent + 2, it) }
            }

            /** The evaluated exchange of [origin], and its index among the inputs, biosphere or impacts. */
            private fun evaluated(origin: ExchangeOrigin): Pair<Int?, ExchangeValue<N>?> {
                val process = explanation.process
                val (origins, exchanges) = when (origin.path.block.section) {
                    Section.PRODUCTS -> derivation.origins.products to process.products
                    Section.INPUTS -> derivation.origins.inputs to process.inputs
                    Section.BIOSPHERE -> derivation.origins.biosphere to process.biosphere
                    Section.IMPACTS -> derivation.origins.impacts to process.impacts
                }
                val index = origins.indexOf(origin).takeIf { it >= 0 } ?: return null to null
                return index to exchanges.getOrNull(index)
            }

            /** The amount of an exchange, from its formula to its value, unless the formula is the value. */
            private fun renderAmount(indent: Int, formula: DataExpression<N>, value: QuantityValue<N>, rows: List<RowRef>) {
                val written = plain.print(formula)
                val substituted = substituting(rows).print(formula).takeIf { it != written && it != quantity(value) }
                val evaluated = quantityWithSimplified(value).takeIf { it != written }
                val steps = listOfNotNull(substituted, evaluated)
                if (steps.isNotEmpty()) line(indent, "= ${steps.joinToString(" = ")}")
            }

            /** How a variable was evaluated: its formula, the formula with values, then its value. */
            private fun renderStep(
                indent: Int,
                name: String,
                expression: DataExpression<N>,
                variable: VariableDerivation<N>?,
                rows: List<RowRef>,
                path: SourcePath,
            ) {
                val formula = plain.print(expression)
                val steps = listOfNotNull(
                    formula,
                    substituting(rows).print(expression),
                    variable?.value?.let { valueText(it) } ?: "<could not be evaluated>",
                ).distinct()
                val location = at(path)
                val oneLine = "$name = ${steps.joinToString(" = ")}"
                if (indent + oneLine.length + location.length <= MAX_LINE_LENGTH) {
                    line(indent, oneLine + location)
                } else {
                    val pad = " ".repeat(name.length + 1)
                    steps.forEachIndexed { i, step ->
                        val prefix = if (i == 0) "$name = " else "$pad= "
                        val value = variable?.value
                        if (i == steps.lastIndex && value is ERecord) renderValue(indent, prefix, value)
                        else line(indent, prefix + step + if (i == 0) location else "")
                    }
                }
                variable?.reads?.forEach { renderRead(indent + name.length + 3, it) }
            }

            private fun variable(path: SourcePath, rows: List<RowRef>): VariableDerivation<N>? =
                derivation.variables.firstOrNull { it.path == path && it.rows == rows }

            private fun substituting(rows: List<RowRef>) =
                DataExpressionPrinter(ops, numbers::format) { name -> derivation.valueOf(name, rows) }

            /** ` (line 42)` for a definition in the file of the process, or its full location otherwise. */
            private fun at(path: SourcePath): String {
                val location = sourceMap[path] ?: return ""
                return if (source != null && location.source == source.source) "   (line ${location.line})" else "   ($location)"
            }
        }

        private fun renderRead(indent: Int, read: DataSourceRead<N>) {
            when (read.kind) {
                ReadKind.LOOKUP -> line(indent, "from ${describe(read.dataSource)}")
                ReadKind.DEFAULT_RECORD -> line(indent, "default record of ${describe(read.dataSource)}")
                ReadKind.FOR_EACH -> line(indent, "read ${describe(read.dataSource)}: ${count(read.records.size, "row")}")
                ReadKind.SUM -> {
                    line(indent, "sum of ${count(read.records.size, "row")} of ${describe(read.dataSource)}:")
                    val primaryKey = read.dataSource.config.primaryKey
                    read.records.take(maxRows).forEachIndexed { r, record ->
                        // A row is named by its primary key, or else by its text columns that are not in the filter
                        val key = primaryKey?.let { record.entries[it] }?.let { "$primaryKey = ${plain.print(it)}" }
                            ?: record.entries.filter { it.value is EStringLiteral && it.key !in read.dataSource.filter }
                                .entries.joinToString(", ") { "${it.key} = ${plain.print(it.value)}" }
                                .ifEmpty { "row #${r + 1}" }
                        val factors = read.columns.joinToString(" x ") { column ->
                            record.entries[column]?.let { plain.print(it) } ?: "<no $column>"
                        }
                        line(indent + 2, "$key: $factors")
                    }
                    if (read.records.size > maxRows) line(indent + 2, "... ${count(read.records.size - maxRows, "more row")}, see --max-rows")
                }
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

        /*
            Contributions
         */

        private fun renderContributions(explanation: ProcessExplanation<N>, contributions: ProductContributions<N>) {
            val product = contributions.product
            val share = if (explanation.process.products.size > 1) " (share ${numbers.format(contributions.share * 100)}%)" else ""
            blank()
            line(2, "contributions to ${quantity(product.quantity)} ${product.product.name}$share:")
            contributions.tables.forEach { table ->
                line(4, "${table.target.getShortName()}:")
                val targetUnit = table.target.referenceUnit()
                val rows = table.rows.map { row ->
                    val port = row.exchange.port()
                    listOf(
                        rowLabel(explanation, row),
                        amount(row.amount, port.referenceUnit()),
                        if (port == table.target) "x 1" else "x ${perUnit(row.factor, table.target, port)}",
                        "= ${amount(row.contribution, targetUnit)}",
                    )
                }
                val check = if (table.matchesSolver) "[ok: matches solver]"
                else "[MISMATCH: solver gives ${perUnit(table.solverPerUnit, table.target, product.product)}]"
                val productUnit = product.product.referenceUnit()
                table(6, rows + listOf(
                    listOf("total", "", "", "= ${amount(table.total, targetUnit)}"),
                    listOf("per ${unitOf(productUnit)} ${product.product.name}", "/ ${amount(table.productAmount, productUnit)}", "",
                        "= ${perUnit(table.perUnit, table.target, product.product)}   $check"),
                ))
                if (table.hidden > 0) line(6, "(${count(table.hidden, "exchange")} without contribution not shown)")
            }
            if (contributions.unaffected.isNotEmpty()) {
                line(4, "no contribution to: ${contributions.unaffected.joinToString(", ") { it.getShortName() }}")
            }
        }

        private fun rowLabel(explanation: ProcessExplanation<N>, row: ContributionRow<N>): String {
            val origins = explanation.derivation?.origins
            return when (val exchange = row.exchange) {
                is TechnoExchangeValue -> {
                    val supplier = explanation.suppliers.getOrNull(row.index)?.let {
                        when (it) {
                            is Supplier.Explained -> " [${it.number}]"
                            is Supplier.NotExplained -> " (${it.name}, not explained)"
                            Supplier.EndPoint -> " (end point)"
                        }
                    } ?: ""
                    exchange.product.name + supplier + rowsOf(origins?.inputs?.getOrNull(row.index))
                }
                is BioExchangeValue -> exchange.substance.getDisplayName() + rowsOf(origins?.biosphere?.getOrNull(row.index))
                is ImpactValue -> exchange.indicator.name + rowsOf(origins?.impacts?.getOrNull(row.index))
            }
        }

        /** `, row #2` for an exchange of a for_each, so that the rows of the same exchange can be told apart. */
        private fun rowsOf(origin: ExchangeOrigin?): String =
            origin?.rows?.takeIf { it.isNotEmpty() }?.joinToString("") { ", row #${it.row + 1}" } ?: ""

        /*
            Substances and globals
         */

        fun renderSubstances(explanation: Explanation<N>) {
            if (explanation.substanceCharacterizations.isEmpty()) return
            blank()
            line(0, "SUBSTANCES")
            explanation.substanceCharacterizations.forEach { characterization ->
                val reference = characterization.referenceExchange
                line(2, "${quantity(reference.quantity)} ${reference.substance.getDisplayName()}:")
                characterization.impacts.forEach { line(4, "${quantity(it.quantity)} ${it.indicator.name}") }
            }
        }

        fun renderGlobals(explanation: Explanation<N>) {
            if (explanation.globals.isEmpty()) return
            blank()
            line(0, "GLOBALS")
            val values = explanation.globals.associate { (it.path as SourcePath.Global).name to it.value }
            val substituting = DataExpressionPrinter(ops, numbers::format) { values[it] }
            explanation.globals.forEach { global ->
                val name = (global.path as SourcePath.Global).name
                sourceMap.nameOf(global.path)?.let {
                    line(2, it)
                    return@forEach
                }
                val steps = listOf(plain.print(global.expression), substituting.print(global.expression), valueText(global.value)).distinct()
                line(2, "$name = ${steps.joinToString(" = ")}${sourceMap[global.path]?.let { "   ($it)" } ?: ""}")
            }
        }
    }

    /*
        Values and units
     */

    private fun processName(process: ProcessValue<N>): String =
        process.name + if (process.labels.isEmpty()) "" else process.labels.entries.joinToString(", ", "{", "}") { "${it.key}=${it.value.s}" }

    private fun processKeyOf(process: ProcessValue<N>) =
        ProcessKey(process.name, process.labels.mapValues { it.value.s })

    private fun supplier(supplier: Supplier): String = when (supplier) {
        is Supplier.Explained -> "-> [${supplier.number}] ${supplier.name}"
        is Supplier.NotExplained -> "-> ${supplier.name} (not explained, beyond --depth)"
        Supplier.EndPoint -> "-> end point"
    }

    private fun describe(dataSource: DataSourceValue<N>): String {
        val location = dataSource.config.location?.let { " ($it)" } ?: ""
        val filter = if (dataSource.filter.isEmpty()) ""
        else dataSource.filter.entries.joinToString(", ", " where ") { "${it.key} = ${dataValue(it.value)}" }
        return "${dataSource.config.name}$location$filter"
    }

    private fun dataValue(value: DataValue<N>): String = when (value) {
        is StringValue -> "\"${value.s}\""
        is QuantityValue -> quantity(value)
        is RecordValue -> value.entries.entries.joinToString(", ", "{", "}") { "${it.key}: ${dataValue(it.value)}" }
    }

    /** A value, with its simplified form when its unit has dimensionless factors: `15 kg.percent (= 0.15 kg)`. */
    private fun valueText(value: DataExpression<N>): String {
        val text = DataExpressionPrinter(ops, numbers::format).print(value)
        val quantity = quantityOf(value) ?: return text
        return units.simplify(quantity)?.let { "$text (= ${quantity(it)})" } ?: text
    }

    private fun quantityWithSimplified(quantity: QuantityValue<N>): String =
        units.simplify(quantity)?.let { "${quantity(quantity)} (= ${quantity(it)})" } ?: quantity(quantity)

    private fun quantityOf(value: DataExpression<N>): QuantityValue<N>? {
        val scale = value as? EQuantityScale<N> ?: return null
        val unit = scale.base as? EUnitLiteral<N> ?: return null
        return QuantityValue(scale.scale, UnitValue(unit.symbol, unit.scale, unit.dimension))
    }

    private fun quantity(quantity: QuantityValue<N>): String = "${numbers.format(quantity.amount.value)} ${unitOf(quantity.unit)}".trim()

    private fun unitOf(unit: UnitValue<N>): String = unit.symbol.toString()

    /** The unit used to display amounts of [unit], simplified if possible, and the factor to convert to it. */
    private fun display(unit: UnitValue<N>): Pair<UnitValue<N>, Double> {
        val simplified = units.simplify(QuantityValue(BasicNumber(1.0), unit)) ?: return unit to 1.0
        return simplified.unit to simplified.amount.value
    }

    /** An amount in the reference [unit], displayed in a simplified unit when possible. */
    private fun amount(value: Double, unit: UnitValue<N>): String {
        val (displayUnit, factor) = display(unit)
        return "${numbers.format(value * factor)} ${unitOf(displayUnit)}".trim()
    }

    /** An amount of [target] per reference unit of [port], e.g. `0.5 kg CO2-Eq/kWh`. */
    private fun perUnit(value: Double, target: MatrixColumnIndex<N>, port: MatrixColumnIndex<N>): String {
        val (targetUnit, targetFactor) = display(target.referenceUnit())
        val (portUnit, portFactor) = display(port.referenceUnit())
        return "${numbers.format(value * targetFactor / portFactor)} ${unitOf(targetUnit)}/${unitOf(portUnit)}"
    }

    private fun count(n: Int, noun: String): String = "$n $noun${if (n == 1) "" else "s"}"
}
