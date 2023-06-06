package ch.kleis.lcaplugin.imports.ecospold.lcia

import ch.kleis.lcaplugin.imports.ecospold.lcia.model.ActivityDataset
import ch.kleis.lcaplugin.imports.model.ImportedImpact
import ch.kleis.lcaplugin.imports.model.ImportedSubstance

class EcoSpold2SubstanceMapper {
    companion object {
        fun map(process: ActivityDataset, methodName: String): ImportedSubstance {

            val pName = process.description.activity.name
            val meta = mutableMapOf<String, String?>(
                "methodName" to methodName,
                "geography" to (process.description.geography?.shortName ?: "")
            )
            val pUid = EcoSpold2ProcessMapper.uid(process)
            val impacts = process.flowData.impactIndicators
                .filter { it.methodName == methodName }
                .map {
                    ImportedImpact(
                        it.amount,
                        it.unitName,
                        it.name,
                        it.categoryName
                    )
                }.toMutableList()

            return ImportedSubstance(pName, "Emission", "u", "", meta = meta, pUid = pUid, impacts = impacts)
        }
    }

}