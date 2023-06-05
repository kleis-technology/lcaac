package ch.kleis.lcaplugin.imports.ecospold.lcai

import ch.kleis.lcaplugin.imports.ImportException
import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.ecospold.lcai.EcospoldImporter.Companion.unitToStr
import ch.kleis.lcaplugin.imports.ecospold.lcai.model.ActivityDataset
import ch.kleis.lcaplugin.imports.ecospold.lcai.model.IntermediateExchange
import ch.kleis.lcaplugin.imports.ecospold.lcai.model.Uncertainty
import ch.kleis.lcaplugin.imports.model.BioExchangeImported
import ch.kleis.lcaplugin.imports.model.ExchangeBlock
import ch.kleis.lcaplugin.imports.model.ProcessImported
import ch.kleis.lcaplugin.imports.model.ProductImported
import ch.kleis.lcaplugin.imports.simapro.sanitizeUnit

class EcoSpold2ProcessMapper {
    companion object {
        fun map(process: ActivityDataset): ProcessImported {
            val pUid = uid(process)
            val result = ProcessImported(pUid)
            val metas = result.meta

            val geo = if (process.description.geography?.shortName == "GLO") ""
            else process.description.geography?.shortName ?: ""

            mapMetas(process, metas)
            val products = process.flowData.intermediateExchanges.map { mapProduct(it, geo) }.toMutableList()
            result.productBlocks = mutableListOf(ExchangeBlock("Products", products))

            result.emissionBlocks = mapEmission(pUid)

            return result
        }

        private fun mapEmission(pUid: String): MutableList<ExchangeBlock<BioExchangeImported>> {
            val bio = BioExchangeImported(listOf(), "1.0", "u", pUid, "")
            return mutableListOf(ExchangeBlock("Virtual Substance for Impact Factors", mutableListOf(bio)))
        }

        private fun mapProduct(e: IntermediateExchange, geo: String): ProductImported {
            val initComments = ArrayList<String>()
            e.name?.let { initComments.add(it) }
            e.classifications.forEach { initComments.add("${it.system} = ${it.value}") }
            e.uncertainty?.let { uncertaintyToStr(initComments, it) }
            e.synonyms.forEachIndexed { i, it -> initComments.add("synonym_$i = $it") }
            val amount = e.amount.toString()
            val unit = sanitizeUnit(unitToStr(e.unit))

            val uid = ModelWriter.sanitizeAndCompact("${e.name}_$geo")
            if (e.outputGroup != 0) {
                throw ImportException("Invalid outputGroup for product, expected 0, found ${e.outputGroup}")
            }
            e.properties.forEach { initComments.add("${it.name} ${it.amount} ${it.unit} isCalculatedAmount=${it.isCalculatedAmount ?: ""} isDefiningValue=${it.isDefiningValue ?: ""}") }
            return ProductImported(initComments, amount, unit, uid, 100.0)
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

        private fun mapMetas(process: ActivityDataset, metas: MutableMap<String, String?>) {
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

        private fun toStr(txts: List<String>): CharSequence {
            return txts.joinToString("\n")
        }


        fun uid(data: ActivityDataset): String {
            return datasetUid(data.description.activity.name, data.description.geography?.shortName ?: "")
        }

        private fun datasetUid(activityName: String, geo: String): String {
            return ModelWriter.sanitizeAndCompact(activityName + "_" + geo)
        }

    }

}