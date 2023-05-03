package ch.kleis.lcaplugin.language.psi.stub.substance

import org.junit.Test
import kotlin.test.assertEquals

class SubstanceKeyTest {
    @Test
    fun test_substanceKey() {
        // given
        val fqn = "abc.xyz.co2"
        val type = "Emission"
        val compartment = "air"
        val subCompartment = "low pop"

        // when
        val actual = SubstanceKey(fqn, type, compartment, subCompartment).getDisplayName()

        // then
        val expected = """co2(compartment="air", sub_compartment="low pop")"""
        assertEquals(expected, actual)
    }

    @Test
    fun test_substanceKey_withoutSubCompartment() {
        // given
        val fqn = "abc.xyz.co2"
        val type = "Emission"
        val compartment = "air"

        // when
        val actual = SubstanceKey(fqn, type, compartment, null).getDisplayName()

        // then
        val expected = """co2(compartment="air")"""
        assertEquals(expected, actual)
    }
}
