package ch.kleis.lcaplugin.imports.ecospold.model

class ActivityDataset private constructor(
    val description: ActivityDescription,
    val flowData: FlowData,
) {
    data class Builder(
        var description: ActivityDescription? = null,
        var flowData: FlowData? = null,
    ) {
        fun description(description: ActivityDescription) = apply { this.description = description }
        fun flowData(flowData: FlowData) = apply { this.flowData = flowData }
        fun build() = ActivityDataset(description!!, flowData!!)
    }
}