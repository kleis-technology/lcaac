package ch.kleis.lcaplugin.imports.simapro

import arrow.core.toNonEmptyListOrNull
import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.Renderer
import io.ktor.utils.io.*
import org.openlca.simapro.csv.process.*
import org.openlca.simapro.csv.refdata.CalculatedParameterRow
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException

fun ProcessBlock.uid(): String {
    val mainProductName = if (this.products().isNullOrEmpty()) {
        ModelWriter.sanitizeAndCompact(this.identifier())
    } else {
        ModelWriter.sanitizeAndCompact(this.products()[0].uid())
    }
    val identifier = if (this.identifier().isNullOrEmpty()) {
        "unknown"
    } else {
        ModelWriter.sanitizeAndCompact(this.identifier())
    }
    return if (this.name().isNullOrBlank()) {
        mainProductName
    } else {
        // Create unique name in case of coproduct with multiple processes
        // Same process name with 2 case :
        // * same process name and same product name with different identifier : 2 different locations
        // * same process name and diff product name with same identifier : 2 coproducts
        this.name() + (mainProductName.hashCode() % 10000) + "_" + (identifier.hashCode() % 10000)
    }
}

fun ExchangeRow.uid(): String {
    return this.name()
}

private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

class ProcessRenderer : Renderer<ProcessBlock> {
    companion object {
        private val engine: ScriptEngine
        val formulaDetector = Regex(".*[a-zA-DF-Z()/+* ]+.*")

        init {
            val mgr = ScriptEngineManager()
            engine = mgr.getEngineByName("Groovy")
        }

    }


    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun render(process: ProcessBlock, writer: ModelWriter) {

        val pUid = ModelWriter.sanitizeAndCompact(process.uid())
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
        val metaBloc = metas.map { """${it.key} = "${it.value}"""" }

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

        val emissionsToAir = process.emissionsToAir().map { render(it, "_air") }.flatten()
        val emissionsToWater = process.emissionsToWater().map { render(it, "_water") }.flatten()
        val emissionsToSoil = process.emissionsToSoil().map { render(it, "_soil") }.flatten()
        val emissionsNonMat = process.nonMaterialEmissions().map { render(it, "_non_mat") }.flatten()
        val emissionsEconomic = process.economicIssues().map { render(it, "_economic") }.flatten()
        val emissionsSocials = process.socialIssues().map { render(it, "_social") }.flatten()
        val emissionsFinalWasteFlows = process.finalWasteFlows().map { render(it) }.flatten()
        val emissionsWasteToTreatment = process.wasteToTreatment().map { render(it) }.flatten()
        val emissionsRemainingWaste = process.remainingWaste().map { "// QQQ ${it.wasteTreatment()}" }
        val emissionsSeparatedWaste = process.separatedWaste().map { "// QQQ ${it.wasteTreatment()}" }

        val resources = process.resources().map { render(it, "_raw") }.flatten()

        writer.write(
            "processes/${process.category()}",
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

}
"""
        )
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
        val amount = tryToCompute(amountFormula)
        val unit = "u"
        val uid = ModelWriter.sanitize(param.name())
        return result.plus("$uid = $amount $unit // $amountFormula")
    }

    private fun render(
        exchange: ExchangeRow,
        suffix: String = "",
        additionalComments: List<String> = listOf()
    ): List<String> {
        val comments = ModelWriter.asComment(exchange.comment())
        val amountFormula = exchange.amount()
        val amount = tryToCompute(amountFormula.toString())
        val unit = exchange.unit()
        val uid = ModelWriter.sanitizeAndCompact(exchange.uid()) + suffix
        return additionalComments.plus(comments)
            .plus("$amount $unit $uid // $amountFormula")
    }

    private fun renderProduct(product: ProductOutputRow): List<String> {
        val additionalComments = ArrayList<String>();
        product.name()?.let { additionalComments.add("// name: $it") }
        product.category()?.let { additionalComments.add("// category: $it") }

        return render(product, additionalComments = additionalComments)
    }

    private fun renderWasteTreatment(exchange: WasteTreatmentRow): List<String> {
        val additionalComments = ArrayList<String>();
        exchange.name()?.let { additionalComments.add("// name: $it") }
        exchange.category()?.let { additionalComments.add("// category: $it") }
        exchange.wasteType()?.let { additionalComments.add("// wasteType: $it") }

        return render(exchange, additionalComments = additionalComments)
    }

    private fun tryToCompute(amountFormula: String): Any? {

        return try {
            if (formulaDetector.matches(amountFormula)) {
                engine.eval(amountFormula)
            } else {
                amountFormula
            }
        } catch (e: ScriptException) {
            "// QQQ Invalid regex detector for $amountFormula"
        }
    }
}
