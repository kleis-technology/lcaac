package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.*
import org.junit.Assert.assertEquals
import org.junit.Test

class LcaExpressionReducerTest {
    @Test
    fun reduce_whenExchangeWithEUnitOf_shouldReduce() {
        // given
        val innerQuantity = QuantityFixture.twoKilograms
        val unit = EUnitOf(innerQuantity)
        val quantity = EQuantityLiteral(1.0, unit)
        val reducer = LcaExpressionReducer()
        val exchange = ETechnoExchange(
            quantity,
            EConstrainedProduct(
                EProduct(
                    "carrot",
                    unit
                ), None
            )
        )

        // when
        val actual = reducer.reduce(exchange)

        // then
        val expected = ETechnoExchange(
            EQuantityLiteral(1.0, UnitFixture.kg),
            EConstrainedProduct(
                EProduct(
                    "carrot",
                    UnitFixture.kg,
                ), None
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenProcess_shouldReduceExchanges() {
        // given
        val expression = EProcess(
            name = "p",
            products = listOf(
                ETechnoExchange(EQuantityRef("q_carrot"), ProductFixture.carrot),
            ),
            inputs = listOf(
                ETechnoExchange(EQuantityRef("q_water"), ProductFixture.water)
            ),
            biosphere = listOf(
                EBioExchange(EQuantityRef("q_propanol"), SubstanceFixture.propanol),
            ),
        )
        val reducer = LcaExpressionReducer(
            quantityRegister = Register(
                hashMapOf(
                    Pair("q_carrot", QuantityFixture.oneKilogram),
                    Pair("q_water", QuantityFixture.oneLitre),
                    Pair("q_propanol", QuantityFixture.oneKilogram),
                )
            )
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProcess(
            name = "p",
            products = listOf(
                ETechnoExchange(
                    QuantityFixture.oneKilogram,
                    ProductFixture.carrot
                ),
            ),
            inputs = listOf(
                ETechnoExchange(
                    QuantityFixture.oneLitre,
                    ProductFixture.water
                )
            ),
            biosphere = listOf(
                EBioExchange(
                    QuantityFixture.oneKilogram,
                    SubstanceFixture.propanol
                ),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenImpact_shouldReduceQuantityAndIndicator() {
        // given
        val expression = EImpact(
            EQuantityRef("q"),
            EIndicatorRef("cc")
        )
        val reducer = LcaExpressionReducer(
            quantityRegister = Register(
                hashMapOf(
                    Pair("q", QuantityFixture.oneKilogram),
                )
            ),
            indicatorRegister = Register(
                hashMapOf(
                    Pair("cc", IndicatorFixture.climateChange)
                )
            )
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EImpact(
            QuantityFixture.oneKilogram,
            IndicatorFixture.climateChange,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenTechnoExchange_shouldReduceQuantityAndProduct() {
        // given
        val expression = ETechnoExchange(
            EQuantityRef("q"),
            EConstrainedProduct(
                EProductRef("carrot"),
                None,
            )
        )
        val reducer = LcaExpressionReducer(
            productRegister = Register(
                hashMapOf(
                    Pair("carrot", UnconstrainedProductFixture.carrot)
                )
            ),
            quantityRegister = Register(
                hashMapOf(
                    Pair("q", QuantityFixture.oneKilogram),
                )
            ),
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = ETechnoExchange(
            QuantityFixture.oneKilogram,
            ProductFixture.carrot,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenBioExchange_shouldReduceQuantityAndSubstance() {
        // given
        val expression = EBioExchange(
            EQuantityRef("q"),
            ESubstanceRef("propanol"),
        )
        val reducer = LcaExpressionReducer(
            substanceRegister = Register(
                hashMapOf(
                    Pair("propanol", SubstanceFixture.propanol),
                )
            ),
            quantityRegister = Register(
                hashMapOf(
                    Pair("q", QuantityFixture.oneKilogram),
                )
            ),
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EBioExchange(
            QuantityFixture.oneKilogram,
            SubstanceFixture.propanol,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenIndicator_shouldReduceUnit() {
        // given
        val expression = EIndicator(
            "cc",
            EUnitRef("kg"),
        )
        val reducer = LcaExpressionReducer(
            unitRegister = Register(
                hashMapOf(
                    Pair("kg", UnitFixture.kg)
                )
            ),
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EIndicator(
            "cc",
            UnitFixture.kg,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenIndicatorRef_shouldReadEnv() {
        // given
        val expression = EIndicatorRef("cc")
        val reducer = LcaExpressionReducer(
            indicatorRegister = Register(
                hashMapOf(
                    Pair("cc", IndicatorFixture.climateChange)
                )
            )
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = IndicatorFixture.climateChange
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenSubstance_shouldReduceUnit() {
        // given
        val expression = ESubstance(
            "propanol",
            "propanol",
            type = SubstanceType.RESOURCE,
            "air",
            null,
            EUnitRef("kg"),
        )
        val reducer = LcaExpressionReducer(
            unitRegister = Register(
                hashMapOf(
                    Pair("kg", UnitFixture.kg)
                )
            )
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = ESubstance(
            "propanol",
            "propanol",
            type = SubstanceType.RESOURCE,
            "air",
            null,
            UnitFixture.kg,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenSubstanceRef_shouldReadEnv() {
        // given
        val expression = ESubstanceRef("propanol")
        val reducer = LcaExpressionReducer(
            substanceRegister = Register(
                hashMapOf(
                    Pair("propanol", SubstanceFixture.propanol),
                )
            )
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = SubstanceFixture.propanol
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenProduct_shouldReduceUnit() {
        // given
        val expression = EConstrainedProduct(
            EProduct(
                "carrot",
                EUnitRef("kg"),
            ),
            None,
        )
        val reducer = LcaExpressionReducer(
            unitRegister = Register(
                hashMapOf(
                    Pair("kg", UnitFixture.kg)
                )
            ),
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EConstrainedProduct(
            EProduct(
                "carrot",
                UnitFixture.kg,
            ),
            None,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenProductRef_shouldReadEnv() {
        // given
        val carrot = EProduct(
            "carrot",
            EUnitRef("kg"),
        )
        val expression = EConstrainedProduct(
            EProductRef("carrot"),
            None,
        )
        val reducer = LcaExpressionReducer(
            productRegister = Register(
                hashMapOf(
                    Pair("carrot", carrot)
                )
            ),
            unitRegister = Register(
                hashMapOf(
                    Pair("kg", UnitFixture.kg)
                )
            ),
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EConstrainedProduct(
            EProduct(
                "carrot",
                UnitFixture.kg,
            ),
            None,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_withConstraintNone_shouldReduceProduct() {
        // given
        val carrot = EProduct(
            "carrot",
            EUnitRef("kg"),
        )
        val expression = EConstrainedProduct(EProductRef("carrot"), None)
        val reducer = LcaExpressionReducer(
            productRegister = Register(
                hashMapOf(
                    Pair("carrot", carrot)
                )
            ),
            unitRegister = Register(
                hashMapOf(
                    Pair("kg", UnitFixture.kg)
                )
            ),
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EConstrainedProduct(
            EProduct(
                "carrot",
                UnitFixture.kg,
            ), None
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_withConstraintFromProcess_shouldReduceProductAndArguments() {
        // given
        val carrot = EProduct(
            "carrot",
            EUnitRef("kg"),
        )
        val expression = EConstrainedProduct(
            EProductRef("carrot"),
            FromProcessRef(
                "p",
                mapOf(
                    Pair("x", EQuantityRef("q"))
                )
            )
        )
        val reducer = LcaExpressionReducer(
            productRegister = Register(
                hashMapOf(
                    Pair("carrot", carrot)
                )
            ),
            quantityRegister = Register(
                hashMapOf(
                    Pair("q", EQuantityLiteral(3.0, EUnitRef("kg")))
                )
            ),
            unitRegister = Register(
                hashMapOf(
                    Pair("kg", UnitFixture.kg)
                )
            ),
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EConstrainedProduct(
            EProduct(
                "carrot",
                UnitFixture.kg,
            ),
            FromProcessRef(
                "p",
                mapOf(
                    Pair("x", EQuantityLiteral(3.0, UnitFixture.kg))
                )
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenSubstanceCharacterization() {
        // given
        val expression = ESubstanceCharacterization(
            referenceExchange = EBioExchange(
                EQuantityRef("q_propanol"),
                SubstanceFixture.propanol
            ),
            impacts = listOf(
                EImpact(
                    EQuantityRef("q_cc"),
                    IndicatorFixture.climateChange
                ),
            )
        )
        val reducer = LcaExpressionReducer(
            quantityRegister = Register(
                hashMapOf(
                    Pair("q_propanol", QuantityFixture.oneKilogram),
                    Pair("q_cc", QuantityFixture.oneKilogram),
                )
            )
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = ESubstanceCharacterization(
            referenceExchange = EBioExchange(
                QuantityFixture.oneKilogram,
                SubstanceFixture.propanol
            ),
            impacts = listOf(
                EImpact(
                    QuantityFixture.oneKilogram,
                    IndicatorFixture.climateChange
                ),
            )
        )
        assertEquals(expected, actual)
    }
}
