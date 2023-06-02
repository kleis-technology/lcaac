package ch.kleis.lcaplugin.imports.ecospold.lcai

import spold2.ElementaryExchange
import spold2.IntermediateExchange
import spold2.Parameter
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement

@XmlAccessorType(XmlAccessType.FIELD)
class LcaFlowData {
    @XmlElement(name = "intermediateExchange")
    val intermediateExchanges: List<IntermediateExchange> = ArrayList()

    @XmlElement(name = "elementaryExchange")
    val elementaryExchanges: List<ElementaryExchange> = ArrayList()

    @XmlElement(name = "impactIndicator")
    val impactIndicators: List<LcaImpactIndicator> = ArrayList()

    @XmlElement(name = "parameter")
    val parameters: List<Parameter> = ArrayList()
}