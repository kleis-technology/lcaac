package ch.kleis.lcaplugin.imports.ecospold.model

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
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


    fun readDataset(stream: InputStream): ActivityDataset {
        val builder = getSAXBuilder()
        val root = rootElt(builder, stream)
        val xmlDataset = root.getChild("activityDataset") ?: root.getChild("childActivityDataset")

        return ActivityDataset(
            readDescription(xmlDataset.getChild("activityDescription")),
            readFlowData(xmlDataset.getChild("flowData"))
        )
    }

    fun readMethodName(stream: InputStream): List<String> {
        val builder = getSAXBuilder()
        val root = rootElt(builder, stream)

        return root.getChildren("impactMethod")
            .map { it.getChildText("name") }
            .sorted()
    }


    private fun readIndicators(indicators: Sequence<Element>): Sequence<ImpactIndicator> {
        return indicators.map {
            ImpactIndicator(
                amount = it.getAttributeValue("amount").toDouble(),
                name = it.getChildText("name"),
                unitName = it.getChildText("unitName"),
                categoryName = it.getChildText("impactCategoryName"),
                methodName = it.getChildText("impactMethodName"),
            )
        }
    }

    private fun readElementaryExchanges(elementaryExchangeList: List<Element>): Sequence<ElementaryExchange> =
        elementaryExchangeList.asSequence().map {
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

    private fun readSubstanceType(elementaryExchange: Element): SubstanceType {
        val outputGroup = elementaryExchange.getChildText("outputGroup")
        val inputGroup = elementaryExchange.getChildText("inputGroup")
        val subCompartment: String? = elementaryExchange.getChild("compartment")!!.getChildText("subcompartment")
        return when {
            subCompartment?.equals("land") ?: false -> SubstanceType.LAND_USE
            outputGroup?.equals("4") ?: false -> SubstanceType.EMISSION
            inputGroup?.equals("4") ?: false -> SubstanceType.RESOURCE
            else -> throw Error("Invalid input and output group for exchange ${elementaryExchange.getChildText("name")}")
        }
    }


    private fun readFlowData(xmlDesc: Element): FlowData {
        val intermediateExchangeList = xmlDesc.getChildren("intermediateExchange")
            .asSequence()
            .map {
                IntermediateExchange(
                    amount = it.getAttributeValue("amount").toDouble(),
                    name = it.getChildText("name"),
                    unit = it.getChildText("unitName"),
                    synonyms = it.getChildren("synonym").map { n -> n.value },
                    uncertainty = readUncertainty(it.getChild("uncertainty")),
                    outputGroup = it.getChildText("outputGroup")?.toInt(),
                    inputGroup = it.getChildText("inputGroup")?.toInt(),
                    activityLinkId = it.getAttributeValue("activityLinkId"),
                    classifications = readClassifications(it.getChildren("classification")),
                    properties = readProperties(it.getChildren("property")),
                )
            }
        val indicators = readIndicators(xmlDesc.getChildren("impactIndicator").asSequence())
        val elementaryExchangeList = readElementaryExchanges(xmlDesc.getChildren("elementaryExchange"))

        return FlowData(intermediateExchangeList, indicators, elementaryExchangeList)
    }


    private fun readProperties(children: List<Element>): List<Property> {
        return children.map {
            Property(
                it.getChildText("name"),
                it.getAttributeValue("amount").toDouble(),
                it.getChildText("unitName"),
                it.getAttributeValue("isDefiningValue"),
                it.getAttributeValue("isCalculatedAmount"),
            )
        }

    }

    private fun readDescription(xmlDesc: Element): ActivityDescription {
        return ActivityDescription(
            activity = readActivity(xmlDesc.getChild("activity")),
            classifications = readClassifications(xmlDesc.getChildren("classification")),
            geography = readGeography(xmlDesc.getChild("geography")),
        )
    }

    private fun readClassifications(children: List<Element>): List<Classification> {
        return children.map {
            Classification(
                it.getChildText("classificationSystem"),
                it.getChildText("classificationValue")
            )
        }
    }

    private fun readGeography(xml: Element): Geography {
        return Geography(
            xml.getChildText("shortname"),
            readMultiline(xml.getChild("comment"))
        )
    }


    private fun readActivity(xml: Element): Activity {
        return Activity(
            id = xml.getAttributeValue("id"),
            type = xml.getAttributeValue("type"),
            energyValues = xml.getAttributeValue("energyValues"),
            name = xml.getChildText("activityName"),
            includedActivitiesStart = xml.getChildText("includedActivitiesStart"),
            includedActivitiesEnd = xml.getChildText("includedActivitiesEnd"),
            generalComment = readMultiline(xml.getChild("generalComment")),
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

    private fun readUncertainty(xml: Element?): Uncertainty? {
        if (xml == null) return null

        val logNormal: LogNormal? = xml.getChild("lognormal")?.let {
            LogNormal(
                it.getAttributeValue("meanValue").toDouble(),
                it.getAttributeValue("mu").toDouble(),
                it.getAttributeValue("variance").toDouble(),
                it.getAttributeValue("varianceWithPedigreeUncertainty").toDouble()
            )
        }
        val normal: Normal? = xml.getChild("normal")?.let {
            Normal(
                it.getAttributeValue("meanValue").toDouble(),
                it.getAttributeValue("variance").toDouble(),
                it.getAttributeValue("varianceWithPedigreeUncertainty").toDouble(),
            )
        }
        val triangular: Triangular? = xml.getChild("triangular")?.let {
            Triangular(
                it.getAttributeValue("minValue").toDouble(),
                it.getAttributeValue("mostLikelyValue").toDouble(),
                it.getAttributeValue("maxValue").toDouble(),
            )
        }
        val uniform: Uniform? = xml.getChild("uniform")?.let {
            Uniform(
                it.getAttributeValue("minValue").toDouble(),
                it.getAttributeValue("maxValue").toDouble(),
            )
        }
        val undefined: UndefinedUncertainty? = xml.getChild("undefined")?.let {
            UndefinedUncertainty(
                it.getAttributeValue("minValue").toDouble(),
                it.getAttributeValue("maxValue").toDouble(),
                it.getAttributeValue("standardDeviation95").toDouble(),
            )
        }
        val pedigreeMatrix: PedigreeMatrix? = xml.getChild("pedigreeMatrix")?.let {
            PedigreeMatrix(
                it.getAttributeValue("reliability").toInt(),
                it.getAttributeValue("completeness").toInt(),
                it.getAttributeValue("temporalCorrelation").toInt(),
                it.getAttributeValue("geographicalCorrelation").toInt(),
                it.getAttributeValue("furtherTechnologyCorrelation").toInt()
            )
        }
        val comment: String? = xml.getChildText("comment")
        return Uncertainty(logNormal, normal, triangular, uniform, undefined, pedigreeMatrix, comment)
    }

}