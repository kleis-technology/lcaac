package ch.kleis.lcaplugin.imports.simapro

import arrow.core.toNonEmptyListOrNull
import ch.kleis.lcaplugin.ide.imports.simapro.SubstanceImportMode
import ch.kleis.lcaplugin.imports.FormulaConverter
import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.Renderer
import ch.kleis.lcaplugin.imports.simapro.substance.Dictionary
import ch.kleis.lcaplugin.imports.simapro.substance.Ef3xDictionary
import ch.kleis.lcaplugin.imports.simapro.substance.SimaproDictionary
import org.openlca.simapro.csv.process.*
import org.openlca.simapro.csv.refdata.CalculatedParameterRow
import java.io.File
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

private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

class ProcessRenderer(mode: SubstanceImportMode) : Renderer<ProcessBlock> {
    private val substanceDict: Dictionary = when (mode) {
        SubstanceImportMode.SIMAPRO -> SimaproDictionary()
        SubstanceImportMode.NOTHING -> SimaproDictionary() // We don't import substances, but exchanges are linked with simapro naming
        SubstanceImportMode.EF30 -> Ef3xDictionary.fromClassPath("emissions_factors3.0.jar")
        SubstanceImportMode.EF31 -> Ef3xDictionary.fromClassPath("emissions_factors3.1.jar")
    }
    var nbProcesses: Int = 0


    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun render(process: ProcessBlock, writer: ModelWriter) {

        val pUid = process.uid()
        val metas = mutableMapOf<String, String>()
        process.comment()?.let { metas["description"] = ModelWriter.compactAndPad(it, 12) }
        process.category()?.let { metas["category"] = ModelWriter.compactText(it.toString()) }
        process.identifier()?.let { metas["identifier"] = ModelWriter.compactText(it) }
        process.comment()?.let { metas["comment"] = ModelWriter.compactAndPad(it, 12) }
        process.date()?.let {
            metas["date"] =
                ModelWriter.compactText(dateFormatter.format(it.toInstant().atZone(ZoneId.of("UTC")).toLocalDate()))
        }
        process.generator()?.let { metas["generator"] = ModelWriter.compactAndPad(it, 12) }
        process.collectionMethod()?.let { metas["collectionMethod"] = ModelWriter.compactAndPad(it, 12) }
        process.dataTreatment()?.let { metas["dataTreatment"] = ModelWriter.compactAndPad(it, 12) }
        process.verification()?.let { metas["verification"] = ModelWriter.compactAndPad(it, 12) }
        process.systemDescription()
            ?.let { metas["systemDescription"] = ModelWriter.compactText("${it.name()}: ${it.comment()}") }
        process.allocationRules()?.let { metas["allocationRules"] = ModelWriter.compactAndPad(it, 12) }
        process.processType()?.let { metas["processType"] = ModelWriter.compactText(it.toString()) }
        process.status()?.let { metas["status"] = ModelWriter.compactText(it.toString()) }
        process.infrastructure()?.let { metas["infrastructure"] = ModelWriter.compactText(it.toString()) }
        process.record()?.let { metas["record"] = ModelWriter.compactText(it) }
        process.platformId()?.let { metas["platformId"] = ModelWriter.compactText(it) }
        process.literatures()?.toNonEmptyListOrNull()
            ?.let {
                metas["literatures"] =
                    it.map { s -> renderLiterature(s) }
                        .joinToString("\n", "\n")
            }
        val metaBloc = metas.map { """"${it.key}" = "${it.value}"""" }

        val baseProducts = process.products().map { renderProduct(it) }
        val wasteTreatment =
            if (process.wasteTreatment() == null) listOf() else listOf(renderWasteTreatment(process.wasteTreatment()))
        val wasteScenario =
            if (process.wasteScenario() == null) listOf() else listOf(renderWasteTreatment(process.wasteScenario()))
        val products = baseProducts
            .plus(wasteTreatment)
            .plus(wasteScenario).flatten()

        val avoidProducts = process.avoidedProducts().map { render(it) }.flatten()

        val params = process.calculatedParameters().map { render(it) }.flatten()

        val inputsMatAndFuel = process.materialsAndFuels().map { render(it) }.flatten()
        val inputsElectricity = process.electricityAndHeat().map { render(it) }.flatten()

        val emissionsToAir = process.emissionsToAir().map { renderElementary(it, "Emission", "air") }.flatten()
        val emissionsToWater = process.emissionsToWater().map { renderElementary(it, "Emission", "water") }.flatten()
        val emissionsToSoil = process.emissionsToSoil().map { renderElementary(it, "Emission", "soil") }.flatten()
        val emissionsNonMat =
            process.nonMaterialEmissions().map { renderElementary(it, "Emission", "non_mat") }.flatten()
        val emissionsEconomic = process.economicIssues().map { renderElementary(it, "Emission", "economic") }.flatten()
        val emissionsSocials = process.socialIssues().map { renderElementary(it, "Emission", "social") }.flatten()
        val emissionsFinalWasteFlows = process.finalWasteFlows().map { render(it) }.flatten()
        val emissionsWasteToTreatment = process.wasteToTreatment().map { render(it) }.flatten()
        val emissionsRemainingWaste = process.remainingWaste().map { "// QQQ ${it.wasteTreatment()}" }
        val emissionsSeparatedWaste = process.separatedWaste().map { "// QQQ ${it.wasteTreatment()}" }

        val allRes = process.resources().groupBy { if (it.subCompartment() == "land") "land_use" else "resources" }
        val resources = (allRes["resources"] ?: listOf()).map { renderElementary(it, "Resource", "raw") }.flatten()
        val landUses = (allRes["land_use"] ?: listOf()).map { renderElementary(it, "Land_use", "raw") }.flatten()

        val subFolder = if (process.category() == null) "" else "${process.category()}${File.separatorChar}"
        writer.write(
            "processes${File.separatorChar}$subFolder$pUid.lca",
            """

process $pUid {

${ModelWriter.block("meta {", metaBloc)}

${ModelWriter.optionalBlock("variables {", params)}

${ModelWriter.block("products { // Product", products)}
    
${ModelWriter.block("products { // Avoid Products", avoidProducts)}
    
${ModelWriter.block("inputs { // materialsAndFuels", inputsMatAndFuel)}

${ModelWriter.block("inputs { // electricityAndHeat", inputsElectricity)}

${ModelWriter.block("emissions { // To Air", emissionsToAir)}

${ModelWriter.block("emissions { // To Water", emissionsToWater)}

${ModelWriter.block("emissions { // To Soil", emissionsToSoil)}

${ModelWriter.block("emissions { // Economics", emissionsEconomic)}

${ModelWriter.block("emissions { // Non Material", emissionsNonMat)}

${ModelWriter.block("emissions { // Social", emissionsSocials)}

${ModelWriter.block("emissions { // Final Waste Flows", emissionsFinalWasteFlows)}

${ModelWriter.block("inputs { // Waste To Treatment", emissionsWasteToTreatment)}

${ModelWriter.block("emissions { // Remaining Waste", emissionsRemainingWaste)}

${ModelWriter.block("emissions { // Separated Waste", emissionsSeparatedWaste)}

${ModelWriter.block("resources {", resources)}

${ModelWriter.block("land_use {", landUses)}

}
""", index = true, closeAfterWrite = true
        )
        nbProcesses++
    }

    private fun renderLiterature(s: LiteratureRow): String {
        val sep = " ".repeat(12)
        return s.name()
            ?.split("\n")
            ?.joinToString("\n$sep", "$sep* ") { ModelWriter.compactText(it) }
            ?: ""
    }


    private fun render(param: CalculatedParameterRow): List<String> {
        val result = if (param.comment().isNullOrBlank()) emptyList<String>() else listOf(param.comment())
        val amountFormula = param.expression()
        val (amount, changed) = FormulaConverter.compute(amountFormula)
        val unit = "u"
        val uid = ModelWriter.sanitize(param.name())
        val endingComment = createCommentLine(listOf(if (changed) "Formula=[$amountFormula]" else ""))
        return result.plus("$uid = $amount $unit$endingComment")
    }

    private fun render(
        exchange: ExchangeRow,
        suffix: String = "",
        additionalComments: List<String> = listOf()
    ): List<String> {
        val comments = ModelWriter.asComment(exchange.comment())
        val amountFormula = exchange.amount().toString()
        val (amount, changed) = FormulaConverter.compute(amountFormula)
        val unit = sanitizeUnit(exchange.unit())
        val uid = ModelWriter.sanitizeAndCompact(exchange.name()) + suffix
        val endingComment = createCommentLine(listOf(if (changed) "Formula=[$amountFormula]" else ""))
        return additionalComments.plus(comments)
            .plus("$amount $unit $uid$endingComment")
    }

    private fun renderElementary(
        exchange: ElementaryExchangeRow,
        type: String,
        compartment: String
    ): List<String> {
        val comments = ModelWriter.asComment(exchange.comment())
        val amountFormula = exchange.amount().toString()
        val (amount, changed) = FormulaConverter.compute(amountFormula)
        val unit = sanitizeUnit(exchange.unit())
        val sub = exchange.subCompartment()
        val name = exchange.name()
        val realKey = substanceDict.realKeyForSubstance(name, type, unit, compartment, sub)
        val info = if (!realKey.hasChanged) "" else "Fallback for [$name, $type, $compartment, ${sub}]"
        val endingComment = createCommentLine(listOf(if (changed) "Formula=[$amountFormula]" else "", info))
        val uid = realKey.uid()
        val args = listOfNotNull(
            """compartment = "$compartment"""",
            if (!sub.isNullOrBlank()) """sub_compartment = "$sub" """ else null,
        ).joinToString()
        val spec = "$uid($args)"
        return comments.plus("$amount $unit $spec $endingComment")
    }

    private fun renderProduct(product: ProductOutputRow): List<String> {
        val additionalComments = ArrayList<String>()
        product.name()?.let { additionalComments.add("// name: $it") }
        product.category()?.let { additionalComments.add("// category: $it") }

        val comments = ModelWriter.asComment(product.comment())
        val amountFormula = product.amount().toString()
        val (amount, changed) = FormulaConverter.compute(amountFormula)
        val unit = sanitizeUnit(product.unit())
        val uid = product.uid()
        val allocation = product.allocation()
        val endingComment = createCommentLine(listOf(if (changed) "Formula=[$amountFormula]" else ""))
        return additionalComments.plus(comments)
            .plus("$amount $unit $uid allocate $allocation percent$endingComment")
    }

    private fun renderWasteTreatment(exchange: WasteTreatmentRow): List<String> {
        val additionalComments = ArrayList<String>()
        exchange.name()?.let { additionalComments.add("// name: $it") }
        exchange.category()?.let { additionalComments.add("// category: $it") }
        exchange.wasteType()?.let { additionalComments.add("// wasteType: $it") }

        return render(exchange, additionalComments = additionalComments)
    }

    private fun createCommentLine(comments: List<String>): String {
        val cleaned = comments.filter { it.isNotBlank() }

        return if (cleaned.isEmpty()) ""
        else cleaned.joinToString(", ", " // ")
    }

}
