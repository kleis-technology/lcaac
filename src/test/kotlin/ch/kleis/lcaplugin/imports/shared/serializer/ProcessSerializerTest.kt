package ch.kleis.lcaplugin.imports.shared.serializer

import ch.kleis.lcaplugin.imports.model.*
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import kotlin.test.assertEquals

class ProcessSerializerTest {
    private val proc: ImportedProcess

    init {
        val meta = mapOf(
            "name" to "name_value",
            "description" to "The \"module\" reflects.\n[This dataset ]\nProduction volume: 0.15000000596046448 p",
            "category" to "energy",
            "identifier" to "EI1519605797",
            "literatures" to "\n    * Methodological Guidelines for the Life Cycle Inventory of Agricultural Products.\n    * Another Methodological.",
        )

        val prod1 = ImportedProductExchange(
            "1.0",
            "p",
            "absorption_chiller_100kw",
            80.0,
            listOf(
                "name: Absorption chiller, 100kW {RoW}| production | Cut-off, U",
                "category: Cogeneration\\Gas\\Transformation\\Infrastructure"
            ),
        )
        val prod2 = ImportedProductExchange(
            "(x + 2) * 1",
            "p",
            "absorption_chiller_50kw",
            20.0,
            listOf(
                "name: Absorption chiller, 50kW {RoW}",
            ),
        )
        val products = listOf(ExchangeBlock("Products", sequenceOf(prod1, prod2)))
        val inputs = listOf(
            ExchangeBlock(
                "materialsAndFuels",
                sequenceOf(
                    ImportedInputExchange(
                        uid = "aluminium_glo",
                        qty = "420.0",
                        unit = "kg",
                        fromProcess = "market_for_aluminium_glo",
                        comments = listOf("Estimation based on few references"),
                    )
                )
            )
        )
        val emissions = listOf(
            ExchangeBlock(
                "To Air",
                sequenceOf(
                    ImportedBioExchange(
                        "0.9915", "m3", "water_m3", "air", null,
                        listOf("Calculated value based on expertise"),
                    )
                )
            ),
            ExchangeBlock(
                "To Water with sub",
                sequenceOf(
                    ImportedBioExchange(
                        "0.9915", "m3", "water_m3", "water", "river",
                        listOf("Calculated value based on expertise"),
                    )
                )
            )
        )
        val resources = listOf(
            ExchangeBlock(
                "Natural",
                sequenceOf(
                    ImportedBioExchange(
                        "5.9", "m3", "water", "water", "in river",
                        listOf(
                            """
                        Approximation this is a first paragraph
                        
                        And this is a second paragraph, which should also be commented.
                        """.trimIndent()
                        ),
                    )
                )
            )
        )
        val landUse = listOf(
            ExchangeBlock(
                "",
                sequenceOf(
                    ImportedBioExchange(
                        "42.17", "m2a", "occupation_industrial_area", "raw", "land",
                        listOf("Rough estimation"),
                    )
                )
            )
        )

        val impacts = listOf(
            ExchangeBlock(
                "",
                sequenceOf(
                    ImportedImpactExchange(
                        "0.0013", "mol_H_p_Eq", "accumulated_exceedance_ae", listOf("acidification")
                    )
                )
            )
        )

        proc = ImportedProcess(
            uid = "uid",
            meta = meta,
            productBlocks = products,
            inputBlocks = inputs,
            emissionBlocks = emissions,
            resourceBlocks = resources,
            landUseBlocks = landUse,
            impactBlocks = impacts,
        )
    }


    @Test
    fun testRender() {
        // Given

        // When
        val result = ProcessSerializer.serialize(proc)

        // Then
        val expected = """

process uid {

    meta {
        "name" = "name_value"
        "description" = "The 'module' reflects.
            [This dataset ]
            Production volume: 0.15000000596046448 p"
        "category" = "energy"
        "identifier" = "EI1519605797"
        "literatures" = "
                * Methodological Guidelines for the Life Cycle Inventory of Agricultural Products.
                * Another Methodological."
    }

    products { // Products
        // name: Absorption chiller, 100kW {RoW}| production | Cut-off, U
        // category: Cogeneration\Gas\Transformation\Infrastructure
        1.0 p absorption_chiller_100kw allocate 80.0 percent
        // name: Absorption chiller, 50kW {RoW}
        (x + 2) * 1 p absorption_chiller_50kw allocate 20.0 percent
    }

    inputs { // materialsAndFuels
        // Estimation based on few references
        420.0 kg aluminium_glo from market_for_aluminium_glo
    }

    emissions { // To Air
        // Calculated value based on expertise
        0.9915 m3 water_m3(compartment = "air")
    }

    emissions { // To Water with sub
        // Calculated value based on expertise
        0.9915 m3 water_m3(compartment = "water", sub_compartment = "river")
    }

    resources { // Natural
        // Approximation this is a first paragraph
        // And this is a second paragraph, which should also be commented.
        5.9 m3 water(compartment = "water", sub_compartment = "in river")
    }

    land_use {
        // Rough estimation
        42.17 m2a occupation_industrial_area(compartment = "raw", sub_compartment = "land")
    }

    impacts {
        // acidification
        0.0013 mol_H_p_Eq accumulated_exceedance_ae
    }
}"""
        assertEquals(expected, result.toString())
    }


    @Test
    fun testRender_WithParams() {
        // Given
        proc.params.add(ImportedParam("x", "3 u"))
        proc.params.add(ImportedParam("y", "5 u"))

        // When
        val result = ProcessSerializer.serialize(proc)

        // Then
        val expected = """
    params {
        x = 3 u
        y = 5 u
    }"""
        assertThat(result.toString(), containsString(expected))
    }

}