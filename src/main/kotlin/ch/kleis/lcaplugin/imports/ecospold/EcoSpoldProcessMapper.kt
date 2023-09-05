package ch.kleis.lcaplugin.imports.ecospold

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.imports.ModelWriter.Companion.asComment
import ch.kleis.lcaplugin.imports.ModelWriter.Companion.compactAndPad
import ch.kleis.lcaplugin.imports.ModelWriter.Companion.compactText
import ch.kleis.lcaplugin.imports.ModelWriter.Companion.sanitizeAndCompact
import ch.kleis.lcaplugin.imports.ecospold.EcospoldImporter.Companion.unitToStr
import ch.kleis.lcaplugin.imports.ecospold.model.*
import ch.kleis.lcaplugin.imports.model.*
import ch.kleis.lcaplugin.imports.simapro.sanitizeSymbol
import ch.kleis.lcaplugin.imports.util.ImportException

object EcoSpoldProcessMapper {
    fun map(
        process: ActivityDataset,
        processDict: Map<String, EcospoldImporter.ProcessDictRecord>,
        methodName: String? = null
    ): ImportedProcess {
        val elementaryExchangeGrouping = process.flowData.elementaryExchanges.groupingBy {
            it.substanceType
        }.aggregate { _, accumulator: MutableList<ImportedBioExchange>?, element: ElementaryExchange, _ ->
            val mappedExchange = elementaryExchangeToImportedBioExchange(element)
            accumulator?.let {
                accumulator.add(mappedExchange)
                accumulator
            } ?: mutableListOf(mappedExchange)
        }

        val mappedIntermediateExchanges = process.flowData.intermediateExchanges.map { intermediateExchange ->
            intermediateExchangeToImportedTechnosphereExchange(
                intermediateExchange,
                processDict,
            )
        }.groupBy {
            when (it) {
                is ImportedProductExchange -> ImportedProductExchange
                is ImportedInputExchange -> ImportedInputExchange
            }
        }

        val (mappedImpactsComment, mappedImpactsExchanges) = methodName?.let {
            mapImpacts(methodName, process.flowData.impactIndicators)
        } ?: (null to emptySequence())

        return ImportedProcess(
            uid = uid(process),
            meta = mapMetas(process.description),
            productBlocks = listOfNonEmptyExchangeBlock(
                mappedIntermediateExchanges[ImportedProductExchange]
                    ?.asSequence()
                    ?.map { it as ImportedProductExchange }
            ),
            inputBlocks = listOfNonEmptyExchangeBlock(
                mappedIntermediateExchanges[ImportedInputExchange]
                    ?.asSequence()
                    ?.map { it as ImportedInputExchange }
            ),
            emissionBlocks = listOfNonEmptyExchangeBlock(
                elementaryExchangeGrouping[SubstanceType.EMISSION]?.asSequence()
            ),
            resourceBlocks = listOfNonEmptyExchangeBlock(
                elementaryExchangeGrouping[SubstanceType.RESOURCE]?.asSequence()
            ),
            landUseBlocks = listOfNonEmptyExchangeBlock(
                elementaryExchangeGrouping[SubstanceType.LAND_USE]?.asSequence()
            ),
            impactBlocks = listOfNonEmptyExchangeBlock(
                mappedImpactsExchanges, mappedImpactsComment
            ),
        )
    }

    fun uid(data: ActivityDataset): String {
        return datasetUid(data.description.activity.name, data.description.geography?.shortName ?: "")
    }

    private fun mapMetas(description: ActivityDescription): Map<String, String?> =
        mapOf("id" to description.activity.id?.let { compactText(it) },
            "name" to description.activity.name.let { compactText(it) },
            "type" to description.activity.type,
            "description" to description.activity.generalComment?.let { compactAndPad(toStr(it), 12) },
            "energyValues" to description.activity.energyValues,
            "includedActivitiesStart" to description.activity.includedActivitiesStart?.let { compactText(it) },
            "includedActivitiesEnd" to description.activity.includedActivitiesEnd?.let { compactText(it) },
            "geography-shortname" to description.geography?.shortName?.let { compactText(it) },
            "geography-comment" to description.geography?.comment?.let { compactText(toStr(it)) }) + description.classifications.associate {
            it.system to compactText(
                it.value
            )
        }

    private fun mapImpacts(
        methodName: String,
        impactIndicatorList: Sequence<ImpactIndicator>,
    ): Pair<String, Sequence<ImportedImpactExchange>> =
        Pair("Impacts for method $methodName",
            impactIndicatorList.filter { it.methodName == methodName }.map {
                ImportedImpactExchange(
                    it.amount.toString(),
                    sanitizeSymbol(sanitizeAndCompact(it.unitName, toLowerCase = false)),
                    sanitizeAndCompact(it.name),
                    listOf(it.categoryName),
                )
            })

    private fun intermediateExchangeToImportedTechnosphereExchange(
        e: IntermediateExchange,
        processDict: Map<String, EcospoldImporter.ProcessDictRecord>,
    ): ImportedTechnosphereExchange {
        val uid = sanitizeAndCompact(e.name)
        val amount = e.amount.toString()
        val unit = sanitizeSymbol(sanitizeAndCompact(unitToStr(e.unit), false))
        val comments = buildIntermediateExchangeComments(e)

        when {
            e.outputGroup != null -> {
                if (e.outputGroup != 0) {
                    throw ImportException("Invalid outputGroup for product, expected 0, found ${e.outputGroup}")
                }

                return ImportedProductExchange(amount, unit, uid, 100.0, comments)
            }

            e.inputGroup != null -> {
                when (e.inputGroup) {
                    1, 2, 3, 5 -> {
                        val fromProcess = e.activityLinkId?.let {
                            processDict[e.activityLinkId]
                        }
                        val fromProcessName = fromProcess?.let {
                            sanitizeAndCompact("${it.processName}_${it.geo}")
                        }
                        return ImportedInputExchange(
                            uid = uid,
                            qty = amount,
                            unit = unit,
                            fromProcess = fromProcessName,
                            comments = comments,
                        )
                    }

                    else -> throw ImportException("Invalid inputGroup for intermediateExchange, expected in {1, 2, 3, 5}, found ${e.inputGroup}")
                }
            }

            else -> throw ImportException("Intermediate exchange without inputGroup or outputGroup")
        }
    }

    private fun buildIntermediateExchangeComments(
        e: IntermediateExchange,
    ): List<String> {
        val initComments = ArrayList<String>()
        initComments.add(e.name)
        e.classifications.forEach { initComments.add("${it.system} = ${it.value}") }
        e.uncertainty?.let { uncertaintyToStr(initComments, it) }
        e.synonyms.forEachIndexed { i, it -> initComments.add("synonym_$i = $it") }
        e.properties.forEach { initComments.add("${it.name} ${it.amount} ${it.unit} isCalculatedAmount=${it.isCalculatedAmount ?: ""} isDefiningValue=${it.isDefiningValue ?: ""}") }
        return initComments
    }

    private fun elementaryExchangeToImportedBioExchange(elementaryExchange: ElementaryExchange): ImportedBioExchange =
        ImportedBioExchange(
            comments = elementaryExchange.comment?.let { listOf(it) } ?: listOf(),
            qty = elementaryExchange.amount.toString(),
            unit = unitToStr(elementaryExchange.unit),
            uid = sanitizeAndCompact(elementaryExchange.name),
            compartment = elementaryExchange.compartment,
            subCompartment = elementaryExchange.subCompartment,
            printAsComment = elementaryExchange.printAsComment,
        )

    private fun <E : ImportedExchange> listOfNonEmptyExchangeBlock(
        exchanges: Sequence<E>?,
        comment: String? = null,
    ): List<ExchangeBlock<E>> =
        // Testing if a sequence is empty is weird. count() will collect it, and there is no isEmpty ...
        exchanges?.firstOrNull()?.let {
            listOf(
                ExchangeBlock(
                    comment = comment,
                    exchanges = exchanges
                )
            )
        } ?: listOf()

    private fun datasetUid(activityName: String, geo: String): String = sanitizeAndCompact(activityName + "_" + geo)

    private fun toStr(txt: List<String>): CharSequence = txt.joinToString("\n")

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