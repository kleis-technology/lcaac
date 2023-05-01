package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.value.UnitValue
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.imports.ImportException
import ch.kleis.lcaplugin.imports.ModelWriter
import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.openlca.simapro.csv.refdata.UnitRow

class UnitRendererTest {


    private val writer = mockk<ModelWriter>()
    private val pathSlot = slot<String>()
    private val bodySlot = slot<String>()
    private val indexSlot = slot<Boolean>()

    @Before
    fun before() {
        every { writer.write(capture(pathSlot), capture(bodySlot), capture(indexSlot)) } returns Unit
        mockkObject(ModelWriter.Companion)
        every { ModelWriter.sanitizeAndCompact("k+g") } returns "k_g"
        every { ModelWriter.sanitizeAndCompact("kg") } returns "kg"
        every { ModelWriter.sanitizeAndCompact("s") } returns "s"
        every { ModelWriter.sanitizeAndCompact("s€c") } returns "s_c"
        every { ModelWriter.sanitizeAndCompact("me2") } returns "me2"
        every { ModelWriter.sanitizeAndCompact("m2") } returns "m2"
    }

    @After
    fun after() {
        unmockkAll()
    }

    @Test
    fun test_writeUnit_ShouldReturnWithoutWritingWhenAlreadyExistWithCompatibleDimension() {
        // Given
        val sut = UnitRenderer.of(mapOf(Pair("kg", UnitValue("k+g", 1.0, Prelude.mass))))
        val data = UnitRow().name("kg")
            .quantity("Mass")
            .conversionFactor(1.0)
            .referenceUnit("kg")
        // When
        sut.render(data, writer)
        // Then
        verify(exactly = 0) { writer.write(any(), any(), any()) }
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
        // Better way to view large diff than using mockk.verify
        Assert.assertEquals("unit", pathSlot.captured)
        Assert.assertEquals(expected, bodySlot.captured)
        Assert.assertEquals(false, indexSlot.captured)
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
        // Better way to view large diff than using mockk.verify
        Assert.assertEquals("unit", pathSlot.captured)
        Assert.assertEquals(expected, bodySlot.captured)
        Assert.assertEquals(false, indexSlot.captured)
    }

    @Test
    fun test_writeUnit_ShouldDeclareAliasWithTheRightCase() {
        // Given
        val sut = UnitRenderer.of(mapOf(Pair("MJ", UnitValue("MJ", 1.0, Prelude.length.pow(2.0)))))
        val data = UnitRow().name("GJ")
            .quantity("Energy")
            .conversionFactor(1000.0)
            .referenceUnit("mj")

        // When
        sut.render(data, writer)

        // Then
        val expected = """

            unit GJ {
                symbol = "GJ"
                alias_for = 1000.0 MJ
            }
            """.trimIndent()
        // Better way to view large diff than using mockk.verify
        Assert.assertEquals("unit", pathSlot.captured)
        Assert.assertEquals(expected, bodySlot.captured)
        Assert.assertEquals(false, indexSlot.captured)
    }

    @Test
    fun test_writeUnit_ShouldDeclareAliasWhenItsNotTheReference() {
        // Given
        val sut = UnitRenderer.of(mapOf(Pair("s", UnitValue("S", 1.0, Prelude.mass))))
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
                alias_for = 2.0 S
            }
""".trimIndent()
        // Better way to view large diff than using mockk.verify
        Assert.assertEquals("unit", pathSlot.captured)
        Assert.assertEquals(expected, bodySlot.captured)
        Assert.assertEquals(false, indexSlot.captured)
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
        verify(atMost = 1) {
            writer.write("unit", any(), false)
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
                "A Unit kg for kg already exists with another dimension, time is not compatible with mass.",
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
        val input = listOf(
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

    @Test
    fun test_getSanitizedComment_whenNoSanitizedUnit_shouldReturnAnEmptyString(){
        // given
        val symbol = "kg"
        val sanitizedSymbol = "kg"
        val sut = UnitRenderer.of(emptyMap())
        // when
        val comment = sut.getSanitizedSymbolComment(symbol, sanitizedSymbol)
        // then
        assertEquals("", comment)
    }

    @Test
    fun test_getSanitizedComment_whenSanitizedUnit_shouldReturnACommentExplainingTheSanitization() {
        // given
        val symbol = "unit"
        val sanitizedSymbol = "u"
        val sut = UnitRenderer.of(emptyMap())
        // when
        val comment = sut.getSanitizedSymbolComment(symbol, sanitizedSymbol)
        // then
        assertEquals(" // unit", comment)
    }
}