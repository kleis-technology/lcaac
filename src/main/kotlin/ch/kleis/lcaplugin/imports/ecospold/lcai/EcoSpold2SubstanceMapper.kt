package ch.kleis.lcaplugin.imports.ecospold.lcai

import ch.kleis.lcaplugin.imports.ecospold.lcai.model.ActivityDataset
import ch.kleis.lcaplugin.imports.model.ImpactImported
import ch.kleis.lcaplugin.imports.model.SubstanceImported

class EcoSpold2SubstanceMapper {
    companion object {
        fun map(process: ActivityDataset, methodName: String): SubstanceImported {

            val pName = process.description.activity.name
            val meta = mutableMapOf<String, String?>(
                "methodName" to methodName,
                "geography" to (process.description.geography?.shortName ?: "")
            )
            val pUid = EcoSpold2ProcessMapper.uid(process)
            val impacts = process.flowData.impactIndicators
                .filter { it.methodName == methodName }
                .map {
                    ImpactImported(
                        it.amount,
                        it.unitName,
                        it.name
                    )
                }.toMutableList()

            return SubstanceImported(pName, "Emission", "u", "", meta = meta, pUid = pUid, impacts = impacts)
        }
    }

}