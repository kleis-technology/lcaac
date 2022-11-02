package ch.kleis.lcaplugin.services

import ch.kleis.lcaplugin.services.formatter.TextBlock
import ch.kleis.lcaplugin.services.formatter.TextIndent
import ch.kleis.lcaplugin.services.formatter.TextLine
import ch.kleis.lcaplugin.services.formatter.TextRegion
import org.openlca.simapro.csv.enums.ElementaryFlowType
import org.openlca.simapro.csv.enums.ProductType
import org.openlca.simapro.csv.process.ProcessBlock
import java.util.stream.Stream

class ScsvProcessBlockFormatter {
    fun format(processBlock: ProcessBlock): String {
        val textRegion = irep(processBlock)
        return textRegion.render()
    }

    private fun irep(processBlock: ProcessBlock): TextRegion {
        return TextBlock(
            listOf(
                TextLine("process \"${processBlock.name()}\" {"),
                TextIndent(
                    listOf(
                        irepProducts(processBlock),
                        irepInputs(processBlock),
                        irepEmissions(processBlock),
                        irepResources(processBlock),
                        irepMeta(processBlock),
                    )
                ),
                TextLine("}")
            )
        )
    }

    private fun irepProducts(processBlock: ProcessBlock): TextRegion {
        return TextBlock(
            listOf(
                TextLine("products {"),
                TextIndent(
                    processBlock.products().stream()
                        .map { TextLine("- \"${it.name()}\" ${it.amount()} ${it.unit()}") }
                        .toList()
                ),
                TextLine("}"),
            )
        )
    }

    private fun irepInputs(processBlock: ProcessBlock): TextRegion {
        val intermediaryInputs = Stream.of(ProductType.MATERIAL_FUELS, ProductType.ELECTRICITY_HEAT, ProductType.WASTE_TO_TREATMENT)
            .flatMap { processBlock.exchangesOf(it).stream() }
        return TextBlock(
            listOf(
                TextLine("inputs {"),
                TextIndent(
                    intermediaryInputs
                        .map { TextLine("- \"${it.name()}\" ${it.amount()} ${it.unit()}") }
                        .toList()
                ),
                TextLine("}")
            )
        )
    }

    private fun irepEmissions(processBlock: ProcessBlock): TextRegion {
        val elementaryOutputs = Stream.of<ElementaryFlowType>(
            ElementaryFlowType.EMISSIONS_TO_AIR,
            ElementaryFlowType.EMISSIONS_TO_WATER,
            ElementaryFlowType.EMISSIONS_TO_SOIL,
            ElementaryFlowType.FINAL_WASTE_FLOWS,
            ElementaryFlowType.NON_MATERIAL_EMISSIONS,
            ElementaryFlowType.SOCIAL_ISSUES,
            ElementaryFlowType.ECONOMIC_ISSUES
        ).flatMap { processBlock.exchangesOf(it).stream()}
        return TextBlock(
            listOf(
                TextLine("emissions {"),
                TextIndent(
                    elementaryOutputs
                        .map { TextLine("- \"${it.name()}\" ${it.amount()} ${it.unit()}") }
                        .toList()
                ),
                TextLine("}"),
            )
        )
    }

    private fun irepResources(processBlock: ProcessBlock): TextRegion {
        val elementaryOutputs = Stream.of<ElementaryFlowType>(
            ElementaryFlowType.RESOURCES,
        ).flatMap { processBlock.exchangesOf(it).stream()}
        return TextBlock(
            listOf(
                TextLine("resources {"),
                TextIndent(
                    elementaryOutputs
                        .map { TextLine("- \"${it.name()}\" ${it.amount()} ${it.unit()}") }
                        .toList()
                ),
                TextLine("}"),
            )
        )
    }

    private fun irepMeta(processBlock: ProcessBlock): TextRegion {
        return TextBlock(
            listOf(
                TextLine("meta {"),
                TextIndent(
                    listOf(
                        TextLine("- identifier: \"${processBlock.identifier()}\""),
                        TextLine("- category: \"${processBlock.category()}\""),
                        TextLine("- processType: \"${processBlock.processType()}\"")
                    )
                ),
                TextLine("}"),
            )
        )
    }
}
