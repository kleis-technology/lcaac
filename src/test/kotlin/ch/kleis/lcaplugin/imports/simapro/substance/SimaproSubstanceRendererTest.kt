package ch.kleis.lcaplugin.imports.simapro.substance

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.imports.ModelWriter
import io.mockk.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.openlca.simapro.csv.enums.ElementaryFlowType
import org.openlca.simapro.csv.refdata.ElementaryFlowBlock
import org.openlca.simapro.csv.refdata.ElementaryFlowRow
import kotlin.test.assertEquals

class SimaproSubstanceRendererTest {
    private val writer = mockk<ModelWriter>()

    private val pathSlot = slot<String>()
    private val bodySlot = slot<String>()
    private val indexSlot = slot<Boolean>()
    private val sut = SimaproSubstanceRenderer()


    @Before
    fun before() {
        every { writer.write(capture(pathSlot), capture(bodySlot), capture(indexSlot)) } returns Unit
        mockkObject(ModelWriter)
        every { ModelWriter.sanitizeAndCompact("kg") } returns "kg"
    }

    @After
    fun after() {
        unmockkAll()
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
            val result = SimaproSubstanceRenderer.resolveSimaproType(param.first, param.second)
            // Then
            assertEquals(expected, result)
        }

    }

    @Test
    fun render() {
        // Given
        val block = ElementaryFlowBlock.of(ElementaryFlowType.RESOURCES)
        block.flows().add(
            ElementaryFlowRow()
                .name("Aluminium")
                .unit("kg")
                .cas("007429-90-5")
                .comment("Formula: Al\nAl\n")
                .platformId("platformId")
        )


        // When
        sut.render(block, writer)

        // Then
        val expected = """

substance aluminium_raw {

    name = "Aluminium"
    type = Resource
    compartment = "raw"
    reference_unit = kg

    impacts {
        1 kg aluminium_raw
    }

    meta {
        "generator" = "kleis-lca-generator"
        "description" = "Formula: Al
            Al"
        "casNumber" = "007429-90-5"
        "platformId" = "platformId"
    }
}
""".trimIndent()
        // Better way to view large diff than using mockk.verify
        Assert.assertEquals("substances/raw/aluminium_raw.lca", pathSlot.captured)
        Assert.assertEquals(expected, bodySlot.captured)
        Assert.assertEquals(true, indexSlot.captured)
    }

    @Test
    fun renderWithoutPlatform() {
        // Given
        val block = ElementaryFlowBlock.of(ElementaryFlowType.RESOURCES)
        block.flows().add(
            ElementaryFlowRow()
                .name("Aluminium")
                .unit("kg")
                .cas("007429-90-5")
                .comment("Formula: Al\nAl\n")
                .platformId("")
        )


        // When
        sut.render(block, writer)

        // Then
        val expected = """

substance aluminium_raw {

    name = "Aluminium"
    type = Resource
    compartment = "raw"
    reference_unit = kg

    impacts {
        1 kg aluminium_raw
    }

    meta {
        "generator" = "kleis-lca-generator"
        "description" = "Formula: Al
            Al"
        "casNumber" = "007429-90-5"
        
    }
}
""".trimIndent()
        // Better way to view large diff than using mockk.verify
        Assert.assertEquals("substances/raw/aluminium_raw.lca", pathSlot.captured)
        Assert.assertEquals(expected, bodySlot.captured)
        Assert.assertEquals(true, indexSlot.captured)
        Assert.assertEquals(1, sut.nbSubstances)
    }

    @Test
    fun render_whenUnitNameIsAReservedKeyword_shouldRenameItWithAnUnderscore() {
        // Given
        val block = ElementaryFlowBlock.of(ElementaryFlowType.RESOURCES)
        block.flows().add(
            ElementaryFlowRow()
                .name("Aluminium")
                .unit("unit") // this unit name is a reserved keyword
                .cas("007429-90-5")
                .comment("Formula: Al\nAl\n")
                .platformId("platformId")
        )


        // When
        sut.render(block, writer)

        // Then
        val expected = """

substance aluminium_raw {

    name = "Aluminium"
    type = Resource
    compartment = "raw"
    reference_unit = _unit

    impacts {
        1 _unit aluminium_raw
    }

    meta {
        "generator" = "kleis-lca-generator"
        "description" = "Formula: Al
            Al"
        "casNumber" = "007429-90-5"
        "platformId" = "platformId"
    }
}
""".trimIndent()
        // Better way to view large diff than using mockk.verify
        Assert.assertEquals("substances/raw", pathSlot.captured)
        Assert.assertEquals(expected, bodySlot.captured)
        Assert.assertEquals(true, indexSlot.captured)
    }
}
