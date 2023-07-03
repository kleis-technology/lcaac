package ch.kleis.lcaplugin.imports.shared

import ch.kleis.lcaplugin.imports.model.*
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import kotlin.test.assertEquals

class ProcessSerializerTest {
    @Suppress("JoinDeclarationAndAssignment")
    private val proc: ImportedProcess

    init {
        proc = ImportedProcess("uid")
        proc.meta["name"] = "name_value"
        proc.meta["description"] = "The \"module\" reflects.\n[This dataset ]\nProduction volume: 0.15000000596046448 p"
        proc.meta["category"] = "energy"
        proc.meta["identifier"] = "EI1519605797"
        proc.meta["literatures"] =
            "\n    * Methodological Guidelines for the Life Cycle Inventory of Agricultural Products.\n    * Another Methodological."

        val prod1 = ImportedProductExchange(
            listOf(
                "name: Absorption chiller, 100kW {RoW}| production | Cut-off, U",
                "category: Cogeneration\\Gas\\Transformation\\Infrastructure"
            ),
            "1.0",
            "p",
            "absorption_chiller_100kw",
            80.0
        )
        val prod2 = ImportedProductExchange(
            listOf(
                "name: Absorption chiller, 50kW {RoW}",
            ),
            "(x + 2) * 1",
            "p",
            "absorption_chiller_50kw",
            20.0
        )
        val products = ExchangeBlock("Products", mutableListOf(prod1, prod2))
        proc.productBlocks.add(products)

        proc.inputBlocks.add(
            ExchangeBlock(
                "materialsAndFuels",
                mutableListOf(
                    ImportedInputExchange(
                        listOf("Estimation based on few references"),
                        "420.0", "kg", "aluminium_glo_market"
                    )
                )
            )
        )

        proc.emissionBlocks.add(
            ExchangeBlock(
                "To Air",
                mutableListOf(
                    ImportedBioExchange(
                        listOf("Calculated value based on expertise"),
                        "0.9915", "m3", "water_m3", "air"
                    )
                )
            )
        )
        proc.emissionBlocks.add(
            ExchangeBlock(
                "To Water with sub",
                mutableListOf(
                    ImportedBioExchange(
                        listOf("Calculated value based on expertise"),
                        "0.9915", "m3", "water_m3", "water", "river"
                    )
                )
            )
        )
        proc.resourceBlocks.add(
            ExchangeBlock(
                "Natural",
                mutableListOf(
                    ImportedBioExchange(
                        listOf("Approximation"),
                        "5.9", "m3", "water", "water", "in river"
                    )
                )
            )
        )

        proc.landUseBlocks.add(
            ExchangeBlock(
                "",
                mutableListOf(
                    ImportedBioExchange(
                        listOf("Rough estimation"),
                        "42.17", "m2a", "occupation_industrial_area", "raw", "land"
                    )
                )
            )
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
        420.0 kg aluminium_glo_market
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
        // Approximation
        5.9 m3 water(compartment = "water", sub_compartment = "in river")
    }

    land_use {
        // Rough estimation
        42.17 m2a occupation_industrial_area(compartment = "raw", sub_compartment = "land")
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