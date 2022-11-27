package ch.kleis.lcaplugin.lib.urn

import junit.framework.TestCase.assertEquals
import org.junit.Test


internal class NamespaceTest {

    @Test
    fun testAppend() {
        // given
        val root = Namespace.ROOT
        val a = root.ns("a")
        val b = a.ns("b")

        val c = Namespace("c", null)
        val d = c.ns("d")
        val urn = d.urn("e")

        // when
        val actual = b.append(urn).path()

        // then
        assertEquals(actual[0], root)
        assertEquals(actual[1], a)
        assertEquals(actual[2], b)
        assertEquals(actual[3], b.ns("c"))
        assertEquals(actual[4], b.ns("c").ns("d"))
    }

    @Test
    fun testSelfUrn() {
        // given
        val ns = Namespace.ROOT.ns("a")

        // when
        val actual = ns.selfUrn()

        // then
        assertEquals(actual.uid, ns.uid)
    }

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
        assertEquals(actual, ".1.a1")
    }

    @Test
    fun testFqn_whenNamespace() {
        // given
        val ns1 = Namespace.ROOT.ns("1")
        val ns2 = ns1.ns("2")

        // when
        val actual = ns2.uid

        // then
        assertEquals(actual, ".1.2")
    }
}
