package ch.kleis.lcaplugin.imports.ecospold.lcai

import spold2.ActivityDescription
import spold2.AdminInfo
import spold2.UserMasterData
import spold2.Validation
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement

@XmlAccessorType(XmlAccessType.FIELD)
open class LcaDataSet {
    @XmlElement(name = "activityDescription")
    var description: ActivityDescription? = null

    @XmlElement(name = "flowData")
    var flowData: LcaFlowData? = null

    @XmlElement(name = "modellingAndValidation")
    var validation: Validation? = null

    @XmlElement(name = "administrativeInformation")
    var adminInfo: AdminInfo? = null

    @XmlElement(name = "usedUserMasterData", namespace = "http://www.EcoInvent.org/UsedUserMasterData")
    var masterData: UserMasterData? = null
}