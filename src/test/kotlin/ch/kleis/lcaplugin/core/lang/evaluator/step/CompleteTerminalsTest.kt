package ch.kleis.lcaplugin.core.lang.evaluator.step

import ch.kleis.lcaplugin.core.lang.evaluator.toValue
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.*
import ch.kleis.lcaplugin.core.lang.value.BioExchangeValue
import ch.kleis.lcaplugin.core.lang.value.ImpactValue
import ch.kleis.lcaplugin.core.lang.value.IndicatorValue
import ch.kleis.lcaplugin.core.lang.value.SubstanceCharacterizationValue
import org.junit.Assert
import org.junit.Test


class CompleteTerminalsTest {
    @Test
    fun eval_withUnknownSubstances_shouldCompleteSubstances() {
        // given
        val process = EProcessFinal(
            EProcess(
                name = "process",
                biosphere = listOf(
                    EBioExchange(QuantityFixture.oneKilogram, ESubstanceSpec("co2"))
                ),
            )
        )

        // when
        val actual = CompleteTerminals.apply(process)

        // then
        val expected = EProcessFinal(
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
        )
        Assert.assertEquals(expected, actual)
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
        val actual = CompleteTerminals.apply(expression).toValue()

        // then
        val expected = SubstanceCharacterizationValue(
            BioExchangeValue(QuantityValueFixture.oneKilogram, FullyQualifiedSubstanceValueFixture.propanol),
            listOf(
                ImpactValue(
                    QuantityValueFixture.oneKilogram,
                    IndicatorValue("cc", UnitValueFixture.kg)
                )
            )
        )
        Assert.assertEquals(expected, actual)
    }
}