package ch.kleis.lcaplugin.imports.ecospold.lcia.model

class ActivityDescription private constructor(
    val activity: Activity,
    val classifications: List<Classification> = ArrayList(),
    val geography: Geography?
) {
    data class Builder(
        var activity: Activity? = null,
        var classifications: List<Classification> = ArrayList(),
        var geography: Geography? = null
    ) {
        fun activity(activity: Activity) = apply { this.activity = activity }
        fun classifications(classifications: List<Classification>) = apply { this.classifications = classifications }
        fun geography(geography: Geography) = apply { this.geography = geography }

        fun build() = ActivityDescription(activity!!, classifications, geography)
    }
}