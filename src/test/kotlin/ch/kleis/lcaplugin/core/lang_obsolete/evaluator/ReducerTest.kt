package ch.kleis.lcaplugin.core.lang_obsolete.evaluator

import ch.kleis.lcaplugin.core.lang_obsolete.*
import org.junit.Assert.assertEquals
import org.junit.Test


class ReducerTest {

    @Test
    fun reduce_instance_shouldTemplateByDefault() {
        // given
        val environment = Environment.of(
            Pair("f", EVar("x"))
        )
        val expression = EInstance(
            EVar("f"), mapOf(
                Pair("x", EVar("a"))
            )
        )
        val reducer = Reducer(environment)

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EVar("a")
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_withEnv() {
        // given
        val environment = Environment.of(
            Pair(
                "kg",
                EUnit(
                    "kg",
                    1.0,
                    Dimension.of("mass"),
                )
            ),
            Pair(
                "carrot",
                EProduct(
                    "carrot",
                    EVar("kg")
                )
            ),
        )
        val expression = EVar("carrot")
        val reducer = Reducer(environment)

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProduct(
            "carrot",
            EUnit(
                "kg",
                1.0,
                Dimension.of("mass"),
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun mul_unit() {
        // given
        val a = EUnit("a", 2.0, Dimension.of("A"))
        val b = EUnit("b", 2.0, Dimension.of("B"))
        val expression = EMul(a, b)
        val reducer = Reducer(emptyEnv())

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = a.multiply(b)
        assertEquals(expected, actual)
    }

    @Test
    fun div_unit() {
        // given
        val a = EUnit("a", 2.0, Dimension.of("A"))
        val b = EUnit("b", 2.0, Dimension.of("B"))
        val expression = EDiv(a, b)
        val reducer = Reducer(emptyEnv())

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = a.divide(b)
        assertEquals(expected, actual)
    }

    @Test
    fun pow_unit() {
        // given
        val a = EUnit("a", 2.0, Dimension.of("A"))
        val expression = EPow(a, 2.0)
        val reducer = Reducer(emptyEnv())

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = a.pow(2.0)
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_var() {
        // given
        val expression = EVar("a")
        val environment = Environment.of(Pair("a", EVar("b")))
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
        val reducer = Reducer(emptyEnv())

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
        val reducer = Reducer(emptyEnv())

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
        val reducer = Reducer(emptyEnv())

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
        val reducer = Reducer(emptyEnv())

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
        val reducer = Reducer(emptyEnv())

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
        val environment = Environment.of(
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
        val environment = Environment.of(
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
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val a = EQuantity(1.0, kg)
        val b = EQuantity(1.0, kg)
        val reducer = Reducer(emptyEnv())

        // when
        val actual = reducer.reduce(EAdd(a, b))

        // then
        val expected = EQuantity(2.0, kg)
        assertEquals(expected, actual)
    }

    @Test
    fun sub() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val a = EQuantity(2.0, kg)
        val b = EQuantity(1.0, kg)
        val reducer = Reducer(emptyEnv())

        // when
        val actual = reducer.reduce(ESub(a, b))

        // then
        val expected = EQuantity(1.0, kg)
        assertEquals(expected, actual)
    }

    @Test
    fun mul() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val a = EQuantity(2.0, kg)
        val b = EQuantity(2.0, kg)
        val reducer = Reducer(emptyEnv())

        // when
        val actual = reducer.reduce(EMul(a, b))

        // then
        val expected = EQuantity(4.0, kg.multiply(kg))
        assertEquals(expected, actual)
    }

    @Test
    fun div() {
        // given
        val person = EUnit("person", 1.0, Dimension.of("person"))
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val a = EQuantity(4.0, person)
        val b = EQuantity(2.0, kg)
        val reducer = Reducer(emptyEnv())

        // when
        val actual = reducer.reduce(EDiv(a, b))

        // then
        val expected = EQuantity(2.0, person.divide(kg))
        assertEquals(expected, actual)
    }

    @Test
    fun pow() {
        // given
        val m = EUnit("m", 1.0, Dimension.of("length"))
        val a = EQuantity(2.0, m)
        val reducer = Reducer(emptyEnv())

        // when
        val actual = reducer.reduce(EPow(a, 2.0))

        // then
        val expected = EQuantity(4.0, m.pow(2.0))
        assertEquals(expected, actual)
    }

    @Test
    fun block() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val carrot = EProduct("carrot", kg)
        val expression = EBlock(
            listOf(
                EExchange(EVar("q"), carrot)
            ),
        )
        val environment = Environment.of(
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
        )
        assertEquals(expected, actual)
    }

    @Test
    fun process() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val carrot = EProduct("carrot", kg)
        val water = EProduct("water", kg)
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
        val environment = Environment.of(
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
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val carrot = EProduct("carrot", kg)
        val water = EProduct("water", kg)
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
        val environment = Environment.of(
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
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val carrot = EProduct("carrot", kg)
        val water = EProduct("water", kg)
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
        val reducer = Reducer(emptyEnv())

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

    @Test
    fun negate_quantity() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val q = EQuantity(10.0, kg)
        val reducer = Reducer(emptyEnv())

        // when
        val actual = reducer.reduce(ENeg(q))

        // then
        val expected = EQuantity(-10.0, kg)
        assertEquals(expected, actual)
    }
}
