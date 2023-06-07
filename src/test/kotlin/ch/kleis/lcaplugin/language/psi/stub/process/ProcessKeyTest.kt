package ch.kleis.lcaplugin.language.psi.stub.process

import org.junit.Assert.assertEquals
import org.junit.Test


class ProcessKeyTest {
    @Test
    fun test_processKey_displayName_whenLabels() {
        // given
        val fqn = "abc.xyz.p"
        val labels = mapOf("geo" to "UK", "env" to "PROD")

        // when
        val actual = ProcessKey(fqn, labels).getDisplayName()

        // then
        val expected = """p match (geo = "UK", env = "PROD")"""
        assertEquals(expected, actual)
    }

    @Test
    fun test_processKey_displayName_withoutLabels() {
        // given
        val fqn = "abc.xyz.p"
        val labels = emptyMap<String, String>()

        // when
        val actual = ProcessKey(fqn, labels).getDisplayName()

        // then
        val expected = """p"""
        assertEquals(expected, actual)
    }
}
