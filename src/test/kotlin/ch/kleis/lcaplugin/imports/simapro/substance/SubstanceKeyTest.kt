package ch.kleis.lcaplugin.imports.simapro.substance

import org.junit.Assert.assertEquals
import org.junit.Test

class SubstanceKeyTest {

    @Test
    fun uid_ShouldCompactAndLowerCase_WithSubCompartment() {
        // Given + When
        val result = SubstanceKey("_nAme_", "_Emission_", "_coMp_", "_Sub_").uid()

        // Then
        assertEquals("_name_emission_comp_sub", result)
    }

    @Test
    fun uid_ShouldCompactAndLowerCase_WithABlankType() { // Need for Simapro import
        // Given + When
        val result = SubstanceKey("_nAme_", "", "_coMp_", null).uid()

        // Then
        assertEquals("_name_comp", result)
    }

    @Test
    fun uid_ShouldCompactAndLowerCase_WithoutSubCompartment() {
        // Given + When
        val result = SubstanceKey("_nAme_", "_Emission_", "_coMp_", null).uid()

        // Then
        assertEquals("_name_emission_comp", result)
    }

}