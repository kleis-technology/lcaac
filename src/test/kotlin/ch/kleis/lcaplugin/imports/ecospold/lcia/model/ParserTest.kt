package ch.kleis.lcaplugin.imports.ecospold.lcia.model

import ch.kleis.lcaplugin.imports.ecospold.model.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ParserTest {

    @Test
    fun readUnits_ShouldParceUnitConversionFile() {
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
    fun readDataset_Parse() {
        // Given
        this::class.java.getResourceAsStream("dataset.xml")!!.use {

            // When
            val dataset = Parser.readDataset(it)

            // Then
            assertEqualsToDescription(dataset.activityDataset.description)
            assertEqualsToFlow(dataset.activityDataset.flowData)
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
        assertEquals(1, flowData.intermediateExchanges.size)
        val exchange = flowData.intermediateExchanges[0]
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

    private fun assertEqualsToIndicators(impactIndicators: List<ImpactIndicator>) {
        assertEquals(1, impactIndicators.size)
        assertEquals("acidification (incl. fate, average Europe total, A&B)", impactIndicators[0].name)
        assertEquals(0.0011083933659871714, impactIndicators[0].amount, 1E-20)
        assertEquals("kg SO2-Eq", impactIndicators[0].unitName)
        assertEquals("CML v4.8 2016", impactIndicators[0].methodName)
        assertEquals("acidification", impactIndicators[0].categoryName)
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