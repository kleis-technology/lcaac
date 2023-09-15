package ch.kleis.lcaac.core.lang.evaluator.reducer

import ch.kleis.lcaac.core.lang.Register
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.fixture.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LcaExpressionReducerTest {
    private val ops = BasicOperations

    @Test
    fun reduce_whenTechnoExchange_shouldReduceLabelSelectors() {
        // given
        val expression = ETechnoExchange(
            QuantityFixture.oneKilogram,
            EProductSpec(
                "a",
                UnitFixture.kg,
                FromProcess(
                    name = "p",
                    matchLabels = MatchLabels(mapOf("geo" to EDataRef("geo"))),
                )
            )
        )
        val reducer = LcaExpressionReducer(
            Register.from(
                mapOf("geo" to EStringLiteral("FR"))
            ),
            ops,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = ETechnoExchange(
            QuantityFixture.oneKilogram,
            EProductSpec(
                "a",
                QuantityFixture.oneKilogram,
                FromProcess(
                    name = "p",
                    matchLabels = MatchLabels(mapOf("geo" to EStringLiteral("FR"))),
                )
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenProcess_shouldReduceExchanges() {
        // given
        val expression = EProcess(
            name = "p",
            products = listOf(
                ETechnoExchange(EDataRef("q_carrot"), ProductFixture.carrot),
            ),
            inputs = listOf(
                ETechnoExchange(EDataRef("q_water"), ProductFixture.water)
            ),
            biosphere = listOf(
                EBioExchange(EDataRef("q_propanol"), SubstanceFixture.propanol),
            ),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = Register.from(
                hashMapOf(
                    Pair("q_carrot", QuantityFixture.oneKilogram),
                    Pair("q_water", QuantityFixture.oneLitre),
                    Pair("q_propanol", QuantityFixture.oneKilogram),
                )
            ),
            ops,
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
        val expression = EImpact<BasicNumber>(
            EDataRef("q"),
            EIndicatorSpec("cc")
        )
        val reducer = LcaExpressionReducer(
            dataRegister = Register.from(
                hashMapOf(
                    Pair("q", QuantityFixture.oneKilogram),
                )
            ),
            ops,
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
        val expression = ETechnoExchange<BasicNumber>(
            EDataRef("q"),
            EProductSpec("carrot"),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = Register.from(
                hashMapOf(
                    Pair("q", QuantityFixture.oneKilogram),
                )
            ),
            ops,
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
        val expression = EBioExchange<BasicNumber>(
            EDataRef("q"),
            ESubstanceSpec("propanol"),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = Register.from(
                hashMapOf(
                    Pair("q", QuantityFixture.oneKilogram),
                )
            ),
            ops,
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
        val expression = EIndicatorSpec<BasicNumber>(
            "cc",
            EDataRef("kg"),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = Register.from(
                hashMapOf(
                    Pair("kg", UnitFixture.kg)
                )
            ),
            ops,
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
        val expression = ESubstanceSpec<BasicNumber>(
            "propanol",
            "propanol",
            type = SubstanceType.RESOURCE,
            "air",
            null,
            EDataRef("kg"),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = Register.from(
                hashMapOf(
                    Pair("kg", UnitFixture.kg)
                )
            ),
            ops,
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
        val expression = EProductSpec<BasicNumber>(
            "carrot",
            EDataRef("kg"),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = Register.from(
                hashMapOf(
                    Pair("kg", UnitFixture.kg)
                )
            ),
            ops,
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
        val expression = EProductSpec<BasicNumber>(
            "carrot",
            EDataRef("kg"),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = Register.from(
                hashMapOf(
                    Pair("kg", UnitFixture.kg)
                )
            ),
            ops,
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
                MatchLabels(emptyMap()),
                mapOf(
                    Pair("x", EDataRef("q"))
                )
            )
        )
        val reducer = LcaExpressionReducer(
            dataRegister = Register.from(
                hashMapOf(
                    Pair("q", EQuantityScale(ops.pure(3.0), EDataRef("kg"))),
                    Pair("kg", UnitFixture.kg)
                )
            ),
            ops,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProductSpec(
            "carrot",
            QuantityFixture.oneKilogram,
            FromProcess(
                "p",
                MatchLabels(emptyMap()),
                mapOf(
                    Pair("x", EQuantityScale(ops.pure(3.0), UnitFixture.kg))
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
                EDataRef("q_propanol"),
                SubstanceFixture.propanol
            ),
            impacts = listOf(
                EImpact(
                    EDataRef("q_cc"),
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
            ),
            ops,
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
