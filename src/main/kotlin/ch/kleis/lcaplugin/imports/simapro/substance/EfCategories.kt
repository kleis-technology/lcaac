package ch.kleis.lcaplugin.imports.simapro.substance

class EfCategories {
    enum class Compartiment(val value: String) {
        AIR("air"),
        BIOSPHERE("biosphere"),
        GROUND("ground"),
        INDUSTRIAL_SOIL("industrial soil"),
        LAND_OCCUPATION("land occupation"),
        LAND_TRANSFORMATION("land transformation"),
        SOIL("soil"),
        WATER("water");

        companion object {
            private val map = Compartiment.values().associateBy { it.value }
            infix fun from(value: String) = map[value]
        }
    }

    enum class SubCompartiment(val value: String) {
        AGRICULTURAL("agricultural"),
        FRESH_WATER("fresh water"),
        INDOOR("indoor"),
        LONG_TERM("long-term"),
        LOWER_STRATOSPHERE_AND_UPPER_TROPOSPHERE("lower stratosphere and upper troposphere"),
        NON_AGRICULTURAL("non-agricultural"),
        NON_RENEWABLE("non-renewable"),
        NON_URBAN_CLOSE_GROUND("non-urban close ground"),
        NON_URBAN_HIGH_STACK("non-urban high stack"),
        NON_URBAN_LOW_STACK("non-urban low stack"),
        NON_URBAN_VERY_HIGH_STACK("non-urban very high stack"),
        OTHER("other"),
        RENEWABLE("renewable"),
        SEA_WATER("sea water"),
        URBAN_AIR_CLOSE_TO_GROUND("urban air close to ground"),
        URBAN_HIGH_STACK("urban high stack"),
        URBAN_LOW_STACK("urban low stack"),
        URBAN_VERY_HIGH_STACK("urban very high stack");

        companion object {
            private val map = SubCompartiment.values().associateBy { it.value }
            infix fun from(value: String) = map[value]
        }
    }
}