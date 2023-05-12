package ch.kleis.lcaplugin.imports.simapro.substance

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.imports.model.ImpactImported
import ch.kleis.lcaplugin.imports.model.SubstanceImported
import ch.kleis.lcaplugin.imports.simapro.sanitizeUnit
import org.openlca.simapro.csv.enums.ElementaryFlowType
import org.openlca.simapro.csv.refdata.ElementaryFlowRow

class SimaproSubstanceMapper {
    companion object {
        fun map(
            element: ElementaryFlowRow, type: ElementaryFlowType, compartment: String
        ): SubstanceImported {
            val subType = resolveSimaproType(type, element.name())
            val substance =
                SubstanceImported(element.name(), subType.toString(), sanitizeUnit(element.unit()), compartment)
            substance.impacts.add(ImpactImported(1.0, sanitizeUnit(element.unit()), substance.uid))
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