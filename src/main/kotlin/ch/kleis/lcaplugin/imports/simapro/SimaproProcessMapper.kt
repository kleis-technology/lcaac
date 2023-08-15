package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.ide.imports.simapro.SubstanceImportMode
import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.model.*
import ch.kleis.lcaplugin.imports.shared.serializer.FormulaConverter
import ch.kleis.lcaplugin.imports.simapro.substance.Dictionary
import ch.kleis.lcaplugin.imports.simapro.substance.Ef3xDictionary
import ch.kleis.lcaplugin.imports.simapro.substance.SimaproDictionary
import org.openlca.simapro.csv.process.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter


fun ProcessBlock.uid(): String {
    val mainProductNameRaw = if (this.products().isNullOrEmpty()) {
        this.identifier()
    } else {
        this.products()[0].uid()
    }
    val mainProductName = ModelWriter.sanitizeAndCompact(mainProductNameRaw)
    val identifierRaw = if (this.identifier().isNullOrEmpty()) {
        "unknown"
    } else {
        this.identifier()
    }
    val uidRaw = if (this.name().isNullOrBlank()) {
        mainProductName
    } else {
        // Create unique name in case of coproduct with multiple processes
        // Same process name with 2 case :
        // * same process name and same product name with different identifier : 2 different locations
        // * same process name and diff product name with same identifier : 2 coproducts
        this.name() + "_" + (mainProductNameRaw.hashCode() % 10000) + "_" + (identifierRaw.hashCode() % 10000)
    }
    return ModelWriter.sanitizeAndCompact(uidRaw)
}

fun ProductOutputRow.uid(): String {
    return ModelWriter.sanitizeAndCompact(this.name())
}


class SimaproProcessMapper(mode: SubstanceImportMode) {
    companion object {
        fun of(mode: SubstanceImportMode): SimaproProcessMapper {
            return SimaproProcessMapper(mode)
        }
    }

    private val substanceDict: Dictionary = when (mode) {
        SubstanceImportMode.SIMAPRO -> SimaproDictionary()
        SubstanceImportMode.NOTHING -> SimaproDictionary() // We don't import substances, but exchanges are linked with simapro naming
        SubstanceImportMode.EF30 -> Ef3xDictionary.fromClassPath("emissions_factors3.0.jar")
        SubstanceImportMode.EF31 -> Ef3xDictionary.fromClassPath("emissions_factors3.1.jar")
    }

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun map(process: ProcessBlock): ImportedProcess {


        // Products
        val baseProducts = ExchangeBlock("Products", process.products().map { renderProduct(it) }.asSequence())
        val wasteTreatement = process.wasteTreatment()?.let {
            ExchangeBlock("Waste treatment", sequenceOf(renderWasteTreatment(it)))
        }
        val wasteScenario = process.wasteScenario()?.let {
            ExchangeBlock("Waste scenario", sequenceOf(renderWasteTreatment(it)))
        }
        val avoidedProducts = ExchangeBlock(
            "Avoided Products",
            process.avoidedProducts().asSequence().map { render(it) }
        )


        // Resources & Landuse
        val allRes = process.resources().groupBy { if (it.subCompartment() == "land") "land_use" else "resources" }
        val resources = mapResourcesOrLanduse(allRes["resources"]?.asSequence(), "Resource")
        val landUse = mapResourcesOrLanduse(allRes["land_use"]?.asSequence(), "Land_use")

        val result = ImportedProcess(
            uid = process.uid(),
            meta = mapMetas(process),
            productBlocks = listOfNotNull(baseProducts, wasteTreatement, wasteScenario, avoidedProducts),
            inputBlocks = listOf(
                mapInputs(process.materialsAndFuels().asSequence(), "inputsMatAndFuel"),
                mapInputs(process.electricityAndHeat().asSequence(), "inputsElectricity"),
                mapInputs(process.wasteToTreatment().asSequence(), "wasteToTreatment"),
            ),
            emissionBlocks = listOf(
                mapEmissions(process.emissionsToAir().asSequence(), "air"),
                mapEmissions(process.emissionsToWater().asSequence(), "water"),
                mapEmissions(process.emissionsToSoil().asSequence(), "soil"),
                mapEmissions(process.economicIssues().asSequence(), "economic"),
                mapEmissions(process.nonMaterialEmissions().asSequence(), "non_mat"),
                mapEmissions(process.socialIssues().asSequence(), "social"),
                mapEmissions(process.finalWasteFlows().asSequence(), "Final Waste Flows"),
            ),
            resourceBlocks = listOf(resources),
            landUseBlocks = listOf(landUse),
        )

        // Unsupported
        result.comments.addAll(process.remainingWaste().map { "// QQQ Unsupported ${it.wasteTreatment()}" })
        result.comments.addAll(process.separatedWaste().map { "// QQQ Unsupported ${it.wasteTreatment()}" })

        return result
    }

    private fun mapMetas(process: ProcessBlock) = mapOf(
        "description" to process.comment(),
        "category" to process.category()?.toString(),
        "identifier" to process.identifier(),
        "date" to process.date()?.let {
            dateFormatter.format(it.toInstant().atZone(ZoneId.of("UTC")).toLocalDate())
        },
        "generator" to process.generator(),
        "collectionMethod" to process.collectionMethod(),
        "dataTreatment" to process.dataTreatment(),
        "verification" to process.verification(),
        "systemDescription" to process.systemDescription()?.let { "${it.name()}: ${it.comment()}" },
        "allocationRules" to process.allocationRules(),
        "processType" to process.processType()?.toString(),
        "status" to process.status()?.toString(),
        "infrastructure" to process.infrastructure()?.toString(),
        "record" to process.record(),
        "platformId" to process.platformId(),
        "literatures" to process.literatures()?.joinToString("\n", "\n") { s ->
            renderLiterature(s)
        }
    )

    private fun mapInputs(
        techExchangeRows: Sequence<TechExchangeRow>,
        type: String
    ): ExchangeBlock<ImportedInputExchange> =
        ExchangeBlock(type, techExchangeRows.map { render(it).asInput() })

    private fun mapEmissions(
        elementaryExchangeRows: Sequence<ElementaryExchangeRow>?,
        comp: String
    ): ExchangeBlock<ImportedBioExchange> =
        ExchangeBlock(
            "Emission to $comp",
            elementaryExchangeRows?.map { renderElementary(it, "Emission", comp) } ?: emptySequence()
        )

    private fun mapResourcesOrLanduse(
        elementaryExchangeRows: Sequence<ElementaryExchangeRow>?,
        type: String
    ): ExchangeBlock<ImportedBioExchange> =
        ExchangeBlock("",
            elementaryExchangeRows?.map { renderElementary(it, type, "raw") } ?: emptySequence()
        )

    private fun renderLiterature(s: LiteratureRow): String {
        val sep = " ".repeat(ModelWriter.BASE_PAD)
        return s.name()
            ?.split("\n")
            ?.joinToString("\n$sep$sep", "$sep* ") { ModelWriter.compactText(it) }
            ?: ""
    }


    private fun renderProduct(product: ProductOutputRow): ImportedProductExchange {
        val initComments = ArrayList<String>()
        product.name()?.let { initComments.add("name: $it") }
        product.category()?.let { initComments.add("category: $it") }
        val comments = createComments(product.comment(), initComments)
        val unit = sanitizeSymbol(product.unit())
        val allocation = product.allocation().value()
        val amount = FormulaConverter.compute(product.amount().toString(), comments)
        return ImportedProductExchange(amount, unit, product.uid(), allocation, comments)
    }

    private fun renderWasteTreatment(exchange: WasteTreatmentRow): ImportedProductExchange {
        val additionalComments = ArrayList<String>()
        exchange.name()?.let { additionalComments.add("name: $it") }
        exchange.category()?.let { additionalComments.add("category: $it") }
        exchange.wasteType()?.let { additionalComments.add("wasteType: $it") }

        return render(exchange, additionalComments = additionalComments)
    }

    private fun render(
        exchange: ExchangeRow,
        suffix: String = "",
        additionalComments: List<String> = listOf()
    ): ImportedProductExchange {
        val comments = createComments(exchange.comment(), additionalComments)
        val unit = sanitizeSymbol(exchange.unit())
        val uid = ModelWriter.sanitizeAndCompact(exchange.name()) + suffix
        val amount = FormulaConverter.compute(exchange.amount().toString(), comments)
        return ImportedProductExchange(amount, unit, uid, comments = comments)
    }

    private fun createComments(text: String, existingComments: List<String> = listOf()): MutableList<String> {
        val comments = existingComments.toMutableList()
        val additionalComments = text
            .split("\n")
            .filter { it.isNotBlank() }
        comments.addAll(additionalComments)
        return comments
    }

    private fun renderElementary(
        exchange: ElementaryExchangeRow,
        type: String,
        compartment: String
    ): ImportedBioExchange {
        val comments = createComments(exchange.comment())
        val amount = FormulaConverter.compute(exchange.amount().toString(), comments)
        val unit = sanitizeSymbol(exchange.unit())
        val sub = exchange.subCompartment().ifBlank { null }
        val name = exchange.name()
        val realKey = substanceDict.realKeyForSubstance(name, type, unit, compartment, sub)
        if (realKey.hasChanged) {
            comments.add("Fallback for [$name, $type, $compartment, ${sub}]")
        }

        val uid = realKey.uid()
        return ImportedBioExchange(amount, unit, uid, compartment, sub, comments)
    }
}