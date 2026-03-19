package ch.kleis.lcaac.cli.mermaid

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations

enum class MermaidGraphOption {
    HIDE_PRODUCTS,
    HIDE_QUANTITIES,
    SHOW_BIOSPHERE,
    SHOW_IMPACTS,
}

enum class ImpactMode { RELATIVE, ABSOLUTE }

class MermaidGraph(
    private val trace: EvaluationTrace<BasicNumber>,
    private val options: Set<MermaidGraphOption> = emptySet(),
    private val indicator: IndicatorValue<BasicNumber>? = null,
    private val impactMode: ImpactMode = ImpactMode.RELATIVE,
) {
    private val quantityOps = QuantityValueOperations(BasicOperations)

    fun render(): String {
        val system = trace.getSystemValue()
        val entryPoint = trace.getEntryPoint()
        val contributionAnalysis: ContributionAnalysis<BasicNumber, BasicMatrix> =
            ContributionAnalysisProgram(system, entryPoint).run()

        val substanceCharacterizations = system.substanceCharacterizations
        val entryPointProductIdMap = entryPoint.products
            .mapIndexed { i, output -> output.product to "ep$i" }.toMap()

        val totalImpact = indicator?.let { ind ->
            entryPoint
                .products.map {
                    contributionAnalysis.getPortContribution(it.product, ind)
                }.reduce { acc, value ->
                    with(quantityOps) {
                        acc + value
                    }
                }
        }

        // Nodes
        val productIdMap = system.productToProcessMap.keys
            .sortedBy { it.getDisplayName() }
            .mapIndexed { i, prod -> prod to "prod$i" }.toMap()
        val danglingProducts = system.processes.flatMap { it.inputs }
            .map { it.product }
            .filter { system.productToProcessMap[it] == null }
            .toSet()
        val danglingProductIdMap = danglingProducts
            .sortedBy { it.getDisplayName() }
            .mapIndexed { i, prod -> prod to "dang$i" }.toMap()
        val danglingSubstances = system.processes.flatMap { it.biosphere }
            .map { it.substance }
            .filter { system.substanceToSubstanceCharacterizationMap[it] == null }
            .toSet()
        val danglingSubstanceIdMap =
            if (MermaidGraphOption.SHOW_BIOSPHERE in options)
                danglingSubstances
                    .sortedBy { it.getDisplayName() }
                    .mapIndexed { i, sub -> sub to "sub$i" }.toMap()
            else emptyMap()
        val indicators = (
                system.processes.flatMap { it.impacts } +
                        substanceCharacterizations.flatMap { it.impacts }
                ).map { it.indicator }.toSet()
        val indicatorIdMap =
            if (MermaidGraphOption.SHOW_IMPACTS in options || MermaidGraphOption.SHOW_BIOSPHERE in options)
                indicators
                    .sortedBy { it.name }
                    .mapIndexed { i, ind -> ind to "ind$i" }.toMap()
            else emptyMap()

        return buildString {
            appendLine("flowchart BT")
            appendLine("    classDef invisible fill:none,stroke:none")
            entryPointProductIdMap.forEach { (_, id) ->
                appendLine("    $id[ ]:::invisible")
            }
            productIdMap.forEach { (product, id) ->
                appendLine("    $id[\"${nodeLabel(product)}\"]")
            }
            danglingProductIdMap.forEach { (product, id) ->
                appendLine("    $id[\"${product.getDisplayName()}\"]")
            }
            danglingSubstanceIdMap.forEach { (substance, id) ->
                appendLine("    $id[\"${substance.getDisplayName()}\"]")
            }
            indicatorIdMap.forEach { (indicator, id) ->
                appendLine("    $id[\"${indicator.name}\"]")
            }

            // Edges from entrypoint product nodes to invisible terminal nodes
            entryPoint.products.forEach { output ->
                val label = danglingEdgeLabel(output, contributionAnalysis, totalImpact)
                if (label != null) {
                    appendLine("    ${productIdMap[output.product]} -->|\"$label\"| ${entryPointProductIdMap[output.product]}")
                } else {
                    appendLine("    ${productIdMap[output.product]} --> ${entryPointProductIdMap[output.product]}")
                }
            }

            // Edges derived from each produced product's process
            system.productToProcessMap.entries.sortedBy { it.key.getDisplayName() }.forEach { (product, process) ->
                val targetId = productIdMap[product]!!
                val output = process.products.first { it.product == product }

                // Edges from process inputs
                process.inputs.forEach { input ->
                    val sourceId = productIdMap[input.product] ?: danglingProductIdMap[input.product]
                    if (sourceId != null) {
                        val label = edgeLabel(output, input, contributionAnalysis, totalImpact)
                        if (label != null) {
                            appendLine("    $sourceId -->|\"$label\"| $targetId")
                        } else {
                            appendLine("    $sourceId --> $targetId")
                        }
                    }
                }

                // Edges from process biosphere
                if (MermaidGraphOption.SHOW_BIOSPHERE in options) {
                    process.biosphere.forEach { emission ->
                        val sc = system.substanceToSubstanceCharacterizationMap[emission.substance]
                        if (sc != null) {
                            sc.impacts.forEach { impact ->
                                val label = edgeLabel(output, impact, contributionAnalysis, totalImpact)
                                if (label != null) {
                                    appendLine("    ${indicatorIdMap[impact.indicator]} -->|\"$label\"| $targetId")
                                } else {
                                    appendLine("    ${indicatorIdMap[impact.indicator]} --> $targetId")
                                }
                            }
                        } else {
                            val label = edgeLabel(output, emission, contributionAnalysis, totalImpact)
                            if (label != null) {
                                appendLine("    ${danglingSubstanceIdMap[emission.substance]} -->|\"$label\"| $targetId")
                            } else {
                                appendLine("    ${danglingSubstanceIdMap[emission.substance]} --> $targetId")
                            }
                        }
                    }
                }

                // Edges from process direct impacts
                if (MermaidGraphOption.SHOW_IMPACTS in options) {
                    process.impacts.forEach { impact ->
                        val label = edgeLabel(output, impact, contributionAnalysis, totalImpact)
                        if (label != null) {
                            appendLine("    ${indicatorIdMap[impact.indicator]} -->|\"$label\"| $targetId")
                        } else {
                            appendLine("    ${indicatorIdMap[impact.indicator]} --> $targetId")
                        }
                    }
                }
            }
        }
    }

    private fun formatQuantity(quantity: QuantityValue<BasicNumber>): String =
        "${"%.2e".format(quantity.amount.value)} ${quantity.unit}"

    private fun formatContribution(
        contribution: QuantityValue<BasicNumber>,
        totalImpact: QuantityValue<BasicNumber>?,
    ): String {
        if (indicator == null) return ""
        return when (impactMode) {
            ImpactMode.ABSOLUTE -> "${formatQuantity(contribution)} ${indicator.name}"
            ImpactMode.RELATIVE -> {
                val percent = with(quantityOps) {
                    contribution.absoluteScaleValue().value /
                        (totalImpact?.absoluteScaleValue()?.value ?: 1.0) * 100.0
                }
                "${"%.1f".format(percent)}% ${indicator.name}"
            }
        }
    }

    private fun danglingEdgeLabel(
        output: TechnoExchangeValue<BasicNumber>,
        contributionAnalysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
        totalImpact: QuantityValue<BasicNumber>?,
    ): String? {
        val parts = mutableListOf<String>()
        val exchangeParts = mutableListOf<String>()
        if (MermaidGraphOption.HIDE_QUANTITIES !in options) {
            val outputSupply = contributionAnalysis.supplyOf(output.product)
            exchangeParts.add(formatQuantity(outputSupply))
        }
        if (MermaidGraphOption.HIDE_PRODUCTS !in options) {
            exchangeParts.add(output.port().getShortName())
        }
        if (exchangeParts.isNotEmpty()) parts.add(exchangeParts.joinToString(" "))
        if (indicator != null) {
            val contribution = contributionAnalysis.getPortContribution(output.product, indicator)
            parts.add(formatContribution(contribution, totalImpact))
        }
        return if (parts.isEmpty()) null else parts.joinToString("\\n")
    }

    private fun edgeLabel(
        output: ExchangeValue<BasicNumber>,
        input: ExchangeValue<BasicNumber>,
        contributionAnalysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
        totalImpact: QuantityValue<BasicNumber>?,
    ): String? {
        val parts = mutableListOf<String>()

        val exchangeParts = mutableListOf<String>()
        if (MermaidGraphOption.HIDE_QUANTITIES !in options) {
            val outputQuantity = output.quantity()
            val outputSupply = contributionAnalysis.supplyOf(output.port())
            val inputQuantity = input.quantity()
            with(quantityOps) {
                val quantity = ((outputSupply / outputQuantity) * inputQuantity).toUnit(inputQuantity.unit)
                exchangeParts.add(formatQuantity(quantity))
            }
        }
        if (MermaidGraphOption.HIDE_PRODUCTS !in options) {
            val portName = when (input.port()) {
                is IndicatorValue<*> -> input.port().getShortName()
                is ProductValue<*> -> input.port().getShortName()
                is FullyQualifiedSubstanceValue<*> -> input.port().getDisplayName()
                is PartiallyQualifiedSubstanceValue<*> -> input.port().getDisplayName()
            }
            exchangeParts.add(portName)
        }
        if (exchangeParts.isNotEmpty()) parts.add(exchangeParts.joinToString(" "))
        if (indicator != null) {
            val contribution = contributionAnalysis.getExchangeContribution(output.port(), input, indicator)
            parts.add(formatContribution(contribution, totalImpact))
        }
        return if (parts.isEmpty()) null else parts.joinToString("\\n")
    }

    private fun nodeLabel(product: ProductValue<BasicNumber>): String {
        val ref = product.fromProcessRef ?: return product.name
        val parts = mutableListOf(ref.name)
        ref.matchLabels.entries.sortedBy { it.key }.forEach { (k, v) -> parts.add("$k: ${v.s}") }
        ref.arguments.entries.sortedBy { it.key }.forEach { (k, v) -> parts.add("$k: $v") }
        return parts.joinToString("\\n")
    }
}
