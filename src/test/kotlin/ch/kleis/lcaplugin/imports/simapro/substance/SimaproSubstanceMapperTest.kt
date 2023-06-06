package ch.kleis.lcaplugin.imports.simapro.substance

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.model.ImportedImpact
import ch.kleis.lcaplugin.imports.simapro.substance.SimaproSubstanceMapper.Companion.resolveSimaproType
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.openlca.simapro.csv.enums.ElementaryFlowType
import org.openlca.simapro.csv.refdata.ElementaryFlowRow

class SimaproSubstanceMapperTest {

    private val row = ElementaryFlowRow()
        .name("Aluminium")
        .unit("kg")
        .cas("007429-90-5")
        .comment("Formula: Al\nAl\n")


    @Before
    fun before() {
        mockkObject(ModelWriter)
        every { ModelWriter.sanitizeAndCompact("kg") } returns "kg"
    }

    @After
    fun after() {
        unmockkAll()
    }

    @Test
    fun map_ShouldMapAllFields() {
        // Given
        row.platformId("platformId")

        // When
        val actual = SimaproSubstanceMapper.map(row, ElementaryFlowType.RESOURCES, "raw")

        // Then
        assertEquals("Aluminium", actual.name)
        assertEquals("Resource", actual.type)
        assertEquals("raw", actual.compartment)
        assertEquals("kg", actual.referenceUnit)
        assertEquals(listOf(ImportedImpact(1.0, "kg", "aluminium")), actual.impacts)
        assertEquals(
            mapOf(
                "generator" to "kleis-lca-generator",
                "description" to "Formula: Al\nAl\n",
                "casNumber" to "007429-90-5",
                "platformId" to "platformId"
            ), actual.meta
        )
    }

    @Test
    fun map_ShouldRemoveEmptyPlatform() {
        // Given
        row.platformId("")

        // When
        val actual = SimaproSubstanceMapper.map(row, ElementaryFlowType.RESOURCES, "raw")

        // Then
        assertEquals(
            mapOf(
                "generator" to "kleis-lca-generator",
                "description" to "Formula: Al\nAl\n",
                "casNumber" to "007429-90-5"
            ), actual.meta
        )
    }

    @Test
    fun render_whenUnitNameIsUnit_shouldRenameItWithU() {
        // Given
        row.unit("unit") // this unit name is a reserved keyword

        // When
        val actual = SimaproSubstanceMapper.map(row, ElementaryFlowType.RESOURCES, "raw")

        // Then
        assertEquals("u", actual.referenceUnit)

    }

    @Test
    fun resolveSimaproType_ShouldReturnTheType() {
        // Given
        val data = listOf(
            (ElementaryFlowType.RESOURCES to "Occupation,") to SubstanceType.LAND_USE,
            (ElementaryFlowType.RESOURCES to "Transformation,") to SubstanceType.LAND_USE,
            (ElementaryFlowType.RESOURCES to "other") to SubstanceType.RESOURCE,
            (ElementaryFlowType.SOCIAL_ISSUES to "lksjda") to SubstanceType.EMISSION,
            (ElementaryFlowType.EMISSIONS_TO_WATER to "33") to SubstanceType.EMISSION,
        )

        data.forEach { (param, expected) ->
            // When
            val result = resolveSimaproType(param.first, param.second)
            // Then
            kotlin.test.assertEquals(expected, result)
        }

    }

}