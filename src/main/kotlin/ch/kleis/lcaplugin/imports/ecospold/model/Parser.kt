package ch.kleis.lcaplugin.imports.ecospold.model

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jdom2.Element
import org.jdom2.JDOMFactory
import org.jdom2.input.SAXBuilder
import org.jdom2.input.sax.SAXHandler
import org.jdom2.input.sax.SAXHandlerFactory
import org.xml.sax.Attributes
import java.io.InputStream

object Parser {
    fun readUnits(stream: InputStream): List<UnitConversion> {
        val builder = getSAXBuilder()
        val root = rootElt(builder, stream)
        fun dimension(d: String): String {
            // Fix Typo in EcoInvent
            return if (d != "lenght") d else "length"
        }

        return root.getChildren("unitConversion")
            .map {
                UnitConversion(
                    it.getAttribute("factor").doubleValue,
                    it.getChildText("unitFromName"),
                    it.getChildText("unitToName"),
                    dimension(it.getChildText("unitType"))
                )
            }
    }

    fun readMethodUnits(stream: InputStream, methodName: String): List<UnitConversion> {
        val builder = getSAXBuilder()
        val root = rootElt(builder, stream)
        fun realName(unitName: String) = if (unitName == "dimensionless") "dimensionless_impact" else unitName

        return root.getChildren("impactMethod").asSequence()
            .filter { it.getChildText("name") == methodName }
            .flatMap { m -> m.getChildren("category") }
            .map { c -> c.getChild("indicator") }
            .map {
                UnitConversion(
                    1.0,
                    realName(it.getChildText("unitName")),
                    "No Ref",
                    realName(it.getChildText("unitName")),
                    it.getChildText("name"),
                )
            }
            .distinctBy { it.fromUnit }
            .toList()
    }


    suspend fun readDataset(stream: InputStream): ActivityDataset = coroutineScope {
        val builder = getSAXBuilder()
        val root = rootElt(builder, stream)
        val xmlDataset = root.getChild("activityDataset") ?: root.getChild("childActivityDataset")

        val description = async { readDescription(xmlDataset.getChild("activityDescription")) }
        val flowData = async { readFlowData(xmlDataset.getChild("flowData")) }
        ActivityDataset(
            description.await(),
            flowData.await(),
        )
    }

    fun readMethodName(stream: InputStream): List<String> {
        val builder = getSAXBuilder()
        val root = rootElt(builder, stream)

        return root.getChildren("impactMethod")
            .map { it.getChildText("name") }
            .sorted()
    }


    private suspend fun readIndicators(indicators: List<Element>): List<ImpactIndicator> = coroutineScope {
        indicators.map {
            async {
                ImpactIndicator.Builder()
                    .amount(it.getAttributeValue("amount").toDouble())
                    .name(it.getChildText("name"))
                    .unitName(it.getChildText("unitName"))
                    .categoryName(it.getChildText("impactCategoryName"))
                    .methodName(it.getChildText("impactMethodName"))
                    .build()
            }
        }.awaitAll()
    }

    private suspend fun readElementaryExchanges(elementaryExchangeList: List<Element>): List<ElementaryExchange> =
        coroutineScope {
            elementaryExchangeList.map {
                async {
                    ElementaryExchange(
                        elementaryExchangeId = it.getAttributeValue("elementaryExchangeId")!!,
                        amount = it.getAttributeValue("amount")!!.toDouble(),
                        name = it.getChildText("name")!!,
                        unit = it.getChildText("unitName")!!,
                        compartment = it.getChild("compartment")!!.getChildText("compartment")!!,
                        subCompartment = it.getChild("compartment")!!.getChildText("subcompartment"),
                        substanceType = readSubstanceType(it),
                        comment = it.getChildText("comment")
                    )
                }
            }.awaitAll()
        }

    private suspend fun readSubstanceType(elementaryExchange: Element): SubstanceType = coroutineScope {
        val outputGroup = async { elementaryExchange.getChildText("outputGroup") }
        val inputGroup = async { elementaryExchange.getChildText("inputGroup") }
        val subCompartment: Deferred<String?> =
            async { elementaryExchange.getChild("compartment")!!.getChildText("subcompartment") }
        when {
            subCompartment.await()?.equals("land") ?: false -> SubstanceType.LAND_USE
            outputGroup.await()?.equals("4") ?: false -> SubstanceType.EMISSION
            inputGroup.await()?.equals("4") ?: false -> SubstanceType.RESOURCE
            else -> throw Error("Invalid input and output group for exchange ${elementaryExchange.getChildText("name")}")
        }
    }


    private suspend fun readFlowData(xmlDesc: Element): FlowData = coroutineScope {
        val intermediateExchangeList = xmlDesc.getChildren("intermediateExchange")
            .map {
                async {
                    IntermediateExchange(
                        amount = it.getAttributeValue("amount").toDouble(),
                        name = it.getChildText("name"),
                        unit = it.getChildText("unitName"),
                        synonyms = it.getChildren("synonym").map { n -> n.value },
                        uncertainty = readUncertainty(it.getChild("uncertainty")),
                        outputGroup = it.getChildText("outputGroup").toInt(),
                        classifications = readClassifications(it.getChildren("classification")),
                        properties = readProperties(it.getChildren("property")),
                    )
                }
            }
        val indicators = readIndicators(xmlDesc.getChildren("impactIndicator"))
        val elementaryExchangeList = readElementaryExchanges(xmlDesc.getChildren("elementaryExchange"))

        FlowData(
            intermediateExchangeList.awaitAll(),
            indicators,
            elementaryExchangeList
        )
    }


    private suspend fun readProperties(children: List<Element>): List<Property> = coroutineScope {
        children.map {
            async {
                Property(
                    it.getChildText("name"),
                    it.getAttributeValue("amount").toDouble(),
                    it.getChildText("unitName"),
                    it.getAttributeValue("isDefiningValue"),
                    it.getAttributeValue("isCalculatedAmount"),
                )
            }
        }.awaitAll()
    }

    private suspend fun readDescription(xmlDesc: Element): ActivityDescription = coroutineScope {
        val activity = async { readActivity(xmlDesc.getChild("activity")) }
        val classifications = async { readClassifications(xmlDesc.getChildren("classification")) }
        val geography = async { readGeography(xmlDesc.getChild("geography")) }
        ActivityDescription(
            activity.await(),
            classifications.await(),
            geography.await()
        )
    }

    private suspend fun readClassifications(children: List<Element>): List<Classification> = coroutineScope {
        children.map {
            async {
                Classification(
                    it.getChildText("classificationSystem"),
                    it.getChildText("classificationValue")
                )
            }
        }.awaitAll()
    }

    private suspend fun readGeography(xml: Element): Geography = coroutineScope {
        val shortName = async { xml.getChildText("shortname") }
        val comment = async { readMultiline(xml.getChild("comment")) }
        Geography(
            shortName.await(),
            comment.await()
        )
    }


    private suspend fun readActivity(xml: Element): Activity = coroutineScope {
        val id = async { xml.getAttributeValue("id") }
        val type = async { xml.getAttributeValue("type") }
        val energyValues = async { xml.getAttributeValue("energyValues") }
        val name = async { xml.getChildText("activityName") }
        val includedActivitiesStart = async { xml.getChildText("includedActivitiesStart") }
        val includedActivitiesEnd = async { xml.getChildText("includedActivitiesEnd") }
        val generalComment = async { readMultiline(xml.getChild("generalComment")) }

        Activity(
            id.await(),
            type.await(),
            energyValues.await(),
            name.await(),
            includedActivitiesStart.await(),
            includedActivitiesEnd.await(),
            generalComment.await()
        )
    }

    private fun readMultiline(xml: Element?): List<String> {
        return xml?.getChildren("text")
            ?.sortedBy { it.getAttribute("index").intValue }
            ?.map { it.text }
            ?: listOf()
    }

    private fun rootElt(builder: SAXBuilder, stream: InputStream): Element {
        val doc = builder.build(stream)
        return doc.rootElement
    }


    /** Get a `SAXBuilder` that ignores namespaces.
     * Any namespaces present in the xml input to this builder will be omitted from the resulting `Document`.  */
    private fun getSAXBuilder(): SAXBuilder {
        val saxBuilder = SAXBuilder()

        saxBuilder.saxHandlerFactory = FACTORY
        return saxBuilder
    }

    private val FACTORY: SAXHandlerFactory = object : SAXHandlerFactory {
        override fun createSAXHandler(factory: JDOMFactory?): SAXHandler {
            return object : SAXHandler() {
                override fun startElement(
                    namespaceURI: String?, localName: String?, qName: String?, atts: Attributes?
                ) {
                    super.startElement("", localName, qName, atts)
                }

                override fun startPrefixMapping(prefix: String?, uri: String?) {
                    return
                }
            }
        }
    }

    private suspend fun readUncertainty(maybeXML: Element?): Uncertainty? = maybeXML?.let { xml ->
        coroutineScope {

            val logNormal: Deferred<LogNormal?> = async {
                xml.getChild("lognormal")?.let {
                    LogNormal(
                        it.getAttributeValue("meanValue").toDouble(),
                        it.getAttributeValue("mu").toDouble(),
                        it.getAttributeValue("variance").toDouble(),
                        it.getAttributeValue("varianceWithPedigreeUncertainty").toDouble()
                    )
                }
            }
            val normal: Deferred<Normal?> = async {
                xml.getChild("normal")?.let {
                    Normal(
                        it.getAttributeValue("meanValue").toDouble(),
                        it.getAttributeValue("variance").toDouble(),
                        it.getAttributeValue("varianceWithPedigreeUncertainty").toDouble(),
                    )
                }
            }
            val triangular: Deferred<Triangular?> = async {
                xml.getChild("triangular")?.let {
                    Triangular(
                        it.getAttributeValue("minValue").toDouble(),
                        it.getAttributeValue("mostLikelyValue").toDouble(),
                        it.getAttributeValue("maxValue").toDouble(),
                    )
                }
            }
            val uniform: Deferred<Uniform?> = async {
                xml.getChild("uniform")?.let {
                    Uniform(
                        it.getAttributeValue("minValue").toDouble(),
                        it.getAttributeValue("maxValue").toDouble(),
                    )
                }
            }
            val undefined: Deferred<UndefinedUncertainty?> = async {
                xml.getChild("undefined")?.let {
                    UndefinedUncertainty(
                        it.getAttributeValue("minValue").toDouble(),
                        it.getAttributeValue("maxValue").toDouble(),
                        it.getAttributeValue("standardDeviation95").toDouble(),
                    )
                }
            }
            val pedigreeMatrix: Deferred<PedigreeMatrix?> = async {
                xml.getChild("pedigreeMatrix")?.let {
                    PedigreeMatrix(
                        it.getAttributeValue("reliability").toInt(),
                        it.getAttributeValue("completeness").toInt(),
                        it.getAttributeValue("temporalCorrelation").toInt(),
                        it.getAttributeValue("geographicalCorrelation").toInt(),
                        it.getAttributeValue("furtherTechnologyCorrelation").toInt()
                    )
                }
            }
            val comment: String? = xml.getChildText("comment")

            Uncertainty(
                logNormal.await(),
                normal.await(),
                triangular.await(),
                uniform.await(),
                undefined.await(),
                pedigreeMatrix.await(),
                comment
            )
        }
    }

}