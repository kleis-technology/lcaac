package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*
import org.junit.Assert.assertEquals
import org.junit.Test


class ReducerTest {

    @Test
    fun reduce_var() {
        // given
        val expression = EVar("a")
        val environment = mapOf(Pair("a", EVar("b")))
        val reducer = Reducer(environment)

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EVar("b")
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_let() {
        // given
        val expression = ELet(
            mapOf(
                Pair("a", EVar("x"))
            ), EVar("a")
        )
        val reducer = Reducer(emptyMap())

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EVar("x")
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_let_chaining() {
        // given
        val expression = ELet(
            mapOf(
                Pair("a", EVar("x")),
                Pair("b", EVar("a")),
                Pair("c", EVar("b")),
            ), EVar("c")
        )
        val reducer = Reducer(emptyMap())

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EVar("x")
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_instance() {
        // given
        val template = ETemplate(
            mapOf(
                Pair("x", null)
            ),
            EVar("x")
        )
        val arguments = mapOf<String, Expression>(
            Pair("x", EVar("a"))
        )
        val expression = EInstance(template, arguments)
        val reducer = Reducer(emptyMap())

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EVar("a")
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_instance_withDefaultValue() {
        // given
        val template = ETemplate(
            mapOf(
                Pair("x", EVar("a"))
            ),
            EVar("x")
        )
        val arguments = emptyMap<String, Expression>()
        val expression = EInstance(template, arguments)
        val reducer = Reducer(emptyMap())

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EVar("a")
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_instance_overrideDefaultValue() {
        // given
        val template = ETemplate(
            mapOf(
                Pair("x", EVar("a"))
            ),
            EVar("x")
        )
        val arguments = mapOf<String, Expression>(
            Pair("x", EVar("b"))
        )
        val expression = EInstance(template, arguments)
        val reducer = Reducer(emptyMap())

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EVar("b")
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_instance_overrideDefaultValue_withEnvironment() {
        // given
        val template = ETemplate(
            mapOf(
                Pair("x", EVar("a"))
            ),
            EVar("x")
        )
        val arguments = mapOf<String, Expression>(
            Pair("x", EVar("b"))
        )
        val expression = EInstance(template, arguments)
        val environment = mapOf(
            Pair("b", EVar("c"))
        )
        val reducer = Reducer(environment)

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EVar("c")
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_instance_withTemplateInEnv() {
        val template = ETemplate(
            mapOf(
                Pair("x", EVar("a"))
            ),
            EVar("x")
        )
        val environment = mapOf(
            Pair("f", template),
            Pair("b", EVar("c"))
        )
        val arguments = mapOf<String, Expression>(
            Pair("x", EVar("b"))
        )
        val expression = EInstance(EVar("f"), arguments)
        val reducer = Reducer(environment)

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EVar("c")
        assertEquals(expected, actual)
    }

    @Test
    fun add() {
        // given
        val kg = EUnit("kg", 1.0, "mass")
        val a = EQuantity(1.0, kg)
        val b = EQuantity(1.0, kg)
        val reducer = Reducer(emptyMap())

        // when
        val actual = reducer.reduce(EAdd(a, b))

        // then
        val expected = EQuantity(2.0, kg)
        assertEquals(expected, actual)
    }

    @Test
    fun sub() {
        // given
        val kg = EUnit("kg", 1.0, "mass")
        val a = EQuantity(2.0, kg)
        val b = EQuantity(1.0, kg)
        val reducer = Reducer(emptyMap())

        // when
        val actual = reducer.reduce(ESub(a, b))

        // then
        val expected = EQuantity(1.0, kg)
        assertEquals(expected, actual)
    }

    @Test
    fun mul() {
        // given
        val kg = EUnit("kg", 1.0, "mass")
        val a = EQuantity(2.0, kg)
        val b = EQuantity(2.0, kg)
        val reducer = Reducer(emptyMap())

        // when
        val actual = reducer.reduce(EMul(a, b))

        // then
        val expected = EQuantity(4.0, kg.multiply(kg))
        assertEquals(expected, actual)
    }

    @Test
    fun div() {
        // given
        val person = EUnit("person", 1.0, "person")
        val kg = EUnit("kg", 1.0, "mass")
        val a = EQuantity(4.0, person)
        val b = EQuantity(2.0, kg)
        val reducer = Reducer(emptyMap())

        // when
        val actual = reducer.reduce(EDiv(a, b))

        // then
        val expected = EQuantity(2.0, person.divide(kg))
        assertEquals(expected, actual)
    }

    @Test
    fun pow() {
        // given
        val m = EUnit("m", 1.0, "length")
        val a = EQuantity(2.0, m)
        val reducer = Reducer(emptyMap())

        // when
        val actual = reducer.reduce(EPow(a, 2.0))

        // then
        val expected = EQuantity(4.0, m.pow(2.0))
        assertEquals(expected, actual)
    }

    @Test
    fun block() {
        // given
        val kg = EUnit("kg", 1.0, "mass")
        val carrot = EProduct("carrot", Dimension.of("mass"), kg)
        val expression = EBlock(
            listOf(
                EExchange(EVar("q"), carrot)
            ),
            Polarity.NEGATIVE,
        )
        val environment = mapOf(
            Pair(
                "q", EQuantity(3.0, kg)
            )
        )
        val reducer = Reducer(environment)

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EBlock(
            listOf(
                EExchange(EQuantity(3.0, kg), carrot)
            ),
            Polarity.NEGATIVE,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun process() {
        // given
        val kg = EUnit("kg", 1.0, "mass")
        val carrot = EProduct("carrot", Dimension.of("mass"), kg)
        val water = EProduct("water", Dimension.of("mass"), kg)
        val expression = EProcess(
            listOf(
                EExchange(EVar("p"), carrot),
                EBlock(
                    listOf(
                        EExchange(EVar("q"), water)
                    )
                ),
            ),
        )
        val environment = mapOf(
            Pair(
                "p", EQuantity(4.0, kg),
            ),
            Pair(
                "q", EQuantity(3.0, kg),
            ),
        )
        val reducer = Reducer(environment)

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProcess(
            listOf(
                EExchange(EQuantity(4.0, kg), carrot),
                EExchange(EQuantity(3.0, kg), water),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun system() {
        // given
        val kg = EUnit("kg", 1.0, "mass")
        val carrot = EProduct("carrot", Dimension.of("mass"), kg)
        val water = EProduct("water", Dimension.of("mass"), kg)
        val expression = ESystem(
            listOf(
                EProcess(
                    listOf(
                        EExchange(EVar("p"), carrot)
                    )
                ),
                ESystem(
                    listOf(
                        EProcess(
                            listOf(
                                EExchange(EVar("q"), water)
                            )
                        ),
                    )
                ),
            ),
        )
        val environment = mapOf(
            Pair(
                "p", EQuantity(4.0, kg),
            ),
            Pair(
                "q", EQuantity(3.0, kg),
            ),
        )
        val reducer = Reducer(environment)

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = ESystem(
            listOf(
                EProcess(
                    listOf(
                        EExchange(EQuantity(4.0, kg), carrot)
                    )
                ),
                EProcess(
                    listOf(
                        EExchange(EQuantity(3.0, kg), water)
                    )
                ),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun process_nested() {
        // given
        val kg = EUnit("kg", 1.0, "mass")
        val carrot = EProduct("carrot", Dimension.of("mass"), kg)
        val water = EProduct("water", Dimension.of("mass"), kg)
        val expression = EProcess(
            listOf(
                EExchange(EQuantity(2.0, kg), carrot),
                EProcess(
                    listOf(
                        EExchange(EQuantity(4.0, kg), water),
                    )
                )
            )
        )
        val reducer = Reducer(emptyMap())

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProcess(
            listOf(
                EExchange(EQuantity(2.0, kg), carrot),
                EExchange(EQuantity(4.0, kg), water),
            )
        )
        assertEquals(expected, actual)
    }
}
