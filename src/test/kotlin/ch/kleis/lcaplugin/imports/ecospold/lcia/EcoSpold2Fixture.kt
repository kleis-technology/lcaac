package ch.kleis.lcaplugin.imports.ecospold.lcia

import ch.kleis.lcaplugin.imports.ecospold.model.*

class EcoSpold2Fixture {
    companion object {
        fun buildData(outputGroup: Int = 0): ActivityDataset {
            val activity = Activity.Builder()
                .id("aId")
                .name("aName")
                .type("1")
                .generalComment(listOf("ageneralComment"))
                .energyValues("123")
                .includedActivitiesStart("includedActivitiesStart")
                .includedActivitiesEnd("includedActivitiesEnd")
                .build()
            val c = Classification("System", "Value")
            val geo = Geography("ch", listOf("comment"))
            val description = ActivityDescription.Builder()
                .activity(activity)
                .geography(geo)
                .classifications(listOf(c))
                .build()
            val prod = IntermediateExchange.Builder()
                .name("pName")
                .outputGroup(outputGroup)
                .classifications(listOf(Classification("PSystem", "PValue")))
                .uncertainty(
                    Uncertainty(
                        logNormal = LogNormal(1.2, 3.4, 2.3, 4.5)
                    )
                )
                .amount(1.0)
                .unit("km")
                .synonyms(listOf("p1"))
                .build()
            val impacts = listOf(
                ImpactIndicator("EF v3.0 no LT", "water use", "deprivation", 0.1188, "m3 world eq. deprived"),
                ImpactIndicator("EF v3.1", "acidification", "accumulated exceedance (AE)", 0.0013, "mol H+-Eq"),
                ImpactIndicator("EF v3.1", "climate change", "global warming potential (GWP100)", 0.6, "kg CO2-Eq"),
            )
            return ActivityDataset.Builder()
                .description(description)
                .flowData(FlowData(listOf(prod), impacts))
                .build()
        }

    }
}