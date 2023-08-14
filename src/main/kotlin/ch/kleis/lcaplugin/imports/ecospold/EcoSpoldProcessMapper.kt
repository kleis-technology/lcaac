package ch.kleis.lcaplugin.imports.ecospold

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.imports.ModelWriter.Companion.asComment
import ch.kleis.lcaplugin.imports.ModelWriter.Companion.compactAndPad
import ch.kleis.lcaplugin.imports.ModelWriter.Companion.compactText
import ch.kleis.lcaplugin.imports.ModelWriter.Companion.sanitizeAndCompact
import ch.kleis.lcaplugin.imports.ecospold.EcospoldImporter.Companion.unitToStr
import ch.kleis.lcaplugin.imports.ecospold.model.ActivityDataset
import ch.kleis.lcaplugin.imports.ecospold.model.ElementaryExchange
import ch.kleis.lcaplugin.imports.ecospold.model.IntermediateExchange
import ch.kleis.lcaplugin.imports.ecospold.model.Uncertainty
import ch.kleis.lcaplugin.imports.model.*
import ch.kleis.lcaplugin.imports.simapro.sanitizeSymbol
import ch.kleis.lcaplugin.imports.util.ImportException

class EcoSpoldProcessMapper(
    val process: ActivityDataset,
    private val methodName: String? = null,
) {
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
            return sanitizeAndCompact(activityName + "_" + geo)
        }
    }

    fun map(): ImportedProcess {

        mapMetas()
        mapProducts()
        mapLandUse()
        mapResources()
        mapEmissions()
        mapImpacts()

        return result
    }

    private fun mapProducts() {
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

    private fun mapImpacts() {
        methodName?.let { methodName ->
            result.impactBlocks += mutableListOf(ExchangeBlock(null,
                process.flowData.impactIndicators
                    .filter { it.methodName == methodName }
                    .map {
                        ImportedImpactExchange(
                            it.amount.toString(),
                            sanitizeSymbol(sanitizeAndCompact(it.unitName, toLowerCase = false)),
                            sanitizeAndCompact(it.name),
                            listOf(it.categoryName),
                        )
                    }.toMutableList()
            )
            )
        }
    }

    private fun elementaryExchangeToImportedBioExchange(elementaryExchange: ElementaryExchange): ImportedBioExchange =
        ImportedBioExchange(
            comments = elementaryExchange.comment?.let { listOf(it) } ?: listOf(),
            qty = elementaryExchange.amount.toString(),
            unit = unitToStr(elementaryExchange.unit),
            uid = sanitizeAndCompact(elementaryExchange.name),
            compartment = elementaryExchange.compartment,
            subCompartment = elementaryExchange.subCompartment,
        )

    private fun mapMetas() {
        val metas = result.meta
        process.description.let { description ->
            description.activity.let { activity ->
                activity.id?.let { metas["id"] = compactText(it) }
                activity.name.let { metas["name"] = compactText(it) }
                activity.type.let { metas["type"] = it }
                activity.generalComment?.let {
                    metas["description"] = compactAndPad(toStr(it), 12)
                }
                activity.energyValues?.let { metas["energyValues"] = it }
                activity.includedActivitiesStart?.let {
                    metas["includedActivitiesStart"] = compactText(it)
                }
                activity.includedActivitiesEnd?.let { metas["includedActivitiesEnd"] = compactText(it) }
            }
            description.classifications.forEach { metas[it.system] = compactText(it.value) }
            description.geography?.shortName?.let { metas["geography-shortname"] = compactText(it) }
            description.geography?.comment?.let { metas["geography-comment"] = compactText(toStr(it)) }
        }
    }

    private fun mapProduct(e: IntermediateExchange, geo: String): ImportedProductExchange {
        val initComments = ArrayList<String>()
        e.name?.let { initComments.add(it) }
        e.classifications.forEach { initComments.add("${it.system} = ${it.value}") }
        e.uncertainty?.let { uncertaintyToStr(initComments, it) }
        e.synonyms.forEachIndexed { i, it -> initComments.add("synonym_$i = $it") }
        val amount = e.amount.toString()
        val unit = sanitizeSymbol(sanitizeAndCompact(unitToStr(e.unit)))

        val uid = sanitizeAndCompact("${e.name}_$geo")
        if (e.outputGroup != 0) {
            throw ImportException("Invalid outputGroup for product, expected 0, found ${e.outputGroup}")
        }
        e.properties.forEach { initComments.add("${it.name} ${it.amount} ${it.unit} isCalculatedAmount=${it.isCalculatedAmount ?: ""} isDefiningValue=${it.isDefiningValue ?: ""}") }
        return ImportedProductExchange(amount, unit, uid, 100.0, initComments)
    }


    private fun uncertaintyToStr(comments: ArrayList<String>, it: Uncertainty) {
        it.logNormal?.let { comments.add("// uncertainty: logNormal mean=${it.meanValue}, variance=${it.variance}, mu=${it.mu}") }
        it.pedigreeMatrix?.let { comments.add("// uncertainty: pedigreeMatrix completeness=${it.completeness}, reliability=${it.reliability}, geoCor=${it.geographicalCorrelation}, tempCor=${it.temporalCorrelation}, techCor=${it.furtherTechnologyCorrelation}, ") }
        it.normal?.let { comments.add("// uncertainty: normal mean=${it.meanValue}, variance=${it.variance}, varianceWithPedigreeUncertainty=${it.varianceWithPedigreeUncertainty}, ") }
        it.uniform?.let { comments.add("// uncertainty: uniform minValue=${it.minValue}, maxValue=${it.maxValue}") }
        it.triangular?.let { comments.add("// uncertainty: triangular minValue=${it.minValue}, maxValue=${it.maxValue}, mostLikelyValue=${it.mostLikelyValue}, ") }
        it.comment?.let { comments.add("// uncertainty: comment") }
        it.comment?.let { comments.addAll(asComment(it)) }
    }


}