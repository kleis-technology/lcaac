package ch.kleis.lcaac.core.datasource.resilio_db.api

data class LcStepMapping(
    val key: String,
    val manufacturing: String,
    val transport: String,
    val use: String,
    val endOfLife: String,
) {
    fun getMappedName(lcStep: RdbLcStep): String = when(lcStep) {
        RdbLcStep.MANUFACTURING -> manufacturing
        RdbLcStep.TRANSPORT -> transport
        RdbLcStep.USE -> use
        RdbLcStep.END_OF_LIFE -> endOfLife
    }
}
