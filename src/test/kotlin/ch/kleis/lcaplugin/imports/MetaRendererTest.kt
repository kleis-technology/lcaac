package ch.kleis.lcaplugin.imports

import ch.kleis.lcaplugin.imports.ecospold.model.Classification
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MetaRendererTest {

    private val sut = MetaRenderer()
    private val result = mutableMapOf<String, String>()


    @After
    fun after() {
        unmockkAll()
    }

    @Test
    fun render_ShouldReturnEmpty_WithNull() {
        // Given

        // When
        sut.render(null, "bidon", result)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun render_ShouldReturnKeyValue_WithString() {
        // Given

        // When
        sut.render("Georges", "name", result)

        // Then
        assertEquals(mapOf("name" to "Georges"), result)
    }

    @Test
    fun render_ShouldReturnKeyValue_WithMap() {
        // Given
        val list = mapOf("name" to "Georges")

        // When
        sut.render(list, "", result)

        // Then
        assertEquals(mapOf("name" to "Georges"), result)
    }

    @Test
    fun render_ShouldReturnKeyValue_WithAccumulatedPrefix() {
        // Given
        val subList = mapOf("level2" to "Georges")
        val list = mapOf("level1" to subList)

        // When
        sut.render(list, "", result)

        // Then
        assertEquals(mapOf("level1.level2" to "Georges"), result)
    }

    @Test
    fun render_ShouldReturnKeyValue_WithArray() {
        // Given
        val list = listOf("Georges", "Michael")

        // When
        sut.render(list, "", result)

        // Then
        assertEquals(mapOf("1" to "Georges", "2" to "Michael"), result)
    }

    @Test
    fun render_ShouldReturnMapWithProperties_WithEcospoldObject() {
        // Given
        val desc = Classification("systemValue", "valueValue")

        // When
        sut.render(desc, "", result)

        // Then
        assertEquals(mapOf("system" to "systemValue", "value" to "valueValue"), result)
    }
}