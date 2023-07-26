package ch.kleis.lcaplugin.imports.shared.serializer

import org.junit.Test
import kotlin.test.assertEquals

class FormulaConverterTest {

    @Test
    fun compute_ShouldReturnLiteralNumbers() {
        // Given
        val data = listOf(
            "12.34" to "12.34",
            "-12.3E4" to "-12.3E4",
            "12.3E-4" to "12.3E-4",
            "1.23E+5" to "1.23E+5",
            "12.3e5" to "12.3e5",
            "12.3e-4" to "12.3e-4",
            "1.23e+5" to "1.23e+5",
            "1.23e5" to "1.23e5",
        )

        data.forEach { (param, expected) ->
            // When
            val comments = mutableListOf<String>()
            val result = FormulaConverter.compute(param, comments)
            // Then
            assertEquals(expected, result)
            assertEquals(0, comments.size)
        }
    }

    @Test
    fun compute_ShouldReturnComputedValue_WithoutVariables() {
        // Given
        val data = listOf(
            "11.11 *7" to "77.77",
            "12.3-4" to "8.3",
            "12.3+4" to "16.3",
            "12.3+\t4" to "16.3",
        )
        data.forEach { (param, expected) ->
            // When
            val comments = mutableListOf<String>()
            val result = FormulaConverter.compute(param, comments)
            // Then
            assertEquals(expected, result)
            assertEquals(1, comments.size)
            assertEquals("Formula=[$param]", comments[0])
        }
    }

    @Test
    fun compute_ShouldReturnAFormulaWithQuantity_WhenFormula() {
        // Given
        val data = listOf(
            "4-E" to "( 4 u - E ) * 1",
            " 0.1486*LUC_crop_specific+0.3654*(1-LUC_crop_specific)" to "( 0.1486 u * LUC_crop_specific + 0.3654 u * ( 1 u - LUC_crop_specific ) ) * 1",
            "0.2345*LUC_crop_specific+0.1555*(1-LUC_crop_specific)" to "( 0.2345 u * LUC_crop_specific + 0.1555 u * ( 1 u - LUC_crop_specific ) ) * 1",
            "0.5476*LUC_crop_specific - 0.0037/(1 +LUC_crop_specific)" to "( 0.5476 u * LUC_crop_specific - 0.0037 u / ( 1 u + LUC_crop_specific ) ) * 1",
            "0.547E6*LUC_crop_specific^2 -\t0.003E-7^2/(1E+2 +LUC_crop_specific)" to "( 0.547E6 u * LUC_crop_specific^2 - 0.003E-7 u^2 / ( 1E+2 u + LUC_crop_specific ) ) * 1",
        )
        data.forEach { (param, expected) ->
            // When
            val comments = mutableListOf<String>()
            val result = FormulaConverter.compute(param, comments)
            // Then
            assertEquals(expected, result)
            assertEquals(1, comments.size)
            assertEquals("Formula=[$param]", comments[0])
        }

    }

}