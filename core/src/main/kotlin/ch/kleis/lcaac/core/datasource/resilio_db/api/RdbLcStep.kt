package ch.kleis.lcaac.core.datasource.resilio_db.api

enum class RdbLcStep(val rdbField: String) {
    MANUFACTURING("BLD"),
    TRANSPORT("DIS"),
    USE("USE"),
    END_OF_LIFE("EOL");

    companion object {
        fun fromRdbField(rdbField: String): RdbLcStep? {
            return entries.firstOrNull {
                it.rdbField == rdbField
            }
        }
    }
}
