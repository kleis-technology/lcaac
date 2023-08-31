package ch.kleis.lcaplugin.imports.ecospold.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ParserTest {

    @Test
    fun readUnits_ShouldParseUnitConversionFile() {
        // Given
        this::class.java.getResourceAsStream("units.xml")!!.use {

            // When
            val units = Parser.readUnits(it)

            // Then
            assertEquals(1, units.size)
            assertEquals(1.60934, units[0].factor, 1E-6)
            assertEquals("miles", units[0].fromUnit)
            assertEquals("km", units[0].toUnit)
            assertEquals("distance", units[0].dimension)
        }
    }

    @Test
    fun readDatasetLCIA_Parse() {
        // Given
        this::class.java.getResourceAsStream("dataset_lcia.xml")!!.use {

            // When
            val dataset = Parser.readDataset(it)

            // Then
            assertEqualsToDescription(dataset.description)
            assertEqualsToFlow(dataset.flowData)
        }
    }

    @Test
    fun readDataSet_UPR_Parse() {
        // Given
        this::class.java.getResourceAsStream("dataset_upr.xml")!!.use { istream ->

            // When
            val dataset = Parser.readDataset(istream)

            // Then
            assertEquals(12, dataset.flowData.intermediateExchanges.count())
            assertEquals(1, dataset.flowData.intermediateExchanges.count {
                it.outputGroup?.let { true } ?: false
            })
            assertEquals(2, dataset.flowData.elementaryExchanges.count())
        }
    }

    @Test
    fun readDataset_UPR_inputExchange_ShouldParseActivityLinkId() {
        // Given
        val inputIDs = sequenceOf(
            "0fce055e-fae5-5313-a883-3e5fc3a035ad",
            "26005d7a-6633-5d24-b0db-5f7796b2740a",
            "413ef35a-2c29-5939-a649-2860786d1f9b",
            "511b903b-773c-58ae-b64b-f6b2d1948a56",
            "615e650d-7bbd-5901-9a96-04bb034eb66b",
            "73575312-461e-55cb-a642-9eb8b8bb56a4",
            "7c44ffb5-5119-5c66-bfc0-62fee469ea66",
            "9b9887d7-937f-5c20-9a0f-f99944341c24",
            "b3ceebfa-4923-5801-9141-a6d8b268b70e",
            "bc47228e-c762-5260-8193-ad730d2af531",
            "fba1d66b-f42d-50e3-be7f-8fd2b919753c",
        )
        this::class.java.getResourceAsStream("dataset_upr.xml")!!.use { istream ->
            // When
            val dataset = Parser.readDataset(istream)
            val inputExchanges = dataset.flowData.intermediateExchanges.filter { it.inputGroup != null }

            // Then
            inputExchanges.zip(inputIDs).forEach { (exchange, expectedID) ->
                assertEquals(expectedID, exchange.activityLinkId)
            }
        }
    }

    @Test
    fun readDataset_ShouldParse_WithOnlyMandatoryFields() {
        // Given
        this::class.java.getResourceAsStream("dataset_min.xml")!!.use {

            // When
            val dataset = Parser.readDataset(it)

            // Then
            assertNotNull(dataset)
        }
    }

    @Test
    fun readMethodUnits_Parse() {
        // Given
        this::class.java.getResourceAsStream("impact_method.xml")!!.use {

            // When
            val units = Parser.readMethodUnits(it, "EF v3.1 no LT")

            // Then
            assertEquals(
                listOf(
                    UnitConversion(
                        1.0,
                        "mol H+-Eq",
                        "No Ref",
                        "mol H+-Eq",
                        "accumulated exceedance (AE) no LT"
                    )
                ), units
            )
        }
    }

    @Test
    fun readMethodUnits_ShouldReplaceDimension_WithDimensionless() {
        // Given
        this::class.java.getResourceAsStream("impact_method.xml")!!.use {

            // When
            val units = Parser.readMethodUnits(it, "EF v1.0")

            // Then
            assertEquals(
                listOf(
                    UnitConversion(
                        1.0,
                        "dimensionless_impact",
                        "No Ref",
                        "dimensionless_impact",
                        "accumulated Bad"
                    )
                ), units
            )
        }
    }

    @Test
    fun readMethodNames_ShouldReturnList() {
        // Given
        this::class.java.getResourceAsStream("impact_method.xml")!!.use {

            // When
            val names = Parser.readMethodName(it)

            // Then
            assertEquals(listOf("EF v1.0", "EF v3.1 no LT"), names)
        }
    }

    private fun assertEqualsToFlow(flowData: FlowData) {
        assertEquals(1, flowData.intermediateExchanges.count())
        val exchange = flowData.intermediateExchanges.first()
        assertEquals("electricity, high voltage", exchange.name)
        assertEquals(1.0, exchange.amount)
        assertEquals("kWh", exchange.unit)
        assertEquals(listOf("WTF"), exchange.synonyms)
        assertEquals(0, exchange.outputGroup)
        assertEquals(
            listOf(Classification("By-product classification", "allocatable product")),
            exchange.classifications
        )
        assertEquals(LogNormal(8.688, 2.162, 0.015, 0.0152), exchange.uncertainty?.logNormal)
        assertEquals(PedigreeMatrix(1, 2, 3, 4, 5), exchange.uncertainty?.pedigreeMatrix)
        assertEquals("uncertainty is calculated", exchange.uncertainty?.comment)
        assertEquals(
            listOf(
                Property("carbon allocation", 0.0, "kg", "false", "true")
            ), exchange.properties
        )

        assertEqualsToIndicators(flowData.impactIndicators)

    }

    private fun assertEqualsToIndicators(impactIndicators: Sequence<ImpactIndicator>) {
        assertEquals(1, impactIndicators.count())
        assertEquals("acidification (incl. fate, average Europe total, A&B)", impactIndicators.first().name)
        assertEquals(0.0011083933659871714, impactIndicators.first().amount, 1E-20)
        assertEquals("kg SO2-Eq", impactIndicators.first().unitName)
        assertEquals("CML v4.8 2016", impactIndicators.first().methodName)
        assertEquals("acidification", impactIndicators.first().categoryName)
    }


    private fun assertEqualsToDescription(description: ActivityDescription?) {
        assertNotNull(description)
        val desc: ActivityDescription = description
        assertEquals("electricity, high voltage", desc.activity.name)
        assertEquals("24342343", desc.activity.id)
        assertEquals("2", desc.activity.type)
        assertEquals("This activity represents", desc.activity.includedActivitiesStart)
        assertEquals("This activity is not.", desc.activity.includedActivitiesEnd)
        assertEquals("0", desc.activity.energyValues)
        assertEquals(listOf("First", "Second"), desc.activity.generalComment)

        assertEquals(1, desc.classifications.size)
        assertEquals(Classification("ISIC rev.4", "3510:"), desc.classifications[0])
        assertEquals("TJ", desc.geography?.shortName)
        assertEquals(listOf("Empty"), desc.geography?.comment)
    }


}