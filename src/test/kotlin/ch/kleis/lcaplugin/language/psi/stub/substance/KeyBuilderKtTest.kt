package ch.kleis.lcaplugin.language.psi.stub.substance

import org.junit.Test
import kotlin.test.assertEquals

class KeyBuilderKtTest {
    @Test
    fun test_substanceKey() {
        // given
        val fqn = "abc.xyz.co2"
        val type = "Emission"
        val compartment = "air"
        val subCompartment = "low pop"

        // when
        val actual = substanceKey(fqn, type, compartment, subCompartment)

        // then
        val expected = """abc.xyz.co2(type="Emission", compartment="air", sub_compartment="low pop")"""
        assertEquals(expected, actual)
    }

    @Test
    fun test_substanceKey_withoutSubCompartment() {
        // given
        val fqn = "abc.xyz.co2"
        val type = "Emission"
        val compartment = "air"

        // when
        val actual = substanceKey(fqn, type, compartment, null)

        // then
        val expected = """abc.xyz.co2(type="Emission", compartment="air")"""
        assertEquals(expected, actual)
    }
}
