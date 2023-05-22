package ch.kleis.lcaplugin.core.lang.expression

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class LcaExpressionTest {
    @Test
    fun test_SubstanceType_ShouldConvertByName() {
        // Given
        val name = "Emission"

        // When
        val sub = SubstanceType.of(name)

        // Then
        assertEquals(SubstanceType.EMISSION, sub)
    }

    @Test
    fun test_SubstanceType_ShouldFailWithInvalidName() {
        // Given
        val name = "Bad"

        // When / then
        val e = assertFailsWith(EvaluatorException::class, null) { SubstanceType.of(name) }
        assertEquals("Invalid SubstanceType: Bad", e.message)
    }

}