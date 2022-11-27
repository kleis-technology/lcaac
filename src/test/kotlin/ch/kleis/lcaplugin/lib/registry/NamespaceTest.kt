package ch.kleis.lcaplugin.lib.registry

import junit.framework.TestCase.assertEquals
import org.junit.Test


internal class NamespaceTest {
    @Test
    fun testResolve_whenAscendant() {
        // given
        val ns1 = Namespace.ROOT.ns("1")
        val a1 = ns1.urn("a1")
        val ns2 = ns1.ns("2")

        // when
        val actual = ns2.resolve("a1")

        // then
        assertEquals(actual, a1)
    }

    @Test
    fun testResolve_whenLocal() {
        // given
        val ns1 = Namespace.ROOT.ns("1")
        val a1 = ns1.urn("a1")

        // when
        val actual = ns1.resolve("a1")

        // then
        assertEquals(actual, a1)
    }

    @Test
    fun testResolve_whenNotFound() {
        // given
        val ns1 = Namespace.ROOT.ns("1")
        ns1.urn("a1")

        // when
        val actual = ns1.resolve("a2")

        // then
        assertEquals(actual, null)
    }

    @Test
    fun testFqn_whenUrn() {
        // given
        val ns1 = Namespace.ROOT.ns("1")
        val a1 = ns1.urn("a1")

        // when
        val actual = a1.uid

        // then
        assertEquals(actual, "/1/a1")
    }
    
    @Test
    fun testFqn_whenNamespace() {
        // given
        val ns1 = Namespace.ROOT.ns("1")
        val ns2 = ns1.ns("2")

        // when
        val actual = ns2.uid

        // then
        assertEquals(actual, "/1/2")
    }
}
