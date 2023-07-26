package ch.kleis.lcaplugin.imports.ecospold

import ch.kleis.lcaplugin.imports.ecospold.model.ActivityDataset
import ch.kleis.lcaplugin.imports.model.ImportedImpact
import ch.kleis.lcaplugin.imports.model.ImportedSubstance

// Jo: AFAIK, this is only used because of the virtual substance required by #261. Remove after close.
class EcoSpoldSubstanceMapper {
    companion object {
        fun map(process: ActivityDataset, methodName: String): ImportedSubstance {

            val pName = process.description.activity.name
            val meta = mutableMapOf<String, String?>(
                "methodName" to methodName,
                "geography" to (process.description.geography?.shortName ?: "")
            )
            val pUid = EcoSpoldProcessMapper.uid(process)
            val impacts = process.flowData.impactIndicators
                .filter { it.methodName == methodName }
                .map {
                    ImportedImpact(
                        it.amount,
                        it.unitName,
                        it.categoryName,
                        it.name
                    )
                }.toMutableList()

            return ImportedSubstance(pName, "Emission", "u", "", meta = meta, pUid = pUid, impacts = impacts)
        }
    }

}