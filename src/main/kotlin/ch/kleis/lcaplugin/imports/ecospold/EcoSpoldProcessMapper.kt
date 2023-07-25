package ch.kleis.lcaplugin.imports.ecospold

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.ecospold.EcospoldImporter.Companion.unitToStr
import ch.kleis.lcaplugin.imports.ecospold.model.ActivityDataset
import ch.kleis.lcaplugin.imports.ecospold.model.ElementaryExchange
import ch.kleis.lcaplugin.imports.ecospold.model.IntermediateExchange
import ch.kleis.lcaplugin.imports.ecospold.model.Uncertainty
import ch.kleis.lcaplugin.imports.model.ExchangeBlock
import ch.kleis.lcaplugin.imports.model.ImportedBioExchange
import ch.kleis.lcaplugin.imports.model.ImportedProcess
import ch.kleis.lcaplugin.imports.model.ImportedProductExchange
import ch.kleis.lcaplugin.imports.simapro.sanitizeSymbol
import ch.kleis.lcaplugin.imports.util.ImportException

open class EcoSpoldProcessMapper(val process: ActivityDataset) {
    private val pUid = uid(process)
    val result = ImportedProcess(pUid)

    companion object {
        fun uid(data: ActivityDataset): String {
            return datasetUid(data.description.activity.name, data.description.geography?.shortName ?: "")
        }

        private fun toStr(txt: List<String>): CharSequence {
            return txt.joinToString("\n")
        }

        private fun datasetUid(activityName: String, geo: String): String {
            return ModelWriter.sanitizeAndCompact(activityName + "_" + geo)
        }
    }

    fun map(): ImportedProcess {

        mapMetas()
        mapProducts()
        mapInputs()
        mapLandUse()
        mapResources()
        mapEmissions()

        return result
    }

    open fun mapProducts() {
        val geo = if (process.description.geography?.shortName == "GLO") ""
        else process.description.geography?.shortName ?: ""
        val products = process.flowData.intermediateExchanges.map { mapProduct(it, geo) }.toMutableList()
        result.productBlocks = mutableListOf(ExchangeBlock("Products", products))
    }

    private fun mapEmissions() {
        result.emissionBlocks += ExchangeBlock(null,
            process.flowData.elementaryExchanges
                .filter { it.substanceType == SubstanceType.EMISSION }
                .map(::elementaryExchangeToImportedBioExchange).toMutableList()
        )

        // TODO: Remove when closing #261
        val bio = ImportedBioExchange(listOf(), "1.0", "u", pUid, "")
        result.emissionBlocks += ExchangeBlock("Virtual Substance for Impact Factors", mutableListOf(bio))

    }

    private fun mapLandUse() {
        result.landUseBlocks = mutableListOf(ExchangeBlock(null,
            process.flowData.elementaryExchanges
                .filter { it.substanceType == SubstanceType.LAND_USE }
                .map(::elementaryExchangeToImportedBioExchange).toMutableList()
        ))
    }

    private fun mapResources() {
        result.resourceBlocks = mutableListOf(ExchangeBlock(null,
            process.flowData.elementaryExchanges
                .filter { it.substanceType == SubstanceType.RESOURCE }
                .map(::elementaryExchangeToImportedBioExchange).toMutableList()
        ))
    }

    private fun elementaryExchangeToImportedBioExchange(elementaryExchange: ElementaryExchange): ImportedBioExchange =
        ImportedBioExchange(
            comments = elementaryExchange.comment?.let { listOf(it) } ?: listOf(),
            qty = elementaryExchange.amount.toString(),
            unit = elementaryExchange.unit,
            uid = ModelWriter.sanitizeAndCompact(elementaryExchange.name),
            compartment = elementaryExchange.compartment,
            subCompartment = elementaryExchange.subCompartment,
        )

    open fun mapInputs() {}

    open fun mapMetas() {
        val metas = result.meta
        process.description.let { description ->
            description.activity.let { activity ->
                activity.id?.let { metas["id"] = ModelWriter.compactText(it) }
                activity.name.let { metas["name"] = ModelWriter.compactText(it) }
                activity.type.let { metas["type"] = it }
                activity.generalComment?.let {
                    metas["description"] = ModelWriter.compactAndPad(toStr(it), 12)
                }
                activity.energyValues?.let { metas["energyValues"] = it }
                activity.includedActivitiesStart?.let {
                    metas["includedActivitiesStart"] = ModelWriter.compactText(it)
                }
                activity.includedActivitiesEnd?.let { metas["includedActivitiesEnd"] = ModelWriter.compactText(it) }
            }
            description.classifications.forEach { metas[it.system] = ModelWriter.compactText(it.value) }
            description.geography?.shortName?.let { metas["geography-shortname"] = ModelWriter.compactText(it) }
            description.geography?.comment?.let { metas["geography-comment"] = ModelWriter.compactText(toStr(it)) }
        }
    }

    private fun mapProduct(e: IntermediateExchange, geo: String): ImportedProductExchange {
        val initComments = ArrayList<String>()
        e.name?.let { initComments.add(it) }
        e.classifications.forEach { initComments.add("${it.system} = ${it.value}") }
        e.uncertainty?.let { uncertaintyToStr(initComments, it) }
        e.synonyms.forEachIndexed { i, it -> initComments.add("synonym_$i = $it") }
        val amount = e.amount.toString()
        val unit = sanitizeSymbol(unitToStr(e.unit))

        val uid = ModelWriter.sanitizeAndCompact("${e.name}_$geo")
        if (e.outputGroup != 0) {
            throw ImportException("Invalid outputGroup for product, expected 0, found ${e.outputGroup}")
        }
        e.properties.forEach { initComments.add("${it.name} ${it.amount} ${it.unit} isCalculatedAmount=${it.isCalculatedAmount ?: ""} isDefiningValue=${it.isDefiningValue ?: ""}") }
        return ImportedProductExchange(initComments, amount, unit, uid, 100.0)
    }


    private fun uncertaintyToStr(comments: ArrayList<String>, it: Uncertainty) {
        it.logNormal?.let { comments.add("// uncertainty: logNormal mean=${it.meanValue}, variance=${it.variance}, mu=${it.mu}") }
        it.pedigreeMatrix?.let { comments.add("// uncertainty: pedigreeMatrix completeness=${it.completeness}, reliability=${it.reliability}, geoCor=${it.geographicalCorrelation}, tempCor=${it.temporalCorrelation}, techCor=${it.furtherTechnologyCorrelation}, ") }
        it.normal?.let { comments.add("// uncertainty: normal mean=${it.meanValue}, variance=${it.variance}, varianceWithPedigreeUncertainty=${it.varianceWithPedigreeUncertainty}, ") }
        it.uniform?.let { comments.add("// uncertainty: uniform minValue=${it.minValue}, maxValue=${it.maxValue}") }
        it.triangular?.let { comments.add("// uncertainty: triangular minValue=${it.minValue}, maxValue=${it.maxValue}, mostLikelyValue=${it.mostLikelyValue}, ") }
        it.comment?.let { comments.add("// uncertainty: comment") }
        it.comment?.let { comments.addAll(ModelWriter.asComment(it)) }
    }


}