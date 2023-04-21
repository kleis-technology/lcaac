package ch.kleis.lcaplugin.imports

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertEquals

class FormulaConverterTest {

    // ( 824.9999999999999 u * Heavy_metal_uptake ) * 1 kg crop_default_heavy_metals_uptake_wfldb_3_7_glo_u // Formula=[824.9999999999999 * Heavy_metal_uptake]
    data class Par(val param: String, val expected: String, val hasBeenComputed: Boolean)

    @Test
    fun tryToCompute_ShouldReturnLiteralNumbers() {
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
            val (result, changed) = FormulaConverter.tryToCompute(param)
            // Then
            assertEquals(expected, result)
            assertFalse(changed)
        }
    }

    @Test
    fun tryToCompute_ShouldReturnComputedValue_WithoutVariables() {
        // Given
        val data = listOf(
            "11.11 *7" to "77.77",
            "12.3-4" to "8.3",
            "12.3+4" to "16.3",
        )
// Quid "4-E"
        data.forEach { (param, expected) ->
            // When
            val (result, changed) = FormulaConverter.tryToCompute(param)
            // Then
            assertEquals(expected, result)
            assertTrue(changed)
        }
    }

    @Test
    fun tryToCompute_ShouldReturnAFormulaWithQuantity_WhenFormula() {
        // Given
        val expected = "( 0.1486 u * LUC_crop_specific + 0.3654 u * ( 1 u - LUC_crop_specific ) ) * 1"
        // Given
        val data = listOf(
            "4-E" to "( 4 u - E ) * 1",
            " 0.1486*LUC_crop_specific+0.3654*(1-LUC_crop_specific)" to "( 0.1486 u * LUC_crop_specific + 0.3654 u * ( 1 u - LUC_crop_specific ) ) * 1",
            "0.2345*LUC_crop_specific+0.1555*(1-LUC_crop_specific)" to "( 0.2345 u * LUC_crop_specific + 0.1555 u * ( 1 u - LUC_crop_specific ) ) * 1",
            "0.5476*LUC_crop_specific - 0.0037/(1 +LUC_crop_specific)" to "( 0.5476 u * LUC_crop_specific - 0.0037 u / ( 1 u + LUC_crop_specific ) ) * 1",
            "0.547E6*LUC_crop_specific^2 - 0.003E-7^2/(1E+2 +LUC_crop_specific)" to "( 0.547E6 u * LUC_crop_specific^2 - 0.003E-7 u^2 / ( 1E+2 u + LUC_crop_specific ) ) * 1",
        )
        data.forEach { (param, expected) ->
            // When
            val (result, changed) = FormulaConverter.tryToCompute(param)
            // Then
            assertEquals(expected, result)
            assertTrue(changed)
        }

    }

}