package ch.kleis.lcaplugin.core.lang.expression

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

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

        // When
        try {
            SubstanceType.of(name)
            fail("Should not pass")
        } catch (e: EvaluatorException) {
            // Then
            assertEquals("Invalid SubstanceType: Bad", e.message)
        }
    }

}