package ch.kleis.lcaplugin.imports.ecospold

import ch.kleis.lcaplugin.imports.FormulaConverter
import ch.kleis.lcaplugin.imports.Line
import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.ModelWriter.Companion.createCommentLine
import ch.kleis.lcaplugin.imports.Text
import ch.kleis.lcaplugin.imports.ecospold.EcospoldImporter.Companion.unitToStr
import ch.kleis.lcaplugin.imports.ecospold.EcospoldImporter.ProcessDictRecord
import ch.kleis.lcaplugin.imports.simapro.sanitizeUnit
import com.intellij.openapi.diagnostic.Logger
import spold2.*
import java.io.File

const val LAND_OR_RESOURCE_COMPARTMENT = "natural resource"
const val LAND_SUBCOMPARTMENT = "land"

class ProcessRenderer {
    companion object {
        private val LOG = Logger.getInstance(ProcessRenderer::class.java)
    }

    var nbProcesses: Int = 0
        private set
    var processDict: Map<String, ProcessDictRecord>? = null

    fun render(data: DataSet, writer: ModelWriter, processComment: String?) {
        nbProcesses++
        val pUid = uid(data)
        val metas = mutableMapOf<String, String>()
        data.description?.let { description ->
            description.activity?.let { activity ->
                activity.id?.let { metas["id"] = ModelWriter.compactText(it) }
                activity.name?.let { metas["name"] = ModelWriter.compactText(it) }
                activity.synonyms?.forEachIndexed { i, it -> metas["synonym_$i"] = ModelWriter.compactText(it) }
                activity.generalComment?.let {
                    metas["description"] = ModelWriter.compactAndPad(toStr(it), 12)
                }
                activity.energyValues?.let { metas["energyValues"] = it.toString() }
                activity.includedActivitiesStart?.let { metas["includedActivitiesStart"] = ModelWriter.compactText(it) }
                activity.includedActivitiesEnd?.let { metas["includedActivitiesEnd"] = ModelWriter.compactText(it) }
            }
            description.classifications?.forEach { metas[it.system] = ModelWriter.compactText(it.value) }
            description.geography?.shortName?.let { metas["geography-shortname"] = ModelWriter.compactText(it) }
            description.geography?.comment?.let { metas["geography-comment"] = ModelWriter.compactText(toStr(it)) }
        }
        val geo = ""
//            if (data.description?.geography?.shortName == "GLO") ""
//            else data.description?.geography?.shortName ?: ""

        // TODO Finish Header
        val metaBloc = metas.map { """"${it.key}" = "${it.value}"""" }
        val category = category(data)
        val subFolder = if (category == null) "" else "${category}${File.separatorChar}"

        val params = listOf<CharSequence>() // TODO

        data.validation // TODO

        val technosphere = technosphere(data.flowData.intermediateExchanges, geo)
//            .filter { it.classifications.filter{cl->isProduct(cl)}.isEmpty() }
//            .groupingBy { it. }
        val biosphere = biosphere(data.flowData.elementaryExchanges)

        writer.write(
            "processes${File.separatorChar}$subFolder$pUid.lca",
            """

${if (processComment != null) "// $processComment" else ""}
process $pUid {

${ModelWriter.block("meta {", metaBloc)}

${ModelWriter.optionalBlock("variables {", params)}
${renderTechnosphere(technosphere)}
${renderBiosphere(biosphere)}


}
""", index = true, closeAfterWrite = true
        )
    }


    private fun toStr(txt: RichText?): CharSequence {
        return txt?.texts?.joinToString("\n") { it.value } ?: ""
    }

    enum class TechnoType(val value: Int, val text: String) : Comparable<TechnoType> {
        PRODUCT(0, "products"), INPUT(1, "inputs")
    }

    data class TechnoCategory(val type: TechnoType, val priority: Int)

    enum class BiosphereType(val value: Int, val text: String) : Comparable<BiosphereType> {
        EMISSION(0, "emissions"), LANDUSE(1, "land_use"), RESOURCES(2, "resources")
    }

    data class BioCategory(val type: BiosphereType, val priority: Int, val subType: String?)


    private fun renderTechnosphere(technosphere: List<Pair<TechnoCategory, List<Text>>>): CharSequence {
        val builder = StringBuilder()
        technosphere.map { (cat, exchange) -> ModelWriter.blockText("${cat.type.text} {", exchange) }
            .forEach { builder.append(it).append("\n\n") }
        return builder
    }

    private fun renderBiosphere(biosphere: List<Pair<BioCategory, List<Text>>>): CharSequence {
        val builder = StringBuilder()
        biosphere.map { (cat, exchange) -> ModelWriter.blockText("${cat.type.text} {", exchange) }
            .forEach { builder.append(it).append("\n\n") }
        return builder
    }

    private fun technosphere(flows: List<IntermediateExchange>, geo: String): List<Pair<TechnoCategory, List<Text>>> {
        fun key(techno: IntermediateExchange): TechnoCategory {
            return when {
                techno.outputGroup != null -> TechnoCategory(TechnoType.PRODUCT, -techno.outputGroup)
                else -> TechnoCategory(TechnoType.INPUT, -techno.inputGroup)
            }
        }
        return flows
            .groupBy({ key(it) }, { renderTechnoExchange(it, geo) })
            .entries
            .map { e -> e.key to e.value }
            .sortedWith(compareBy({ it.first.type.value }, { it.first.priority }))
    }

    private fun biosphere(flows: List<ElementaryExchange>): List<Pair<BioCategory, List<Text>>> {
        fun key(bio: ElementaryExchange): BioCategory {
            return when {
                bio.inputGroup != null && bio.compartment?.compartment == LAND_OR_RESOURCE_COMPARTMENT && bio.compartment?.subCompartment == LAND_SUBCOMPARTMENT
                -> BioCategory(BiosphereType.LANDUSE, 5, null)

                bio.inputGroup != null && bio.compartment?.compartment == LAND_OR_RESOURCE_COMPARTMENT
                -> BioCategory(BiosphereType.RESOURCES, 4, bio.compartment?.subCompartment)

                bio.inputGroup != null
                -> BioCategory(BiosphereType.RESOURCES, 3, "QQQ_unknown_" + bio.compartment?.subCompartment)

                else -> BioCategory(BiosphereType.EMISSION, bio.outputGroup, bio.compartment?.compartment)
            }
        }
        return flows
            .groupBy({ key(it) }, { renderBiosphereExchange(it) })
            .entries
            .map { e -> e.key to e.value }
            .sortedWith(compareBy({ it.first.type.value }, { -it.first.priority }))
    }

    private fun renderBiosphereExchange(biosphere: ElementaryExchange): List<Line> {
        val comments = ArrayList<String>()
        biosphere.name?.let { comments.add("// name: $it") }
        biosphere.comment?.let { comments.addAll(ModelWriter.asComment(it)) }
        biosphere.formula?.let { comments.addAll(ModelWriter.asComment(it)) }
        biosphere.compartment.compartment?.let { comments.add("// compartment: $it") }
        biosphere.compartment.subCompartment?.let { comments.add("// subcompartment: $it") }
        biosphere.casNumber?.let { comments.add("// casNumber: $it") }
        biosphere.flowId?.let { comments.add("// flowId: $it") }
        biosphere.mathematicalRelation?.let { comments.add("// mathematicalRelation: $it") }
        biosphere.isCalculated?.let { comments.add("// isCalculated: $it") }
        biosphere.uncertainty?.let { uncertaintyToStr(comments, it) }
        biosphere.variableName?.let { comments.add("// variableName: $it") }

        val amountFormula = biosphere.amount.toString()
        val (amount, changed) = FormulaConverter.compute(amountFormula)
        val unit = sanitizeUnit(unitToStr(biosphere.unit))
        val uid = uid(biosphere)
        val endingComment = createCommentLine(listOf(if (changed) "Formula=[$amountFormula]" else ""))
        return comments
            .plus("$amount $unit $uid $endingComment")
    }

    private fun uncertaintyToStr(comments: ArrayList<String>, it: Uncertainty) {
        it.logNormal?.let { comments.add("// uncertainty: logNormal mean=${it.meanValue}, variance=${it.variance}, mu=${it.mu}") }
        it.pedigreeMatrix?.let { comments.add("// uncertainty: pedigreeMatrix completeness=${it.completeness}, reliability=${it.reliability}, geoCor=${it.geographicalCorrelation}, tempCor=${it.temporalCorrelation}, techCor=${it.technologyCorrelation}, ") }
        it.normal?.let { comments.add("// uncertainty: normal mean=${it.meanValue}, variance=${it.variance}, varianceWithPedigreeUncertainty=${it.varianceWithPedigreeUncertainty}, ") }
        it.uniform?.let { comments.add("// uncertainty: uniform minValue=${it.minValue}, maxValue=${it.maxValue}") }
        it.triangular?.let { comments.add("// uncertainty: triangular minValue=${it.minValue}, maxValue=${it.maxValue}, mostLikelyValue=${it.mostLikelyValue}, ") }
        it.comment?.let { comments.add("// uncertainty: comment") }
        it.comment?.let { comments.addAll(ModelWriter.asComment(it)) }
    }

    private fun uid(bio: ElementaryExchange): String {
        return ModelWriter.sanitizeAndCompact("${bio.name}_${bio.compartment?.compartment}_${bio.compartment?.subCompartment}")
    }

    private fun renderTechnoExchange(techno: IntermediateExchange, geo: String): List<Line> {
        val comments = ArrayList<String>()
        techno.name?.let { comments.add("// name: $it") }
        techno.classifications?.forEach { comments.add("// classification : ${it.system} = ${it.value}") }
        techno.comment?.let { comments.addAll(ModelWriter.asComment(it)) }
        techno.uncertainty?.let { uncertaintyToStr(comments, it) }

        val amountFormula = techno.amount.toString()
        val (amount, changed) = FormulaConverter.compute(amountFormula)
        val unit = sanitizeUnit(unitToStr(techno.unit))
        val uid = uid(techno, geo)
        val (allocation, from) =
            if (techno.outputGroup != null) {
                Pair("allocate 100 percent", "")  // TODO Comment Allocation est géré dans Ecospold ?
            } else {
                val refProc = processDict?.get(techno.activityLinkId)
                if (refProc == null) {
                    LOG.warn("Unable to find reference process [${techno.activityLinkId}] for product [${techno.id}]")
                    Pair("", "")
                } else {
                    val refProcUid = datasetUid(refProc.processName, refProc.geo)
                    Pair("", "from ${refProcUid}()")
                }
            }
        val endingComment = createCommentLine(listOf(if (changed) "Formula=[$amountFormula]" else ""))
        return comments
            .plus("$amount $unit $uid $allocation $from $endingComment")
    }

    private fun uid(techno: IntermediateExchange, geo: String): String {
        return if (techno.outputGroup != null)
            ModelWriter.sanitizeAndCompact("${techno.name}_$geo")
        else
            ModelWriter.sanitizeAndCompact(techno.name)
    }


    private fun uid(data: DataSet): String {
        return datasetUid(data.description.activity.name, data.description.geography?.shortName ?: "")
    }

    private fun datasetUid(activityName: String, geo: String): String {
        return ModelWriter.sanitizeAndCompact(activityName + "_" + geo)
    }

    private fun category(data: DataSet): String? {
        val desc = data.description.classifications
            .firstOrNull { it.system == "EcoSpold01Categories" }
            ?.value
        return desc?.let { ModelWriter.sanitizeAndCompact(it) }
    }

}