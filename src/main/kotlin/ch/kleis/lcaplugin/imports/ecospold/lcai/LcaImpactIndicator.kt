package ch.kleis.lcaplugin.imports.ecospold.lcai

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement

@XmlAccessorType(XmlAccessType.FIELD)
class LcaImpactIndicator {
    @XmlAttribute
    var amount: Double? = null

    @XmlElement(name = "impactMethodName")
    var methodName: String? = null

    @XmlElement(name = "impactCategoryName")
    var categoryName: String? = null

    @XmlElement(name = "name")
    var name: String? = null

    @XmlElement(name = "unitName")
    var unit: String? = null
}