package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.value.UnitValue
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.imports.ImportException
import ch.kleis.lcaplugin.imports.ModelWriter
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import org.openlca.simapro.csv.refdata.UnitRow

class UnitRendererTest {


    private val writer = mockk<ModelWriter>()

    @Before
    fun before() {
        every { writer.write(any(), any()) } returns Unit
        every { ModelWriter.sanitizeString("k+g") } returns "k_g"
        every { ModelWriter.sanitizeString("kg") } returns "kg"
        every { ModelWriter.sanitizeString("s") } returns "s"
        every { ModelWriter.sanitizeString("s€c") } returns "s_c"
        every { ModelWriter.sanitizeString("me2") } returns "me2"
        every { ModelWriter.sanitizeString("m2") } returns "m2"
    }

    @Test
    fun test_writeUnit_ShouldReturnEmptyWhenAlreadyExistWithCompatibleDimension() {
        // Given
        val sut = UnitRenderer.of(mapOf(Pair("kg", UnitValue("k+g", 1.0, Prelude.mass))))
        val data = UnitRow().name("kg")
            .quantity("Mass")
            .conversionFactor(1.0)
            .referenceUnit("kg")

        // When
        sut.render(data, writer)

        // Then
        verify {
            writer.write("unit.lca", "")
        }
    }

    @Test
    fun test_writeUnit_ShouldDeclareUnitWhenItsTheReferenceForNewDimension() {
        // Given
        val sut = UnitRenderer.of(mapOf(Pair("kg", UnitValue("k+g", 1.0, Prelude.mass))))
        val data = UnitRow().name("s€c")
            .quantity("Time")
            .conversionFactor(1.0)
            .referenceUnit("s")

        // When
        sut.render(data, writer)

        // Then
        val expected = """
            
            unit s_c {
                symbol = "s€c"
                dimension = "time"
            }
""".trimIndent()
        verify {
            writer.write(
                "unit.lca", expected
            )
        }
    }

    @Test
    fun test_writeUnit_ShouldDeclareAliasWhenItsAnAliasForExistingDimension() {
        // Given
        val sut = UnitRenderer.of(mapOf(Pair("m2", UnitValue("m2", 1.0, Prelude.length.pow(2.0)))))
        val data = UnitRow().name("me2")
            .quantity("Area")
            .conversionFactor(1.0)
            .referenceUnit("m2")

        // When
        sut.render(data, writer)

        // Then
        val expected = """
            
            unit me2 {
                symbol = "me2"
                alias_for = 1.0 m2
            }
""".trimIndent()
        verify {
            writer.write(
                "unit.lca", expected
            )
        }
    }

    @Test
    fun test_writeUnit_ShouldDeclareAliasWhenItsNotTheReference() {
        // Given
        val sut = UnitRenderer.of(mapOf(Pair("kg", UnitValue("k+g", 1.0, Prelude.mass))))
        val data = UnitRow().name("s€c")
            .quantity("Time")
            .conversionFactor(2.0)
            .referenceUnit("s")

        // When
        sut.render(data, writer)

        // Then
        val expected = """
            
            unit s_c {
                symbol = "s€c"
                alias_for = 2.0 s
            }
""".trimIndent()
        verify {
            writer.write(
                "unit.lca", expected
            )
        }
    }

    @Test
    fun test_writeUnit_ShouldRecordNewUnit() {
        // Given
        val sut = UnitRenderer.of(emptyMap())
        val data = UnitRow().name("s€c")
            .quantity("Time")
            .conversionFactor(2.0)
            .referenceUnit("s")
        sut.render(data, writer)

        // When
        sut.render(data, writer)

        // Then
        verifyOrder {
            writer.write("unit.lca", any())
            writer.write("unit.lca", "")
        }
    }

    @Test
    fun test_writeUnit_ShouldFailWithAnotherDimension() {
        // Given
        val sut = UnitRenderer.of(mapOf(Pair("kg", UnitValue("k+g", 1.0, Prelude.mass))))
        val data = UnitRow().name("kg")
            .quantity("Time")
            .conversionFactor(1.0)
            .referenceUnit("kg")

        // When + Then
        try {
            sut.render(data, writer)
            fail("Should not pass !")
        } catch (e: ImportException) {
            assertEquals(
                "A Unit kg for kg already exists with another dimension, time[1.0] is not compatible with mass[1.0].",
                e.message
            )
        }
    }

    @Test
    fun test_writeUnit_ShouldFailWithAReferenceToItselfInAnExistingDimension() {
        // Given
        val sut = UnitRenderer.of(mapOf(Pair("g", UnitValue("g", 1.0, Prelude.mass))))
        val data = UnitRow().name("kg")
            .quantity("mass")
            .conversionFactor(1.0)
            .referenceUnit("kg")

        // When + Then
        try {
            sut.render(data, writer)
            fail("Should not pass !")
        } catch (e: ImportException) {
            assertEquals(
                "Unit kg is referencing itself in its own declaration",
                e.message
            )
        }
    }

    @Test
    fun test_areCompatible() {
        // Given
        val input = listOf<Triple<Dimension, Dimension, Boolean>>(
            Triple(Prelude.length, Prelude.length, true),
            Triple(Dimension.of("volume"), Prelude.length.pow(3.0), true),
            Triple(Prelude.length.pow(3.0), Dimension.of("volume"), true),

            Triple(Dimension.of("power"), Prelude.energy.divide(Prelude.time), true),
            Triple(Prelude.energy.divide(Prelude.time), Dimension.of("power"), true),

            Triple(Dimension.of("volume"), Prelude.length.pow(3.0), true),
            Triple(Prelude.length.pow(3.0), Dimension.of("volume"), true),

            Triple(Prelude.none, Dimension.of("amount"), true),
            Triple(Dimension.of("amount"), Prelude.none, true),

            Triple(Prelude.transport, Dimension.of("transport"), true),
            Triple(Dimension.of("transport"), Prelude.transport, true),

            Triple(Prelude.length_time, Dimension.of("length.time"), true),
            Triple(Dimension.of("length.time"), Prelude.length_time, true),

            Triple(Prelude.person_distance, Dimension.of("person.distance"), true),
            Triple(Dimension.of("person.distance"), Prelude.person_distance, true),

            Triple(Prelude.volume_time, Dimension.of("volume.time"), true),
            Triple(Dimension.of("volume.time"), Prelude.volume_time, true),

            Triple(Prelude.radioactivity, Dimension.of("amount"), false),
            Triple(Dimension.of("amount"), Prelude.radioactivity, false),
        )
        val sut = UnitRenderer.of(emptyMap())

        // When + Then
        input.forEach {
            assertEquals(
                "Invalid result for ${it.first} and ${it.second}, expected ${it.third}",
                it.third,
                sut.areCompatible(it.first, it.second)
            )
        }


    }
}