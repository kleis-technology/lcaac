package ch.kleis.lcaplugin.imports.ecospold.model

data class ActivityDescription(
    val activity: Activity,
    val classifications: List<Classification> = ArrayList(),
    val geography: Geography?
)