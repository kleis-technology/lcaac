package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*
import org.junit.Assert.assertEquals
import org.junit.Test


class EvaluatorTest {

    @Test
    fun eval() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val l = EUnit("l", 1.0, Dimension.of("volume"))
        val carrot = EProduct("carrot", kg)
        val water = EProduct("water", l)
        val expression = ESystem(
            listOf(
                EProcess(
                    listOf(
                        EExchange(EQuantity(1.0, kg), carrot),
                        EBlock(
                            listOf(
                                EExchange(EQuantity(-3.0, l), water),
                            ),
                        )
                    )
                )
            )
        )
        val evaluator = Evaluator(emptyEnv())

        // when
        val actual = evaluator.eval(expression)

        // then
        val vKg = VUnit("kg", 1.0, Dimension.of("mass"))
        val vL = VUnit("l", 1.0, Dimension.of("volume"))
        val vCarrot = VProduct("carrot", vKg)
        val vWater = VProduct("water", vL)
        val expected = VSystem(
            listOf(
                VProcess(
                    listOf(
                        VExchange(VQuantity(1.0, vKg), vCarrot),
                        VExchange(VQuantity(-3.0, vL), vWater),
                    )
                )
            )
        )
        assertEquals(expected, actual)
    }
}
