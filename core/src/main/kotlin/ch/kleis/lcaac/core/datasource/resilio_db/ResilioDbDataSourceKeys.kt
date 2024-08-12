package ch.kleis.lcaac.core.datasource.resilio_db

object ResilioDbDataSourceKeys {
    const val RDB_PARAMS_FROM = "paramsFrom"
    const val RDB_FOREIGN_KEY = "foreignKey"
    const val RDB_ENDPOINT = "endpoint"
    const val RDB_LC_STEP_KEY = "lcStepKey"
    const val RDB_MANUFACTURING = "manufacturing"
    const val RDB_TRANSPORT = "transport"
    const val RDB_USE = "use"
    const val RDB_END_OF_LIFE = "endOfLife"

    fun requiredOptionKeys() = setOf(
        RDB_PARAMS_FROM, RDB_FOREIGN_KEY,
        RDB_ENDPOINT,
        RDB_LC_STEP_KEY,
        RDB_MANUFACTURING, RDB_TRANSPORT,
        RDB_USE, RDB_END_OF_LIFE
    )
}
