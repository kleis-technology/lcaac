package ch.kleis.lcaplugin.services

import ch.kleis.lcaplugin.services.formatter.TextBlock
import ch.kleis.lcaplugin.services.formatter.TextIndent
import ch.kleis.lcaplugin.services.formatter.TextLine
import ch.kleis.lcaplugin.services.formatter.TextRegion
import com.intellij.util.containers.addAllIfNotNull
import org.openlca.simapro.csv.enums.ElementaryFlowType
import org.openlca.simapro.csv.enums.ProductType
import org.openlca.simapro.csv.process.ElementaryExchangeRow
import org.openlca.simapro.csv.process.ProcessBlock
import java.util.*
import java.util.regex.Pattern
import java.util.regex.Pattern.MULTILINE
import java.util.regex.Pattern.compile
import java.util.stream.Stream

class ScsvProcessBlockFormatter(
    private val pkg: String
) {
    fun format(processBlock: ProcessBlock): String {
        val textRegion = irep(processBlock)
        return textRegion.render()
    }

    private fun irep(processBlock: ProcessBlock): TextRegion {
        return TextBlock(
            listOf(
                TextLine("package $pkg"),
                TextLine(""),
                TextLine("process ${stringLiteral(processBlock.name())} {"),
                TextIndent(
                    listOf(
                        irepProducts(processBlock),
                        irepInputs(processBlock),
                        irepEmissions(processBlock),
                        irepResources(processBlock),
                        irepMeta(processBlock),
                    )
                ),
                TextLine("}"),
                TextLine(""),
            )
        )
    }

    private fun stringLiteral(name: String?): String {
        return "\"${escape(name)}\"" ?: ""
    }

    private fun escape(name: String?): String? {
        return name?.replace("\"", "\\\"")
    }

    private fun irepProducts(processBlock: ProcessBlock): TextRegion {
        val products = listOf(
            processBlock.products().stream(),
            Optional.ofNullable(processBlock.wasteTreatment()).stream(),
            processBlock.avoidedProducts().stream()
        ).stream().flatMap { it }.toList()

        if (products.isEmpty()) {
            return TextBlock(emptyList())
        }

        return TextBlock(
            listOf(
                TextLine("products {"),
                TextIndent(
                    products
                        .map { TextLine("- ${stringLiteral(it.name())} ${it.amount()} ${it.unit()}") }
                        .toList()
                ),
                TextLine("}"),
                TextLine(""),
            )
        )
    }

    private fun irepInputs(processBlock: ProcessBlock): TextRegion {
        val intermediaryInputs =
            Stream.of(ProductType.MATERIAL_FUELS, ProductType.ELECTRICITY_HEAT, ProductType.WASTE_TO_TREATMENT)
                .flatMap { processBlock.exchangesOf(it).stream() }.toList()
        if (intermediaryInputs.isEmpty()) {
            return TextBlock(emptyList())
        }
        return TextBlock(
            listOf(
                TextLine("inputs {"),
                TextIndent(
                    intermediaryInputs
                        .map { TextLine("- ${stringLiteral(it.name())} ${it.amount()} ${it.unit()}") }
                        .toList()
                ),
                TextLine("}"),
                TextLine(""),
            )
        )
    }

    private fun irepEmissions(processBlock: ProcessBlock): TextRegion {
        val elementaryOutputs = Stream.of(
            ElementaryFlowType.EMISSIONS_TO_AIR,
            ElementaryFlowType.EMISSIONS_TO_WATER,
            ElementaryFlowType.EMISSIONS_TO_SOIL,
            ElementaryFlowType.FINAL_WASTE_FLOWS,
            ElementaryFlowType.NON_MATERIAL_EMISSIONS,
            ElementaryFlowType.SOCIAL_ISSUES,
            ElementaryFlowType.ECONOMIC_ISSUES
        ).flatMap { type ->
            processBlock.exchangesOf(type).stream()
                .map { exchange -> Pair(type, exchange) }
        }.toList()

        if (elementaryOutputs.isEmpty()) {
            return TextBlock(emptyList())
        }
        return TextBlock(
            listOf(
                TextLine("emissions {"),
                TextIndent(
                    elementaryOutputs
                        .map { TextLine("- ${substanceId(it.first, it.second)} ${it.second.amount()} ${it.second.unit()}") }
                        .toList()
                ),
                TextLine("}"),
                TextLine(""),
            )
        )
    }

    private fun substanceId(type: ElementaryFlowType, exchange: ElementaryExchangeRow): String {
        return stringLiteral(listOfNotNull(
            escape(exchange.name()),
            escape(type.compartment()),
            escape(exchange.subCompartment()),
        )
            .filter { it.isNotBlank() }
            .joinToString(", ")
            .lowercase())
    }

    private fun irepResources(processBlock: ProcessBlock): TextRegion {
        val elementaryOutputs = Stream.of(
            ElementaryFlowType.RESOURCES,
        ).flatMap { type ->
            processBlock.exchangesOf(type).stream()
                .map { exchange -> Pair(type, exchange) }
        }.toList()
        if (elementaryOutputs.isEmpty()) {
            return TextBlock(emptyList())
        }
        return TextBlock(
            listOf(
                TextLine("resources {"),
                TextIndent(
                    elementaryOutputs
                        .map { TextLine("- ${substanceId(it.first, it.second)} ${it.second.amount()} ${it.second.unit()}") }
                        .toList()
                ),
                TextLine("}"),
                TextLine(""),
            )
        )
    }

    private fun irepMeta(processBlock: ProcessBlock): TextRegion {
        val regions = ArrayList<TextLine>()
        regions.addAllIfNotNull(
            TextLine("- identifier: \"${processBlock.identifier()}\""),
            TextLine("- category: \"${processBlock.category()}\""),
            TextLine("- processType: \"${processBlock.processType()}\""),
            geography(processBlock)
        )
        return TextBlock(
            listOf(
                TextLine("meta {"),
                TextIndent(
                    regions
                ),
                TextLine("}"),
                TextLine(""),
            )
        )
    }

    private fun geography(processBlock: ProcessBlock): TextLine? {
        val pattern: Pattern = compile("^Geography: .* modelled for (.*)$", MULTILINE)
        val comment = processBlock.comment() ?: return null
        val matcher = pattern.matcher(comment)
        if (matcher.find()) {
            val geography = matcher.group(1)
            return TextLine("- geography: \"${geography}\"")
        }
        return null
    }
}
