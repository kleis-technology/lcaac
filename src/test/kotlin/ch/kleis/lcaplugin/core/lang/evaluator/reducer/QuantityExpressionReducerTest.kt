package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.UnitSymbol
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.DimensionFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnitFixture
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.pow
import kotlin.test.assertFailsWith

class QuantityExpressionReducerTest {
    /*
        QUANTITIES
     */

    @Test
    fun reduce_whenScale_shouldReduce() {
        // given
        val innerQuantity = QuantityFixture.oneKilogram
        val quantity = EQuantityScale(2.0, innerQuantity)
        val reducer = QuantityExpressionReducer(
            Register.empty()
        )

        // when
        val actual = reducer.reduce(quantity)

        // then
        val expected = QuantityFixture.twoKilograms
        assertEquals(expected, actual)
    }

    @Test
    fun test_reduce_whenScaleOfScale_shouldReduce() {
        // given
        val quantity = EQuantityScale(1.0, QuantityFixture.twoKilograms)
        val reducer = QuantityExpressionReducer(Register.empty())
        val expected = EQuantityScale(2.0, UnitFixture.kg)

        // when
        val actual = reducer.reduce(quantity)

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenScaleAndUnboundRef_shouldDoNothing() {
        // given
        val innerQuantity = EQuantityRef("a")
        val quantity = EQuantityScale(2.0, innerQuantity)
        val reducer = QuantityExpressionReducer(
            Register.empty()
        )

        // when
        val actual = reducer.reduce(quantity)

        // then
        assertEquals(quantity, actual)
    }

    @Test
    fun reduce_whenLiteral_shouldReduceUnit() {
        // given
        val quantityEnvironment: Register<QuantityExpression> = Register.from(
            hashMapOf(
                Pair("kg", UnitFixture.kg)
            )
        )
        val quantity = EQuantityScale(1.0, EQuantityRef("kg"))
        val reducer = QuantityExpressionReducer(quantityEnvironment)

        // when
        val actual = reducer.reduce(quantity)

        // then
        val expected = UnitFixture.kg
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_add_whenSameDimension_shouldAddAndSelectBiggestScale() {
        // given
        val a = EQuantityScale(2.0, UnitFixture.kg)
        val b = EQuantityScale(1000.0, UnitFixture.g)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(EQuantityAdd(a, b))

        // then
        val expected = EQuantityScale(3.0, UnitFixture.kg)
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_add_whenSameUnitLiteral_shouldAddAsScale() {
        // given
        val a = UnitFixture.kg
        val b = UnitFixture.kg
        val expected = EQuantityScale(2.0, UnitFixture.kg)
        val reducer = QuantityExpressionReducer(Register.empty())

        // when
        val actual = reducer.reduce(EQuantityAdd(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_add_whenNotSameDimension_shouldThrowEvaluatorException() {
        // given
        val a = EQuantityScale(2.0, UnitFixture.kg)
        val b = EQuantityScale(1000.0, UnitFixture.m)
        val quantity = EQuantityAdd(a, b)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
        )

        // when/then
        assertFailsWith(
            EvaluatorException::class,
            "incompatible dimensions: mass vs length in left=2.0 kg and right=1000.0 m"
        ) { reducer.reduce(quantity) }
    }

    @Test
    fun reduce_add_whenSameDimensionLiteral_shouldAddAsScale() {
        // given
        val a = UnitFixture.g
        val b = UnitFixture.kg
        val expected = EQuantityScale(1.001, EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass))
        val reducer = QuantityExpressionReducer(Register.empty())

        // when
        val actual = reducer.reduce(EQuantityAdd(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_add_whenOnlyOneScaleLeft_shouldAddAsScale() {
        // given
        val a = QuantityFixture.twoKilograms
        val b = UnitFixture.kg
        val expected = EQuantityScale(3.0, EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass))
        val reducer = QuantityExpressionReducer(Register.empty())

        // when
        val actual = reducer.reduce(EQuantityAdd(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_add_whenOnlyOneScaleRight_shouldAddAsScale() {
        // given
        val a = UnitFixture.kg
        val b = QuantityFixture.twoKilograms
        val expected = EQuantityScale(3.0, EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass))
        val reducer = QuantityExpressionReducer(Register.empty())

        // when
        val actual = reducer.reduce(EQuantityAdd(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_sub_whenSameDimension_shouldSubAndSelectBiggestScale() {
        // given
        val a = EQuantityScale(2.0, UnitFixture.kg)
        val b = EQuantityScale(1000.0, UnitFixture.g)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(EQuantitySub(a, b))

        // then
        val expected = EQuantityScale(1.0, UnitFixture.kg)
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_sub_whenNotSameDimension_shouldThrowEvaluatorException() {
        // given
        val a = EQuantityScale(2.0, UnitFixture.kg)
        val b = EQuantityScale(1000.0, UnitFixture.m)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
        )
        val eQuantitySub = EQuantitySub(a, b)

        // when/then
        assertFailsWith(
            EvaluatorException::class,
            "incompatible dimensions: mass vs length in left=2.0 kg and right=1000.0 m"
        ) { reducer.reduce(eQuantitySub) }
    }

    @Test
    fun reduce_sub_whenSameUnitLiteral_shouldSubAsScale() {
        // given
        val a = UnitFixture.kg
        val b = UnitFixture.kg
        val expected = EQuantityScale(0.0, UnitFixture.kg)
        val reducer = QuantityExpressionReducer(Register.empty())

        // when
        val actual = reducer.reduce(EQuantitySub(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_sub_whenSameDimensionLiteral_shouldSubAsScale() {
        // given
        val a = UnitFixture.kg
        val b = UnitFixture.g
        val expected = EQuantityScale(0.999, EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass))
        val reducer = QuantityExpressionReducer(Register.empty())

        // when
        val actual = reducer.reduce(EQuantitySub(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_sub_whenOnlyOneScaleLeft_shouldSubAsScale() {
        // given
        val a = QuantityFixture.twoKilograms
        val b = UnitFixture.kg
        val expected = EQuantityScale(1.0, EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass))
        val reducer = QuantityExpressionReducer(Register.empty())

        // when
        val actual = reducer.reduce(EQuantitySub(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_sub_whenOnlyOneScaleRight_shouldSubAsScale() {
        // given
        val a = UnitFixture.kg
        val b = EQuantityScale(0.5, UnitFixture.kg)
        val expected = EQuantityScale(0.5, EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass))
        val reducer = QuantityExpressionReducer(Register.empty())

        // when
        val actual = reducer.reduce(EQuantitySub(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_mul_whenTwoScales_shouldMultiply() {
        // given
        val a = EQuantityScale(2.0, UnitFixture.person)
        val b = EQuantityScale(2.0, UnitFixture.km)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(EQuantityMul(a, b))

        // then
        val expected = EQuantityScale(
            4.0,
            EUnitLiteral(
                UnitSymbol.of("person").multiply(UnitSymbol.of("km")),
                1.0 * 1000.0,
                Dimension.None.multiply(DimensionFixture.length)
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_mul_whenTwoUnits_shouldMultiply() {
        // given
        val a = UnitFixture.person
        val b = UnitFixture.km
        val expected = EUnitLiteral(
            UnitSymbol.of("person").multiply(UnitSymbol.of("km")),
            1.0 * 1000.0,
            Dimension.None.multiply(DimensionFixture.length)
        )
        val reducer = QuantityExpressionReducer(
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(EQuantityMul(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_mul_whenOnlyOneScaleLeft_shouldMultiply() {
        // given
        val a = UnitFixture.kg
        val b = QuantityFixture.twoKilograms
        val expected =
            EQuantityScale(
                2.0,
                EUnitLiteral(UnitSymbol.of("kg").pow(2.0), 1.0, DimensionFixture.mass.multiply(DimensionFixture.mass))
            )

        // when
        val reducer = QuantityExpressionReducer(
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(EQuantityMul(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_mul_whenOnlyOneScaleRight_shouldMultiply() {
        // given
        val a = QuantityFixture.twoKilograms
        val b = UnitFixture.kg
        val expected =
            EQuantityScale(
                2.0,
                EUnitLiteral(UnitSymbol.of("kg").pow(2.0), 1.0, DimensionFixture.mass.multiply(DimensionFixture.mass))
            )

        // when
        val reducer = QuantityExpressionReducer(
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(EQuantityMul(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_div_whenTwoScales_shouldDiv() {
        // given
        val a = EQuantityScale(4.0, UnitFixture.km)
        val b = EQuantityScale(2.0, UnitFixture.hour)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(EQuantityDiv(a, b))

        // then
        val expected = EQuantityScale(
            2.0,
            EUnitLiteral(
                UnitSymbol.of("km").divide(UnitSymbol.of("hour")),
                1000.0 / 3600.0,
                DimensionFixture.length.divide(DimensionFixture.time)
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_div_whenTwoUnits_shouldDiv() {
        // given
        val a = UnitFixture.km
        val b = UnitFixture.hour
        val expected = EUnitLiteral(
            UnitSymbol.of("km").divide(UnitSymbol.of("hour")),
            1000.0 / 3600.0,
            DimensionFixture.length.divide(DimensionFixture.time)
        )
        val reducer = QuantityExpressionReducer(Register.empty())

        // when
        val actual = reducer.reduce(EQuantityDiv(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_div_whenOnlyOneScaleLeft_shouldDiv() {
        // given
        val a = EQuantityScale(4.0, UnitFixture.km)
        val b = UnitFixture.hour
        val expected = EQuantityScale(
            4.0,
            EUnitLiteral(
                UnitSymbol.of("km").divide(UnitSymbol.of("hour")),
                1000.0 / 3600.0,
                DimensionFixture.length.divide(DimensionFixture.time)
            )
        )
        val reducer = QuantityExpressionReducer(Register.empty())

        // when
        val actual = reducer.reduce(EQuantityDiv(a, b))

        // then
        assertEquals(expected, actual)

    }

    @Test
    fun reduce_pow_shouldExponentiate() {
        // given
        val a = EQuantityScale(4.0, UnitFixture.km)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(EQuantityPow(a, 2.0))

        // then
        val expected = EQuantityScale(
            16.0,
            EUnitLiteral(
                UnitSymbol.of("km").pow(2.0),
                1e6,
                DimensionFixture.length.pow(2.0)
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenRef_shouldReadEnvironment() {
        // given
        val a = EQuantityRef("a")
        val reducer = QuantityExpressionReducer(
            Register.from(
                hashMapOf(
                    Pair("a", EQuantityScale(1.0, UnitFixture.kg))
                )
            ),
        )

        // when
        val actual = reducer.reduce(a)

        // then
        val expected = UnitFixture.kg
        assertEquals(expected, actual)
    }

    /*
        UNITS
     */

    @Test
    fun reduce_whenUnitComposition_shouldReturnEUnitLiteral() {
        // given
        val kg = EUnitLiteral(UnitSymbol.of("kg"), 1.0, Dimension.of("mass"))
        val quantityConversion = EQuantityScale(2.2, kg)
        val unitComposition = EUnitAlias("lbs", quantityConversion)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
        )
        // when
        val actual = reducer.reduce(unitComposition)
        // then
        val expect = EUnitLiteral(UnitSymbol.of("lbs"), scale = 2.2, Dimension.of("mass"))
        assertEquals(actual, expect)
    }

    @Test
    fun reduce_whenUnitComposition_shouldRespectScaling() {
        // given
        val g = EUnitLiteral(UnitSymbol.of("g"), 1.0E-3, Dimension.of("mass"))
        val quantityConversion = EQuantityScale(2200.0, g)
        val unitComposition = EUnitAlias("lbs", quantityConversion)
        val reducer = QuantityExpressionReducer(
            Register.empty(),
        )
        // when
        val actual = reducer.reduce(unitComposition)
        // then
        val expect = EUnitLiteral(UnitSymbol.of("lbs"), scale = 2.2, Dimension.of("mass"))
        assertEquals(actual, expect)
    }

    @Test
    fun reduce_whenUnitCompositionComposition_shouldDeepReduce() {
        // given
        val expr = EUnitAlias("foo", EUnitAlias("bar", UnitFixture.kg))
        val expected = EUnitLiteral(UnitSymbol.of("foo"), 1.0, DimensionFixture.mass)
        val reducer = QuantityExpressionReducer(Register.empty())

        // when
        val actual = reducer.reduce(expr)

        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenUnitClosure_shouldReduceWithGivenTable() {
        // given
        val symbolTable = SymbolTable(
            quantities = Register.from(
                mapOf("a" to UnitFixture.kg)
            )
        )
        val unit = EQuantityClosure(symbolTable, EQuantityRef("a"))
        val reducer = QuantityExpressionReducer(
            Register.from(
                mapOf("a" to UnitFixture.l)
            )
        )

        // when
        val actual = reducer.reduce(unit)

        // then
        val expected = UnitFixture.kg
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenLiteral_shouldReturnSame() {
        // given
        val kg = UnitFixture.kg
        val reducer = QuantityExpressionReducer(
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(kg)

        // then
        assertEquals(kg, actual)
    }

    @Test
    fun reduce_whenUnitOfUnitLiteral_shouldReturnUnitLiteral() {
        // given
        val expr = EUnitOf(UnitFixture.l)
        val expected = UnitFixture.l
        val reducer = QuantityExpressionReducer(Register.empty())

        // when
        val actual = reducer.reduce(expr)

        // then
        assertEquals(actual, expected)
    }

    @Test
    fun reduce_whenUnitOfRef_shouldReturnAsIs() {
        // given
        val expr = EUnitOf(EQuantityRef("beer"))
        val reducer = QuantityExpressionReducer(Register.empty())

        // when
        val actual = reducer.reduce(expr)

        // then
        assertEquals(actual, expr)
    }

    @Test
    fun reduce_whenUnitOfComplexExpression_shouldReturnUnitLiteral() {
        // given
        val expr = EUnitOf(EQuantityMul(UnitFixture.kg, QuantityFixture.twoLitres))
        val expected =
            EUnitLiteral(UnitSymbol.of("2.0 kg.l"), 2.0e-3, DimensionFixture.mass.multiply(DimensionFixture.volume))
        val reducer = QuantityExpressionReducer(Register.empty())

        // when
        val actual = reducer.reduce(expr)

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenUnitOfComplexExpression_opposite_shouldReturnUnitLiteral() {
        // given
        val expr = EUnitOf(EQuantityMul(QuantityFixture.twoLitres, UnitFixture.kg))
        val expected =
            EUnitLiteral(UnitSymbol.of("2.0 l.kg"), 2.0e-3, DimensionFixture.mass.multiply(DimensionFixture.volume))
        val reducer = QuantityExpressionReducer(Register.empty())

        // when
        val actual = reducer.reduce(expr)

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenUnitOfUnitOfRef_shouldReturnUnitOfRef() {
        // given
        val expr = EUnitOf(EUnitOf(EQuantityRef("beer")))
        val expected = EUnitOf(EQuantityRef("beer"))
        val reducer = QuantityExpressionReducer(Register.empty())

        // when
        val actual = reducer.reduce(expr)

        // then
        assertEquals(actual, expected)
    }

    @Test
    fun reduce_whenDiv_shouldDivide() {
        // given
        val kg = UnitFixture.kg
        val l = UnitFixture.l
        val reducer = QuantityExpressionReducer(
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(EQuantityDiv(kg, l))

        // then
        val expected = EUnitLiteral(
            UnitSymbol.of("kg").divide(UnitSymbol.of("l")),
            1.0 / 1.0e-3,
            DimensionFixture.mass.divide(DimensionFixture.volume),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenMul_shouldMultiply() {
        // given
        val kg = UnitFixture.kg
        val l = UnitFixture.l
        val reducer = QuantityExpressionReducer(
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(EQuantityMul(kg, l))

        // then
        val expected = EUnitLiteral(
            UnitSymbol.of("kg").multiply(UnitSymbol.of("l")),
            1.0 * 1.0e-3,
            DimensionFixture.mass.multiply(DimensionFixture.volume),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenPow_shouldPow() {
        // given
        val m = UnitFixture.m
        val reducer = QuantityExpressionReducer(
            Register.empty(),
        )

        // when
        val actual = reducer.reduce(EQuantityPow(m, 2.0))

        // then
        val expected = EUnitLiteral(
            UnitSymbol.of("m").pow(2.0),
            1.0.pow(2.0),
            DimensionFixture.length.pow(2.0),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenRef_shouldReadEnv() {
        // given
        val ref = EQuantityRef("kg")
        val units: Register<QuantityExpression> = Register.from(
            hashMapOf(
                Pair("kg", UnitFixture.kg)
            )
        )
        val reducer = QuantityExpressionReducer(
            units,
        )

        // when
        val actual = reducer.reduce(ref)

        // then
        assertEquals(UnitFixture.kg, actual)
    }
}
