package ch.kleis.lcaplugin.imports.ecospold.lcai

import ch.kleis.lcaplugin.imports.ImportException
import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.ecospold.lcai.EcospoldImporter.Companion.unitToStr
import ch.kleis.lcaplugin.imports.model.BioExchangeImported
import ch.kleis.lcaplugin.imports.model.ExchangeBlock
import ch.kleis.lcaplugin.imports.model.ProcessImported
import ch.kleis.lcaplugin.imports.model.ProductImported
import ch.kleis.lcaplugin.imports.simapro.sanitizeUnit
import spold2.DataSet
import spold2.IntermediateExchange
import spold2.RichText
import spold2.Uncertainty

class EcoSpold2ProcessMapper {
    companion object {
        fun map(process: DataSet): ProcessImported {
            val pUid = uid(process)
            val result = ProcessImported(pUid)
            val metas = result.meta

            val geo = if (process.description?.geography?.shortName == "GLO") ""
            else process.description?.geography?.shortName ?: ""

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
            e.classifications?.forEach { initComments.add("${it.system} = ${it.value}") }
            e.uncertainty?.let { uncertaintyToStr(initComments, it) }
            val amount = e.amount.toString()
            val unit = sanitizeUnit(unitToStr(e.unit))

            val uid = ModelWriter.sanitizeAndCompact("${e.name}_$geo")
            if (e.outputGroup != 0) {
                throw ImportException("Invalid outputGroup for product, expected 0, found ${e.outputGroup}")
            }
            return ProductImported(initComments, amount, unit, uid, 100.0)
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

        private fun mapMetas(process: DataSet, metas: MutableMap<String, String?>) {
            process.description?.let { description ->
                description.activity?.let { activity ->
                    activity.id?.let { metas["id"] = ModelWriter.compactText(it) }
                    activity.name?.let { metas["name"] = ModelWriter.compactText(it) }
                    activity.synonyms?.forEachIndexed { i, it -> metas["synonym_$i"] = ModelWriter.compactText(it) }
                    activity.generalComment?.let {
                        metas["description"] = ModelWriter.compactAndPad(toStr(it), 12)
                    }
                    activity.energyValues?.let { metas["energyValues"] = it.toString() }
                    activity.includedActivitiesStart?.let {
                        metas["includedActivitiesStart"] = ModelWriter.compactText(it)
                    }
                    activity.includedActivitiesEnd?.let { metas["includedActivitiesEnd"] = ModelWriter.compactText(it) }
                }
                description.classifications?.forEach { metas[it.system] = ModelWriter.compactText(it.value) }
                description.geography?.shortName?.let { metas["geography-shortname"] = ModelWriter.compactText(it) }
                description.geography?.comment?.let { metas["geography-comment"] = ModelWriter.compactText(toStr(it)) }
            }
        }

        private fun toStr(txt: RichText?): CharSequence {
            return txt?.texts?.joinToString("\n") { it.value } ?: ""
        }


        private fun uid(data: DataSet): String {
            return datasetUid(data.description!!.activity.name, data.description!!.geography?.shortName ?: "")
        }

        private fun datasetUid(activityName: String, geo: String): String {
            return ModelWriter.sanitizeAndCompact(activityName + "_" + geo)
        }

    }

}