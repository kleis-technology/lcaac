package ch.kleis.lcaac.core.lang.evaluator.reducer

import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.register.DataRegister
import ch.kleis.lcaac.core.lang.register.Register
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.fixture.DimensionFixture
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture
import ch.kleis.lcaac.core.lang.fixture.UnitFixture
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.junit.jupiter.api.Test
import kotlin.math.pow
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DataExpressionReducerTest {
    private val ops = BasicOperations

    /*
        QUANTITIES
     */

    @Test
    fun reduce_whenUnitLiteral_shouldSetToNormalForm() {
        // given
        val unit = EUnitLiteral<BasicNumber>(UnitSymbol.of("a"), 123.0, Dimension.of("a"))
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
        )

        // when
        val actual = reducer.reduce(unit)

        // then
        val expected = EQuantityScale(ops.pure(1.0), unit)
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenScale_shouldReduce() {
        // given
        val innerQuantity = QuantityFixture.oneKilogram
        val quantity = EQuantityScale(ops.pure(2.0), innerQuantity)
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
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
        val quantity = EQuantityScale(ops.pure(1.0), QuantityFixture.twoKilograms)
        val reducer = DataExpressionReducer(Register.empty(), ops)
        val expected = EQuantityScale(ops.pure(2.0), UnitFixture.kg)

        // when
        val actual = reducer.reduce(quantity)

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenScaleAndUnboundRef_shouldDoNothing() {
        // given
        val innerQuantity = EDataRef<BasicNumber>("a")
        val quantity = EQuantityScale(ops.pure(2.0), innerQuantity)
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
        )

        // when
        val actual = reducer.reduce(quantity)

        // then
        assertEquals(quantity, actual)
    }

    @Test
    fun reduce_whenLiteral_shouldReduceUnit() {
        // given
        val quantityEnvironment = DataRegister(
            mapOf(
                DataKey("kg") to UnitFixture.kg
            )
        )
        val quantity = EQuantityScale(ops.pure(1.0), EDataRef("kg"))
        val reducer = DataExpressionReducer(quantityEnvironment, ops)

        // when
        val actual = reducer.reduce(quantity)

        // then
        val expected = QuantityFixture.oneKilogram
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_add_whenSameDimension_shouldAddAndSelectBiggestScale() {
        // given
        val a = EQuantityScale(ops.pure(2.0), UnitFixture.kg)
        val b = EQuantityScale(ops.pure(1000.0), UnitFixture.g)
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
        )

        // when
        val actual = reducer.reduce(EQuantityAdd(a, b))

        // then
        val expected = EQuantityScale(ops.pure(3.0), UnitFixture.kg)
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_add_whenSameUnitLiteral_shouldAddAsScale() {
        // given
        val a = UnitFixture.kg
        val b = UnitFixture.kg
        val expected = EQuantityScale(ops.pure(2.0), UnitFixture.kg)
        val reducer = DataExpressionReducer(Register.empty(), ops)

        // when
        val actual = reducer.reduce(EQuantityAdd(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_add_whenNotSameDimension_shouldThrowEvaluatorException() {
        // given
        val a = EQuantityScale(ops.pure(2.0), UnitFixture.kg)
        val b = EQuantityScale(ops.pure(1000.0), UnitFixture.m)
        val quantity = EQuantityAdd(a, b)
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
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
        val expected = EQuantityScale(ops.pure(1.001), EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass))
        val reducer = DataExpressionReducer(Register.empty(), ops)

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
        val expected = EQuantityScale(ops.pure(3.0), EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass))
        val reducer = DataExpressionReducer(Register.empty(), ops)

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
        val expected = EQuantityScale(ops.pure(3.0), EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass))
        val reducer = DataExpressionReducer(Register.empty(), ops)

        // when
        val actual = reducer.reduce(EQuantityAdd(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_sub_whenSameDimension_shouldSubAndSelectBiggestScale() {
        // given
        val a = EQuantityScale(ops.pure(2.0), UnitFixture.kg)
        val b = EQuantityScale(ops.pure(1000.0), UnitFixture.g)
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
        )

        // when
        val actual = reducer.reduce(EQuantitySub(a, b))

        // then
        val expected = EQuantityScale(ops.pure(1.0), UnitFixture.kg)
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_sub_whenNotSameDimension_shouldThrowEvaluatorException() {
        // given
        val a = EQuantityScale(ops.pure(2.0), UnitFixture.kg)
        val b = EQuantityScale(ops.pure(1000.0), UnitFixture.m)
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
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
        val expected = EQuantityScale(ops.pure(0.0), UnitFixture.kg)
        val reducer = DataExpressionReducer(Register.empty(), ops)

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
        val expected = EQuantityScale(ops.pure(0.999), EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass))
        val reducer = DataExpressionReducer(Register.empty(), ops)

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
        val expected = EQuantityScale(ops.pure(1.0), EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass))
        val reducer = DataExpressionReducer(Register.empty(), ops)

        // when
        val actual = reducer.reduce(EQuantitySub(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_sub_whenOnlyOneScaleRight_shouldSubAsScale() {
        // given
        val a = UnitFixture.kg
        val b = EQuantityScale(ops.pure(0.5), UnitFixture.kg)
        val expected = EQuantityScale(ops.pure(0.5), EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass))
        val reducer = DataExpressionReducer(Register.empty(), ops)

        // when
        val actual = reducer.reduce(EQuantitySub(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_mul_whenTwoScales_shouldMultiply() {
        // given
        val a = EQuantityScale(ops.pure(2.0), UnitFixture.person)
        val b = EQuantityScale(ops.pure(2.0), UnitFixture.km)
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
        )

        // when
        val actual = reducer.reduce(EQuantityMul(a, b))

        // then
        val expected = EQuantityScale(
            ops.pure(4.0),
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
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
        )

        // when
        val actual = reducer.reduce(EQuantityMul(a, b))

        // then
        val expected = EQuantityScale(
            ops.pure(1.0),
            EUnitLiteral(
                UnitSymbol.of("person").multiply(UnitSymbol.of("km")),
                1.0 * 1000.0,
                Dimension.None.multiply(DimensionFixture.length)
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_mul_whenOnlyOneScaleLeft_shouldMultiply() {
        // given
        val a = UnitFixture.kg
        val b = QuantityFixture.twoKilograms
        val expected =
            EQuantityScale(
                ops.pure(2.0),
                EUnitLiteral(UnitSymbol.of("kg").pow(2.0), 1.0, DimensionFixture.mass.multiply(DimensionFixture.mass))
            )

        // when
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
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
                ops.pure(2.0),
                EUnitLiteral(UnitSymbol.of("kg").pow(2.0), 1.0, DimensionFixture.mass.multiply(DimensionFixture.mass))
            )

        // when
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
        )

        // when
        val actual = reducer.reduce(EQuantityMul(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_div_whenTwoScales_shouldDiv() {
        // given
        val a = EQuantityScale(ops.pure(4.0), UnitFixture.km)
        val b = EQuantityScale(ops.pure(2.0), UnitFixture.hour)
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
        )

        // when
        val actual = reducer.reduce(EQuantityDiv(a, b))

        // then
        val expected = EQuantityScale(
            ops.pure(2.0),
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
        val reducer = DataExpressionReducer(Register.empty(), ops)

        // when
        val actual = reducer.reduce(EQuantityDiv(a, b))

        // then
        val expected = EQuantityScale(
            ops.pure(1.0),
            EUnitLiteral(
                UnitSymbol.of("km").divide(UnitSymbol.of("hour")),
                1000.0 / 3600.0,
                DimensionFixture.length.divide(DimensionFixture.time)
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_div_whenOnlyOneScaleLeft_shouldDiv() {
        // given
        val a = EQuantityScale(ops.pure(4.0), UnitFixture.km)
        val b = UnitFixture.hour
        val expected = EQuantityScale(
            ops.pure(4.0),
            EUnitLiteral(
                UnitSymbol.of("km").divide(UnitSymbol.of("hour")),
                1000.0 / 3600.0,
                DimensionFixture.length.divide(DimensionFixture.time)
            )
        )

        val reducer = DataExpressionReducer(Register.empty(), ops)

        // when
        val actual = reducer.reduce(EQuantityDiv(a, b))

        // then
        assertEquals(expected, actual)

    }

    @Test
    fun reduce_pow_shouldExponentiate() {
        // given
        val a = EQuantityScale(ops.pure(4.0), UnitFixture.km)
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
        )

        // when
        val actual = reducer.reduce(EQuantityPow(a, 2.0))

        // then
        val expected = EQuantityScale(
            ops.pure(16.0),
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
        val a = EDataRef<BasicNumber>("a")
        val reducer = DataExpressionReducer(
            DataRegister(
                mapOf(
                    DataKey("a") to EQuantityScale(ops.pure(1.0), UnitFixture.kg)
                )
            ),
            ops,
        )

        // when
        val actual = reducer.reduce(a)

        // then
        val expected = QuantityFixture.oneKilogram
        assertEquals(expected, actual)
    }

    /*
        UNITS
     */

    @Test
    fun reduce_whenUnitComposition_shouldReturnNormalForm() {
        // given
        val kg = EUnitLiteral<BasicNumber>(UnitSymbol.of("kg"), 1.0, Dimension.of("mass"))
        val quantityConversion = EQuantityScale(ops.pure(2.2), kg)
        val unitComposition = EUnitAlias("lbs", quantityConversion)
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
        )
        // when
        val actual = reducer.reduce(unitComposition)
        // then
        val expect = EQuantityScale(
            ops.pure(1.0),
            EUnitLiteral(UnitSymbol.of("lbs"), scale = 2.2, Dimension.of("mass"))
        )
        assertEquals(actual, expect)
    }

    @Test
    fun reduce_whenUnitComposition_shouldRespectScaling() {
        // given
        val g = EUnitLiteral<BasicNumber>(UnitSymbol.of("g"), 1.0E-3, Dimension.of("mass"))
        val quantityConversion = EQuantityScale(ops.pure(2200.0), g)
        val unitComposition = EUnitAlias("lbs", quantityConversion)
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
        )
        // when
        val actual = reducer.reduce(unitComposition)
        // then
        val expect = EQuantityScale(
            ops.pure(1.0),
            EUnitLiteral(UnitSymbol.of("lbs"), scale = 2.2, Dimension.of("mass"))
        )
        assertEquals(actual, expect)
    }

    @Test
    fun reduce_whenUnitCompositionComposition_shouldDeepReduce() {
        // given
        val expr = EUnitAlias("foo", EUnitAlias("bar", UnitFixture.kg))
        val reducer = DataExpressionReducer(Register.empty(), ops)

        // when
        val actual = reducer.reduce(expr)

        // then
        val expected = EQuantityScale(ops.pure(1.0), EUnitLiteral(UnitSymbol.of("foo"), 1.0, DimensionFixture.mass))
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenUnitClosure_shouldReduceWithGivenTable() {
        // given
        val symbolTable = SymbolTable(
            data = DataRegister(
                mapOf(DataKey("a") to UnitFixture.kg)
            ),
        )
        val unit = EQuantityClosure(symbolTable, EDataRef("a"))
        val reducer = DataExpressionReducer(
            DataRegister(
                mapOf(DataKey("a") to UnitFixture.l)
            ),
            ops,
        )

        // when
        val actual = reducer.reduce(unit)

        // then
        val expected = QuantityFixture.oneKilogram
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenLiteral_shouldReturnNormalForm() {
        // given
        val kg = UnitFixture.kg
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
        )

        // when
        val actual = reducer.reduce(kg)

        // then
        val expected = QuantityFixture.oneKilogram
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenUnitOfUnitLiteral_shouldReturnNormalForm() {
        // given
        val expr = EUnitOf(UnitFixture.l)
        val expected = QuantityFixture.oneLitre
        val reducer = DataExpressionReducer(Register.empty(), ops)

        // when
        val actual = reducer.reduce(expr)

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenUnitOfRef_shouldReturnAsIs() {
        // given
        val expr = EUnitOf(EDataRef<BasicNumber>("beer"))
        val reducer = DataExpressionReducer(Register.empty(), ops)

        // when
        val actual = reducer.reduce(expr)

        // then
        assertEquals(actual, expr)
    }

    @Test
    fun reduce_whenUnitOfComplexExpression_shouldReturnNormalForm() {
        // given
        val expr = EUnitOf(EQuantityMul(UnitFixture.kg, QuantityFixture.twoLitres))
        val expected =
            EQuantityScale(
                ops.pure(1.0),
                EUnitLiteral(
                    UnitSymbol.of("kg").multiply(UnitSymbol.of("l")),
                    1.0e-3,
                    DimensionFixture.mass.multiply(DimensionFixture.volume)
                )
            )
        val reducer = DataExpressionReducer(Register.empty(), ops)

        // when
        val actual = reducer.reduce(expr)

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenUnitOfComplexExpression_opposite_shouldReturnNormalForm() {
        // given
        val expr = EUnitOf(EQuantityMul(QuantityFixture.twoLitres, UnitFixture.kg))
        val expected =
            EQuantityScale(
                ops.pure(1.0),
                EUnitLiteral(
                    UnitSymbol.of("l").multiply(UnitSymbol.of("kg")),
                    1.0e-3,
                    DimensionFixture.mass.multiply(DimensionFixture.volume)
                )
            )
        val reducer = DataExpressionReducer(Register.empty(), ops)

        // when
        val actual = reducer.reduce(expr)

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenUnitOfUnitOfRef_shouldMerge() {
        // given
        val expr = EUnitOf(EUnitOf(EDataRef<BasicNumber>("beer")))
        val reducer = DataExpressionReducer(Register.empty(), ops)

        // when
        val actual = reducer.reduce(expr)

        // then
        val expected = EUnitOf(EDataRef<BasicNumber>("beer"))
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenDiv_shouldDivide() {
        // given
        val kg = UnitFixture.kg
        val l = UnitFixture.l
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
        )

        // when
        val actual = reducer.reduce(EQuantityDiv(kg, l))

        // then
        val expected = EQuantityScale(
            ops.pure(1.0),
            EUnitLiteral(
                UnitSymbol.of("kg").divide(UnitSymbol.of("l")),
                1.0 / 1.0e-3,
                DimensionFixture.mass.divide(DimensionFixture.volume),
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenMul_shouldMultiply() {
        // given
        val kg = UnitFixture.kg
        val l = UnitFixture.l
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
        )

        // when
        val actual = reducer.reduce(EQuantityMul(kg, l))

        // then
        val expected = EQuantityScale(
            ops.pure(1.0),
            EUnitLiteral(
                UnitSymbol.of("kg").multiply(UnitSymbol.of("l")),
                1.0 * 1.0e-3,
                DimensionFixture.mass.multiply(DimensionFixture.volume),
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenPow_shouldPow() {
        // given
        val m = UnitFixture.m
        val reducer = DataExpressionReducer(
            Register.empty(),
            ops,
        )

        // when
        val actual = reducer.reduce(EQuantityPow(m, 2.0))

        // then
        val expected = EQuantityScale(
            ops.pure(1.0),
            EUnitLiteral(
                UnitSymbol.of("m").pow(2.0),
                1.0.pow(2.0),
                DimensionFixture.length.pow(2.0),
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenRef_shouldReadEnv() {
        // given
        val ref = EDataRef<BasicNumber>("kg")
        val units = DataRegister(
            mapOf(
                DataKey("kg") to UnitFixture.kg
            )
        )
        val reducer = DataExpressionReducer(
            units,
            ops,
        )

        // when
        val actual = reducer.reduce(ref)

        // then
        val expected = QuantityFixture.oneKilogram
        assertEquals(expected, actual)
    }


    /*
        Strings
     */

    @Test
    fun reduce_whenStringLiteral() {
        // given
        val expression = EStringLiteral<BasicNumber>("FR")
        val reducer = DataExpressionReducer(
            DataRegister.empty(),
            ops,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        assertEquals(expression, actual)
    }

    @Test
    fun reduce_refToString_whenFound() {
        // given
        val expression = EDataRef<BasicNumber>("geo")
        val reducer = DataExpressionReducer(
            DataRegister(
                mapOf(
                    DataKey("geo") to EDataRef("geo2"),
                    DataKey("geo2") to EStringLiteral("FR"),
                )
            ),
            ops,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EStringLiteral<BasicNumber>("FR")
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_refToString_whenNotFound() {
        // given
        val expression = EDataRef<BasicNumber>("foo")
        val reducer = DataExpressionReducer(
            DataRegister(
                mapOf(
                    DataKey("geo") to EDataRef("geo2"),
                    DataKey("geo2") to EStringLiteral("FR"),
                )
            ),
            ops,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        assertEquals(expression, actual)
    }
}