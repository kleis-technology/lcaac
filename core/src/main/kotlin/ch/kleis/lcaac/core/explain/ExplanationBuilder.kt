package ch.kleis.lcaac.core.explain

import ch.kleis.lcaac.core.allocation.Allocation
import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.graph.stronglyConnectedComponents
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.evaluator.derivation.VariableDerivation
import ch.kleis.lcaac.core.lang.source.SourcePath
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.QuantityOperations

/**
 * Builds the [Explanation] of an evaluation. The trace should record derivations
 * (`EvaluatorOptions(recordDerivations = true)`); otherwise processes are explained without them.
 */
class ExplanationBuilder<Q, M>(
    private val ops: QuantityOperations<Q>,
    private val options: ExplainOptions = ExplainOptions(),
) {
    fun build(trace: EvaluationTrace<Q>, analysis: ContributionAnalysis<Q, M>): Explanation<Q> {
        val system = trace.getSystemValue()
        val entryPoint = trace.getEntryPoint()
        val suppliers = system.productToProcessMap
        val targets = analysis.getControllablePorts().getElements()
            .filter { options.targets.isEmpty() || it.getShortName() in options.targets }
            .sortedBy { it.getShortName() }

        val numbering = number(entryPoint, suppliers)
        val usedBy = HashMap<ProcessValue<Q>, MutableSet<Int>>()
        numbering.forEach { (process, numberAndDepth) ->
            process.inputs.mapNotNull { suppliers[it.product] }
                .forEach { usedBy.getOrPut(it) { sortedSetOf() }.add(numberAndDepth.first) }
        }
        val loops = loops(system.processes, suppliers)

        val processes = numbering.map { (process, numberAndDepth) ->
            val (number, depth) = numberAndDepth
            ProcessExplanation(
                number = number,
                depth = depth,
                process = process,
                derivation = trace.getDerivation(process),
                usedBy = usedBy[process]?.toList() ?: emptyList(),
                loop = loops[process]?.mapNotNull { numbering[it]?.first }?.sorted() ?: emptyList(),
                suppliers = process.inputs.map { input ->
                    when (val supplier = suppliers[input.product]) {
                        null -> Supplier.EndPoint
                        else -> numbering[supplier]
                            ?.let { Supplier.Explained(it.first, supplier.name) }
                            ?: Supplier.NotExplained(supplier.name)
                    }
                },
                contributions = contributions(process, targets, analysis),
            )
        }

        return Explanation(
            entryPoint = processes.first(),
            targets = targets,
            results = entryPoint.products.map { exchange ->
                val amount = referenceAmount(exchange.quantity, exchange.product)
                ProductResult(exchange, targets.map { target ->
                    val perUnit = factor(analysis, exchange.product, target)
                    TargetImpact(target, amount * perUnit, perUnit)
                })
            },
            processes = processes,
            substanceCharacterizations = system.substanceCharacterizations
                .sortedBy { it.referenceExchange.substance.getDisplayName() },
            globals = globals(processes),
        )
    }

    /** The number and depth of each process, from the entry point, breadth first, up to the maximum depth. */
    private fun number(
        entryPoint: ProcessValue<Q>,
        suppliers: Map<ProductValue<Q>, ProcessValue<Q>>,
    ): LinkedHashMap<ProcessValue<Q>, Pair<Int, Int>> {
        val numbering = LinkedHashMap<ProcessValue<Q>, Pair<Int, Int>>()
        val queue = ArrayDeque(listOf(entryPoint to 0))
        while (queue.isNotEmpty()) {
            val (process, depth) = queue.removeFirst()
            if (process in numbering || options.maxDepth?.let { depth > it } == true) continue
            numbering[process] = (numbering.size + 1) to depth
            process.inputs.mapNotNull { suppliers[it.product] }.distinct()
                .forEach { queue.addLast(it to depth + 1) }
        }
        return numbering
    }

    /**
     * The loops of the supply chain: for each process in a loop, the processes of its loop,
     * i.e. of its strongly connected component in the supplier graph.
     */
    private fun loops(
        processes: Set<ProcessValue<Q>>,
        suppliers: Map<ProductValue<Q>, ProcessValue<Q>>,
    ): Map<ProcessValue<Q>, List<ProcessValue<Q>>> {
        fun next(process: ProcessValue<Q>) = process.inputs.mapNotNull { suppliers[it.product] }.distinct()
        return stronglyConnectedComponents(processes, ::next)
            // A single process is a loop only if it uses its own product
            .filter { it.size > 1 || it.single() in next(it.single()) }
            .flatMap { component -> component.map { it to component } }
            .toMap()
    }

    private fun contributions(
        process: ProcessValue<Q>,
        targets: List<MatrixColumnIndex<Q>>,
        analysis: ContributionAnalysis<Q, M>,
    ): List<ProductContributions<Q>> = process.products.mapNotNull { productExchange ->
        val product = productExchange.product
        val allocated = analysis.findOwnerOf(product) ?: return@mapNotNull null
        val productAmount = referenceAmount(productExchange.quantity, product)
        val exchanges = allocated.inputs.mapIndexed { i, it -> Triple(ExchangeKind.INPUT, i, it as ExchangeValue<Q>) } +
            allocated.biosphere.mapIndexed { i, it -> Triple(ExchangeKind.BIOSPHERE, i, it) } +
            allocated.impacts.mapIndexed { i, it -> Triple(ExchangeKind.IMPACT, i, it) }

        val tables = targets.map { target ->
            val rows = exchanges.map { (kind, index, exchange) ->
                val amount = referenceAmount(exchange.quantity(), exchange.port())
                val factor = factor(analysis, exchange.port(), target)
                ContributionRow(kind, index, exchange, amount, factor, amount * factor)
            }
            val shown = rows.filter { it.contribution != 0.0 }
            val total = rows.sumOf { it.contribution }
            ContributionTable(
                target = target,
                rows = shown,
                hidden = rows.size - shown.size,
                total = total,
                productAmount = productAmount,
                perUnit = total / productAmount,
                solverPerUnit = factor(analysis, product, target),
            )
        }
        val (unaffected, affected) = tables.partition { it.rows.isEmpty() && it.matchesSolver }
        ProductContributions(
            product = productExchange,
            share = Allocation(ops).ratioOf(productExchange, process),
            tables = affected,
            unaffected = unaffected.map { it.target },
        )
    }

    /** The impact of one reference unit of [port] on [target], from the solver; NaN if the solver does not know it. */
    private fun factor(analysis: ContributionAnalysis<Q, M>, port: MatrixColumnIndex<Q>, target: MatrixColumnIndex<Q>): Double =
        try {
            with(ops) { analysis.getImpactFactors().characterizationFactor(port, target).amount.toDouble() }
        } catch (e: IllegalStateException) {
            Double.NaN
        }

    private fun referenceAmount(quantity: QuantityValue<Q>, port: MatrixColumnIndex<Q>): Double =
        with(ops) { quantity.amount.toDouble() } * quantity.unit.scale / port.referenceUnit().scale

    private fun globals(processes: List<ProcessExplanation<Q>>): List<VariableDerivation<Q>> =
        processes.flatMap { it.derivation?.variables ?: emptyList() }
            .filter { it.path is SourcePath.Global }
            .distinctBy { it.path }
}
