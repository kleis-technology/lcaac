package ch.kleis.lcaplugin.imports.simapro.substance

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.imports.model.ImportedImpact
import ch.kleis.lcaplugin.imports.model.ImportedSubstance
import ch.kleis.lcaplugin.imports.simapro.sanitizeSymbol
import org.openlca.simapro.csv.enums.ElementaryFlowType
import org.openlca.simapro.csv.refdata.ElementaryFlowRow

class SimaproSubstanceMapper {
    companion object {
        fun map(
            element: ElementaryFlowRow, type: ElementaryFlowType, compartment: String
        ): ImportedSubstance {
            val subType = resolveSimaproType(type, element.name())
            val substance =
                ImportedSubstance(element.name(), subType.toString(), sanitizeSymbol(element.unit()), compartment)
            substance.impacts.add(ImportedImpact(1.0, sanitizeSymbol(element.unit()), substance.uid))
            substance.meta["generator"] = "kleis-lca-generator"
            substance.meta["description"] = element.comment()
            substance.meta["casNumber"] = element.cas()
            if (!element.platformId().isNullOrBlank())
                substance.meta["platformId"] = element.platformId()
            return substance
        }

        fun resolveSimaproType(type: ElementaryFlowType, name: String): SubstanceType {
            return when (type) {
                ElementaryFlowType.RESOURCES ->
                    return when {
                        name.startsWith("Occupation,") || name.startsWith("Transformation,") -> SubstanceType.LAND_USE
                        else -> SubstanceType.RESOURCE
                    }

                else -> SubstanceType.EMISSION
            }
        }
    }
}