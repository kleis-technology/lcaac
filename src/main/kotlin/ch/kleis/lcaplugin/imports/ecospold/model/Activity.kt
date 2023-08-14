package ch.kleis.lcaplugin.imports.ecospold.model

data class Activity(
    val id: String? = null,
    val type: String,
    val energyValues: String?,
    val name: String,
    val includedActivitiesStart: String?,
    val includedActivitiesEnd: String?,
    val generalComment: List<String>? = null,
    val tags: List<String> = ArrayList()
)