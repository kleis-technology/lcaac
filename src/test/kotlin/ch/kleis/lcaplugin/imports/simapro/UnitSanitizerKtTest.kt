package ch.kleis.lcaplugin.imports.simapro

import junit.framework.TestCase
import org.junit.Test

class UnitSanitizerKtTest {
    @Test
    fun test_sanitizeSymbol_whenNoSanitizeNeeded_shouldReturnSameSymbol(){
        // given
        val symbol = "kg"
        // when
        val sanitizedSymbol = sanitizeUnit(symbol)
        // then
        TestCase.assertEquals(symbol, sanitizedSymbol)
    }

    @Test
    fun test_sanitizeSymbol_whenUnitSymbol_shouldReturnU(){
        // given
        val symbol = "unit"
        // when
        val sanitizedSymbol = sanitizeUnit(symbol)
        // then
        TestCase.assertEquals("_unit", sanitizedSymbol)
    }
}