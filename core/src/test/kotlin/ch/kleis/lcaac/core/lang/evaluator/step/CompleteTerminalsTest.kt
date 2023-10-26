package ch.kleis.lcaac.core.lang.evaluator.step

import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.fixture.*
import ch.kleis.lcaac.core.lang.value.BioExchangeValue
import ch.kleis.lcaac.core.lang.value.ImpactValue
import ch.kleis.lcaac.core.lang.value.IndicatorValue
import ch.kleis.lcaac.core.lang.value.SubstanceCharacterizationValue
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class CompleteTerminalsTest {
    private val ops = BasicOperations

    @Test
    fun eval_withUnknownSubstances_shouldCompleteSubstances() {
        // given
        val process =
            EProcess(
                name = "process",
                biosphere = listOf(
                    EBioExchange(QuantityFixture.oneKilogram, ESubstanceSpec("co2"))
                ),
            )

        // when
        val actual = CompleteTerminals(ops).apply(process)

        // then
        val expected =
            EProcess(
                name = "process",
                biosphere = listOf(
                    EBioExchange(
                        QuantityFixture.oneKilogram,
                        ESubstanceSpec(
                            "co2",
                            referenceUnit = QuantityFixture.oneKilogram
                        )
                    )
                ),
            )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenSubstanceCharacterization_shouldCompleteIndicator() {
        // given
        val expression = ESubstanceCharacterization(
            EBioExchange(QuantityFixture.oneKilogram, SubstanceFixture.propanol),
            listOf(
                EImpact(QuantityFixture.oneKilogram, EIndicatorSpec("cc"))
            )
        )

        // when
        val actual = with(ToValue(BasicOperations)) { CompleteTerminals(ops).apply(expression).toValue() }

        // then
        val expected = SubstanceCharacterizationValue(
            BioExchangeValue(QuantityValueFixture.oneKilogram, FullyQualifiedSubstanceValueFixture.propanol),
            listOf(
                ImpactValue(
                    QuantityValueFixture.oneKilogram,
                    IndicatorValue("cc", UnitValueFixture.kg())
                )
            )
        )
        assertEquals(expected, actual)
    }
}
