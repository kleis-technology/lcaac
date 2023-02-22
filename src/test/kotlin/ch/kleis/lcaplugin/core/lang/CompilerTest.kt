package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.prelude.Prelude
import org.junit.Assert.assertEquals
import org.junit.Test


class CompilerTest {
    @Test
    fun run() {
        // given
        val abc = Package(
            "abc",
            listOf(ImportSymbol("prelude.units", "kg")),
            Environment.of(
                Pair("x", EVar("kg")),
            )
        )
        val entryPoint = EntryPoint(abc, "x")
        val program = Compiler(entryPoint, Prelude.packages.values.toList()).compile()

        // when
        val actual = program.run()

        // then
        val expected = VUnit("kg", 1.0, Dimension.of("mass"))
        assertEquals(expected, actual)
    }
}
