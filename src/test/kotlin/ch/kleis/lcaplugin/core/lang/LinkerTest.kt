package ch.kleis.lcaplugin.core.lang

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test


class LinkerTest {
    @Test
    fun run_whenConflictingVariableUse_importWildcard() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val dep = Package(
            "dep",
            emptyList(),
            Environment.of(
                Pair("x", EQuantity(1.0, kg))
            )
        )
        val abc = Package(
            "abc",
            listOf(ImportWildCard("dep")),
            Environment.of(
                Pair("x", EQuantity(2.0, kg)),
                Pair("main", EAdd(EVar("x"), EQuantity(3.0, kg))),
            )
        )
        val entryPoint = EntryPoint(abc, "main")
        val linker = Linker(
            entryPoint,
            listOf(dep),
        )

        // when/then
        try {

            linker.link()
            fail("should have thrown LinkerException")
        } catch (e : LinkerException) {
            // success
        }
    }

    @Test
    fun run_basic() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val abc = Package(
            "abc",
            emptyList(),
            Environment.of(
                Pair("x", EQuantity(1.0, kg)),
            )
        )
        val entryPoint = EntryPoint(abc, "x")
        val linker = Linker(
            entryPoint,
            emptyList(),
        )

        // when
        val (_, actual) = linker.link()

        // then
        val expected = Environment.of(
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
            Environment.of(
                Pair("x", EQuantity(1.0, kg)),
                Pair("y", EVar("x")),
            )
        )
        val entryPoint = EntryPoint(abc, "x")
        val linker = Linker(
            entryPoint,
            emptyList(),
        )

        // when
        val (_, actual) = linker.link()

        // then
        val expected = Environment.of(
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
            Environment.of(
                Pair("x", EQuantity(1.0, kg))
            )
        )
        val abc = Package(
            "abc",
            listOf(ImportSymbol("dep", "x")),
            Environment.of(
                Pair("y", EVar("x")),
            )
        )
        val entryPoint = EntryPoint(abc, "y")
        val linker = Linker(
            entryPoint,
            listOf(dep),
        )

        // when
        val (_, actual) = linker.link()

        // then
        val expected = Environment.of(
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
            Environment.of(
                Pair("z", EQuantity(1.0, kg)),
                Pair("x", EVar("z")),
            )
        )
        val abc = Package(
            "abc",
            listOf(ImportSymbol("dep", "x")),
            Environment.of(
                Pair("y", EVar("x")),
            )
        )
        val entryPoint = EntryPoint(abc, "y")
        val linker = Linker(
            entryPoint,
            listOf(dep),
        )

        // when
        val (_, actual) = linker.link()

        // then
        val expected = Environment.of(
            Pair("dep.z", EQuantity(1.0, kg)),
            Pair("dep.x", EVar("dep.z")),
            Pair("abc.y", EVar("dep.x")),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun run_wildcard() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val dep = Package(
            "dep",
            emptyList(),
            Environment.of(
                Pair("z", EQuantity(1.0, kg)),
                Pair("x", EVar("z")),
            )
        )
        val abc = Package(
            "abc",
            listOf(ImportWildCard("dep")),
            Environment.of(
                Pair("y", EVar("x")),
            )
        )
        val entryPoint = EntryPoint(abc, "y")
        val linker = Linker(
            entryPoint,
            listOf(dep),
        )

        // when
        val (_, actual) = linker.link()

        // then
        val expected = Environment.of(
            Pair("dep.z", EQuantity(1.0, kg)),
            Pair("dep.x", EVar("dep.z")),
            Pair("abc.y", EVar("dep.x")),
        )
        assertEquals(expected, actual)
    }
}
