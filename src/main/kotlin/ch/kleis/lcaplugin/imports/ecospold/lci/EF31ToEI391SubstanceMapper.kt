package ch.kleis.lcaplugin.imports.ecospold.lci

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.imports.ecospold.model.ElementaryExchange

typealias UID = String

// This voluntarily does not include flows that are "ecoinvent orphans": the mapper between EF31 and EI391 will return
// None(null), and it is to the caller to decide what to do with an unmapped substance.

// FIXME: proxy mapped comps
// FIXME: unmapped comps
// FIXME: substance type
object EF31ToEI391SubstanceMapper {
    const val method = "EF v3.1"
    const val ecoinvent_database_version = "3.9.1"
    private val mapping: Map<UID, ElementaryExchange?> = buildSubstanceMap()

    fun mapElementaryExchange(eiElementaryExchange: ElementaryExchange): ElementaryExchange? =
            mapping[eiElementaryExchange.elementaryExchangeId]?.let {
                it.copy(
                        amount = eiElementaryExchange.amount * it.amount,
                        substanceType = eiElementaryExchange.substanceType
                )
            }

    // FIXME: build cleanly
    // sqlite3 ef-31-mapping-3.9.1.sqlite "select id, conversion_factor, method_name, method_unit, method_compartment, method_subcompartment from mapping WHERE compartment_status is 'mapped';" | awk -F '|' '{ print "\"" $1 "\" to ElementaryExchange(\"" $1 "\",", $2 ",", "\"" $3 "\",", "\"" $4 "\",", "\"" $5 "\",", "\"" $6 "\",", "SubstanceType.EMISSION, null)," }' | sed 's/""/null/'
    private fun buildSubstanceMap(): Map<UID, ElementaryExchange?> = mapOf(
            "584ffb1c-036d-417b-a9d1-1ec694dc2cdc" to ElementaryExchange("584ffb1c-036d-417b-a9d1-1ec694dc2cdc", 1.0, "1,2-dichlorobenzene", "kg", "Emissions to air", "Emissions to air, unspecified (long-term)", SubstanceType.EMISSION, null),
            "77db8bd1-5a69-465c-b51f-7b27fbb574a5" to ElementaryExchange("77db8bd1-5a69-465c-b51f-7b27fbb574a5", 1.0, "1,2-dichlorobenzene", "kg", "Emissions to air", "Emissions to lower stratosphere and upper troposphere", SubstanceType.EMISSION, null),
            "06a42317-47bd-481d-b5ce-e091843497c6" to ElementaryExchange("06a42317-47bd-481d-b5ce-e091843497c6", 1.0, "1,2-dichlorobenzene", "kg", "Emissions to air", "Emissions to non-urban air or from high stacks", SubstanceType.EMISSION, null),
            "b1c36287-329c-49f0-93c2-68246d34007c" to ElementaryExchange("b1c36287-329c-49f0-93c2-68246d34007c", 1.0, "1,2-dichlorobenzene", "kg", "Emissions to air", "Emissions to air, unspecified", SubstanceType.EMISSION, null),
            "9645e02f-855a-4b9f-8baf-f34a08fa80c4" to ElementaryExchange("9645e02f-855a-4b9f-8baf-f34a08fa80c4", 1.0, "1,2-dichlorobenzene", "kg", "Emissions to air", "Emissions to urban air close to ground", SubstanceType.EMISSION, null),
            // ... and so on
    )
}