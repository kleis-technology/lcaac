package ch.kleis.lcaplugin.imports.simapro

import arrow.core.toNonEmptyListOrNull
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

        val result = ImportedProcess(process.uid())
        val metas = result.meta
        process.comment()?.let { metas["description"] = it }
        process.category()?.let { metas["category"] = it.toString() }
        process.identifier()?.let { metas["identifier"] = it }
        process.date()?.let {
            metas["date"] = dateFormatter.format(it.toInstant().atZone(ZoneId.of("UTC")).toLocalDate())
        }
        process.generator()?.let { metas["generator"] = it }
        process.collectionMethod()?.let { metas["collectionMethod"] = it }
        process.dataTreatment()?.let { metas["dataTreatment"] = it }
        process.verification()?.let { metas["verification"] = it }
        process.systemDescription()
            ?.let { metas["systemDescription"] = "${it.name()}: ${it.comment()}" }
        process.allocationRules()?.let { metas["allocationRules"] = it }
        process.processType()?.let { metas["processType"] = it.toString() }
        process.status()?.let { metas["status"] = it.toString() }
        process.infrastructure()?.let { metas["infrastructure"] = it.toString() }
        process.record()?.let { metas["record"] = it }
        process.platformId()?.let { metas["platformId"] = it }
        process.literatures()?.toNonEmptyListOrNull()
            ?.let {
                metas["literatures"] =
                    it.map { s -> renderLiterature(s) }
                        .joinToString("\n", "\n")
            }

        // Products
        val baseProducts = process.products().map { renderProduct(it) }
        result.productBlocks.add(ExchangeBlock("Products", baseProducts.toMutableList()))

        if (process.wasteTreatment() != null) {
            val exchanges = listOf(renderWasteTreatment(process.wasteTreatment())).toMutableList()
            result.productBlocks.add(ExchangeBlock("Waste treatment", exchanges))
        }
        if (process.wasteScenario() != null) {
            val exchanges = listOf(renderWasteTreatment(process.wasteScenario()))
            result.productBlocks.add(ExchangeBlock("Waste scenario", exchanges.toMutableList()))
        }

        val avoidProducts = process.avoidedProducts().map { render(it) }
        result.productBlocks.add(ExchangeBlock("Avoid Products", avoidProducts.toMutableList()))

        // Inputs
        result.inputBlocks.add(mapInputs(process.materialsAndFuels(), "inputsMatAndFuel"))
        result.inputBlocks.add(mapInputs(process.electricityAndHeat(), "inputsElectricity"))
        result.inputBlocks.add(mapInputs(process.wasteToTreatment(), "wasteToTreatment"))

        result.emissionBlocks.add(mapEmissions(process.emissionsToAir(), "air"))
        result.emissionBlocks.add(mapEmissions(process.emissionsToWater(), "water"))
        result.emissionBlocks.add(mapEmissions(process.emissionsToSoil(), "soil"))
        result.emissionBlocks.add(mapEmissions(process.economicIssues(), "economic"))
        result.emissionBlocks.add(mapEmissions(process.nonMaterialEmissions(), "non_mat"))
        result.emissionBlocks.add(mapEmissions(process.socialIssues(), "social"))
        result.emissionBlocks.add(mapEmissions(process.finalWasteFlows(), "Final Waste Flows"))
        // Unsupported
        result.comments.addAll(process.remainingWaste().map { "// QQQ Unsupported ${it.wasteTreatment()}" })
        result.comments.addAll(process.separatedWaste().map { "// QQQ Unsupported ${it.wasteTreatment()}" })

        // Resources & Landuse
        val allRes = process.resources().groupBy { if (it.subCompartment() == "land") "land_use" else "resources" }
        result.resourceBlocks.add(mapResourcesOrLanduse(allRes["resources"], "Resource"))
        result.landUseBlocks.add(mapResourcesOrLanduse(allRes["land_use"], "Land_use"))

        return result
    }

    private fun mapInputs(lst: List<TechExchangeRow>, type: String): ExchangeBlock<ImportedInputExchange> {
        val exchanges = lst.map { render(it).asInput() }
        return ExchangeBlock(type, exchanges.toMutableList())
    }

    private fun mapEmissions(
        lst: List<ElementaryExchangeRow>?,
        comp: String
    ): ExchangeBlock<ImportedBioExchange> {
        val exchanges = lst?.map { renderElementary(it, "Emission", comp) } ?: listOf()
        return ExchangeBlock("Emission to $comp", exchanges.toMutableList())
    }

    private fun mapResourcesOrLanduse(
        lst: List<ElementaryExchangeRow>?,
        type: String
    ): ExchangeBlock<ImportedBioExchange> {
        val exchanges = lst?.map { renderElementary(it, type, "raw") } ?: listOf()
        return ExchangeBlock("", exchanges.toMutableList())
    }

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