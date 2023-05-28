package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.*
import org.junit.Assert.assertEquals
import org.junit.Test

class LcaExpressionReducerTest {

    @Test
    fun reduce_whenProcess_shouldReduceExchanges() {
        // given
        val expression = EProcess(
            name = "p",
            labels = emptyMap(),
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
            dataRegister = Register.from(
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
            labels = emptyMap(),
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
            EIndicatorSpec("cc")
        )
        val reducer = LcaExpressionReducer(
            dataRegister = Register.from(
                hashMapOf(
                    Pair("q", QuantityFixture.oneKilogram),
                )
            ),
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EImpact(
            QuantityFixture.oneKilogram,
            EIndicatorSpec("cc"),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenTechnoExchange_shouldReduceQuantity() {
        // given
        val expression = ETechnoExchange(
            EQuantityRef("q"),
            EProductSpec("carrot"),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = Register.from(
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
            EProductSpec("carrot"),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenBioExchange_shouldReduceQuantityAndSubstance() {
        // given
        val expression = EBioExchange(
            EQuantityRef("q"),
            ESubstanceSpec("propanol"),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = Register.from(
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
            ESubstanceSpec("propanol"),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenIndicator_shouldReduceUnit() {
        // given
        val expression = EIndicatorSpec(
            "cc",
            EQuantityRef("kg"),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = Register.from(
                hashMapOf(
                    Pair("kg", UnitFixture.kg)
                )
            ),
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EIndicatorSpec(
            "cc",
            QuantityFixture.oneKilogram,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenSubstance_shouldReduceUnit() {
        // given
        val expression = ESubstanceSpec(
            "propanol",
            "propanol",
            type = SubstanceType.RESOURCE,
            "air",
            null,
            EQuantityRef("kg"),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = Register.from(
                hashMapOf(
                    Pair("kg", UnitFixture.kg)
                )
            )
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = ESubstanceSpec(
            "propanol",
            "propanol",
            type = SubstanceType.RESOURCE,
            "air",
            null,
            QuantityFixture.oneKilogram,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenProduct_shouldReduceUnit() {
        // given
        val expression = EProductSpec(
            "carrot",
            EQuantityRef("kg"),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = Register.from(
                hashMapOf(
                    Pair("kg", UnitFixture.kg)
                )
            ),
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProductSpec(
            "carrot",
            QuantityFixture.oneKilogram,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_withoutFromProcessRef_shouldReduceProduct() {
        // given
        val expression = EProductSpec(
            "carrot",
            EQuantityRef("kg"),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = Register.from(
                hashMapOf(
                    Pair("kg", UnitFixture.kg)
                )
            ),
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProductSpec(
            "carrot",
            QuantityFixture.oneKilogram,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_withConstraintFromProcess_shouldReduceProductAndArguments() {
        // given
        val expression = EProductSpec(
            "carrot",
            UnitFixture.kg,
            FromProcess(
                "p",
                mapOf(
                    Pair("x", EQuantityRef("q"))
                )
            )
        )
        val reducer = LcaExpressionReducer(
            dataRegister = Register.from(
                hashMapOf(
                    Pair("q", EQuantityScale(3.0, EQuantityRef("kg"))),
                    Pair("kg", UnitFixture.kg)
                )
            )
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProductSpec(
            "carrot",
            QuantityFixture.oneKilogram,
            FromProcess(
                "p",
                mapOf(
                    Pair("x", EQuantityScale(3.0, UnitFixture.kg))
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
            dataRegister = Register.from(
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
