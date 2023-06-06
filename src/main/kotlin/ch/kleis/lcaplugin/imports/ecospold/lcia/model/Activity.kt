package ch.kleis.lcaplugin.imports.ecospold.lcia.model

class Activity(
    val id: String? = null,
    val type: String,
    val energyValues: String?,
    val name: String,
    val includedActivitiesStart: String?,
    val includedActivitiesEnd: String?,
    val generalComment: List<String>? = null,
    val tags: List<String> = ArrayList()
) {

    data class Builder(
        var id: String? = null,
        var type: String? = null,
        var energyValues: String? = null,
        var name: String? = null,
        var includedActivitiesStart: String? = null,
        var includedActivitiesEnd: String? = null,
        var generalComment: List<String>? = null,
        var tags: List<String> = ArrayList()
    ) {
        fun id(id: String) = apply { this.id = id }
        fun type(type: String) = apply { this.type = type }
        fun energyValues(energyValues: String?) = apply { this.energyValues = energyValues }
        fun name(name: String) = apply { this.name = name }
        fun includedActivitiesStart(includedActivitiesStart: String?) =
            apply { this.includedActivitiesStart = includedActivitiesStart }

        fun includedActivitiesEnd(includedActivitiesEnd: String?) =
            apply { this.includedActivitiesEnd = includedActivitiesEnd }

        //    public List<String> allocationComment;
        fun generalComment(generalComment: List<String>) = apply { this.generalComment = generalComment }

        fun tags(tags: List<String>) = apply { this.tags = tags }

        fun build() = Activity(
            id,
            type!!,
            energyValues,
            name!!,
            includedActivitiesStart,
            includedActivitiesEnd,
            generalComment,
            tags
        )
    }
}