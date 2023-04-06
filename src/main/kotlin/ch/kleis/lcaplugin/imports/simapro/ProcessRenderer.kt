package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.Renderer
import io.ktor.utils.io.*
import org.openlca.simapro.csv.process.ExchangeRow
import org.openlca.simapro.csv.process.ProcessBlock
import org.openlca.simapro.csv.refdata.CalculatedParameterRow
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException

fun ProcessBlock.uid(): String {
    val mainProductName = if (this.products().isNullOrEmpty()) {
        ModelWriter.sanitizeString(this.identifier())
    } else {
        ModelWriter.sanitizeString(this.products()[0].uid())
    }
    val identifier = if (this.identifier().isNullOrEmpty()) {
        "unknown"
    } else {
        ModelWriter.sanitizeString(this.identifier())
    }
    return if (this.name().isNullOrBlank()) {
        mainProductName
    } else {
        // Create unique name in case of coproduct with multiple processes
        // TODO Review recreate coproducts in same process
        // Same process name with 2 case :
        // * same process name and same product name with different identifier : 2 different locations
        // * same process name and diff product name with same identifier : 2 coproducts
        this.name() + (mainProductName.hashCode() % 10000) + "_" + (identifier.hashCode() % 10000)
    }
}

fun ExchangeRow.uid(): String {
    return this.name()
}

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

        val uid = ModelWriter.sanitizeString(process.uid())
        val metas = mutableMapOf<String, String>()
        process.comment().let { metas["description"] = ModelWriter.compactText(it) }
        process.category().let { metas["category"] = ModelWriter.compactText(it.toString()) }
        process.identifier().let { metas["identifier"] = ModelWriter.compactText(it.toString()) }
        val metaBloc = metas.map { """${it.key} = "${it.value}"""" }

        val baseProducts = process.products().map { render(it) }
        val wasteTreatment =
            if (process.wasteTreatment() == null) listOf() else listOf(render(process.wasteTreatment()))
        val wasteScenario = if (process.wasteScenario() == null) listOf() else listOf(render(process.wasteScenario()))
        val products = baseProducts
            .plus(wasteTreatment)
            .plus(wasteScenario)

        val avoidProducts = process.avoidedProducts().map { render(it) } // C'est quoi ?

        val params = process.calculatedParameters().map { render(it) }.flatten()

        val inputsMatAndFuel = process.materialsAndFuels().map { render(it) }
        val inputsElectricity = process.electricityAndHeat().map { render(it) }

        val emissionsToAir = process.emissionsToAir().map { render(it, "_air") }
        val emissionsToWater = process.emissionsToWater().map { render(it, "_water") }
        val emissionsToSoil = process.emissionsToSoil().map { render(it, "_soil") }
        val emissionsNonMat = process.nonMaterialEmissions().map { render(it, "_non_mat") }
        val emissionsEconomic = process.economicIssues().map { render(it, "_economic") }
        val emissionsSocials = process.socialIssues().map { render(it, "_social") }
        val emissionsFinalWasteFlows = process.finalWasteFlows().map { render(it) }
        val emissionsWasteToTreatment = process.wasteToTreatment().map { render(it) }
        val emissionsRemainingWaste = process.remainingWaste().map { "// QQQ ${it.wasteTreatment()}" }
        val emissionsSeparatedWaste = process.separatedWaste().map { "// QQQ ${it.wasteTreatment()}" }

        val resources = process.resources().map { render(it, "_raw") }

        writer.write(
            "processes/${process.category()}",
            """

process $uid {

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


    private fun render(param: CalculatedParameterRow): List<String> {
        val result = if (param.comment().isNullOrBlank()) emptyList<String>() else listOf(param.comment())
        val amountFormula = param.expression()
        val amount = tryToCompute(amountFormula)
        val unit = "u"
        val uid = ModelWriter.sanitizeString(param.name())
        return result.plus("$uid = $amount $unit // $amountFormula")
    }

    private fun render(exchange: ExchangeRow, suffix: String = ""): String {
        val amountFormula = exchange.amount()
        val amount = tryToCompute(amountFormula.toString())
        val unit = exchange.unit()
        val uid = ModelWriter.sanitizeString(exchange.uid()) + suffix
        return "$amount $unit $uid // $amountFormula"
    }

    private fun tryToCompute(amountFormula: String): Any? {
        return try {
            if (formulaDetector.matches(amountFormula)) {
                engine.eval(amountFormula)
            } else {
                amountFormula
            }
        } catch (e: ScriptException) {
            "// QQQ Invalid regex detector for ${amountFormula}"
        }
    }
}
