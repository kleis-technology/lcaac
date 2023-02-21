package ch.kleis.lcaplugin.core.lang

import org.junit.Assert.assertEquals
import org.junit.Test


class LinkerTest {

    @Test
    fun run_basic() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val abc = Package(
            "abc",
            emptyList(),
            mapOf(
                Pair("x", EQuantity(1.0, kg)),
            )
        )
        val entryPoint = EntryPoint(abc, "x")
        val linker = Linker(
            entryPoint,
            emptySet(),
        )

        // when
        val (_, actual) = linker.run()

        // then
        val expected = mapOf(
            Pair("abc.x", EQuantity(1.0, kg)),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun run_withUse() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val abc = Package(
            "abc",
            emptyList(),
            mapOf(
                Pair("x", EQuantity(1.0, kg)),
                Pair("y", EVar("x")),
            )
        )
        val entryPoint = EntryPoint(abc, "x")
        val linker = Linker(
            entryPoint,
            emptySet(),
        )

        // when
        val (_, actual) = linker.run()

        // then
        val expected = mapOf(
            Pair("abc.x", EQuantity(1.0, kg)),
            Pair("abc.y", EVar("abc.x")),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun run_withImport() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val dep = Package(
            "dep",
            emptyList(),
            mapOf(
                Pair("x", EQuantity(1.0, kg))
            )
        )
        val abc = Package(
            "abc",
            listOf(Import("dep", "x")),
            mapOf(
                Pair("y", EVar("x")),
            )
        )
        val entryPoint = EntryPoint(abc, "y")
        val linker = Linker(
            entryPoint,
            setOf(dep),
        )

        // when
        val (_, actual) = linker.run()

        // then
        val expected = mapOf(
            Pair("dep.x", EQuantity(1.0, kg)),
            Pair("abc.y", EVar("dep.x")),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun run_withImport_transitive() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val dep = Package(
            "dep",
            emptyList(),
            mapOf(
                Pair("z", EQuantity(1.0, kg)),
                Pair("x", EVar("z")),
            )
        )
        val abc = Package(
            "abc",
            listOf(Import("dep", "x")),
            mapOf(
                Pair("y", EVar("x")),
            )
        )
        val entryPoint = EntryPoint(abc, "y")
        val linker = Linker(
            entryPoint,
            setOf(dep),
        )

        // when
        val (_, actual) = linker.run()

        // then
        val expected = mapOf(
            Pair("dep.z", EQuantity(1.0, kg)),
            Pair("dep.x", EVar("dep.z")),
            Pair("abc.y", EVar("dep.x")),
        )
        assertEquals(expected, actual)
    }
}
