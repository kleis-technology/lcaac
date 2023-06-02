package ch.kleis.lcaplugin.imports.ecospold.lcai

import spold2.DataSet
import spold2.ImpactMethod
import java.io.InputStream
import javax.xml.bind.JAXB
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "ecoSpold")
@XmlAccessorType(XmlAccessType.FIELD)
open class LcaEcospold {
    @XmlElement(name = "activityDataset")
    var dataSet: DataSet? = null

    @XmlElement(name = "childActivityDataset")
    var childDataSet: DataSet? = null
        get() = field
        set(value) {
            field = value
        }

    @XmlElement(name = "impactMethod")
    var impactMethod: ImpactMethod? = null

    companion object {
        fun read(input: InputStream?): LcaEcospold {
            return try {
                JAXB.unmarshal(input, LcaEcospold::class.java)
            } catch (e: Exception) {
                val m = "failed to read EcoSpold 2 document"
                throw RuntimeException(m, e)
            }
        }

    }
}