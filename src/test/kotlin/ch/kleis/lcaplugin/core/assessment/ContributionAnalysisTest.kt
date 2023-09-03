package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnitValueFixture
import ch.kleis.lcaplugin.core.lang.value.*
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import org.junit.Test
import kotlin.test.assertEquals

class ContributionAnalysisTest {
    @Test
    fun getExchangeContribution1() {
        // given
        val a = ProductValue("a", UnitValueFixture.kg<BasicNumber>())
        val b = ProductValue("b", UnitValueFixture.kg<BasicNumber>())
        val c = ProductValue("c", UnitValueFixture.kg<BasicNumber>())
        val pa = ProcessValue(
            name = "pa",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.twoKilograms, a,
                )
            ),
            inputs = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.twoKilograms, b,
                ),
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram, c,
                )
            ),
        )
        val pb = ProcessValue(
            name = "pb",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram, b,
                )
            ),
            inputs = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram, c,
                )
            ),
        )
        val system = SystemValue(
            setOf(pa, pb)
        )
        val program = ContributionAnalysisProgram(
            system,
            pa,
        )

        // when
        val analysis = program.run()
        val actual = analysis.getExchangeContribution(
            b,
            pb.inputs[0],
            c,
        )

        // then
        assertEquals(
            QuantityValueFixture.twoKilograms,
            actual,
        )
    }

    @Test
    fun getExchangeContribution2() {
        // given
        val a = ProductValue("a", UnitValueFixture.kg<BasicNumber>())
        val b = ProductValue("b", UnitValueFixture.kg<BasicNumber>())
        val c = ProductValue("c", UnitValueFixture.kg<BasicNumber>())
        val pa = ProcessValue(
            name = "pa",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.twoKilograms, a,
                )
            ),
            inputs = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.twoKilograms, b,
                ),
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram, c,
                )
            ),
        )
        val pb = ProcessValue(
            name = "pb",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram, b,
                )
            ),
            inputs = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram, c,
                )
            ),
        )
        val system = SystemValue(
            setOf(pa, pb)
        )
        val program = ContributionAnalysisProgram(
            system,
            pa,
        )

        // when
        val analysis = program.run()
        val actual = analysis.getExchangeContribution(
            a,
            pa.inputs[1],
            c,
        )

        // then
        assertEquals(
            QuantityValueFixture.oneKilogram,
            actual,
        )
    }

    @Test
    fun getPortContribution() {
        // given
        val a = ProductValue("a", UnitValueFixture.kg<BasicNumber>())
        val b = ProductValue("b", UnitValueFixture.kg<BasicNumber>())
        val c = ProductValue("c", UnitValueFixture.kg<BasicNumber>())
        val pa = ProcessValue(
            name = "pa",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.twoKilograms, a,
                )
            ),
            inputs = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.twoKilograms, b,
                )
            ),
        )
        val pb = ProcessValue(
            name = "pb",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram, b,
                )
            ),
            inputs = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram, c,
                )
            ),
        )
        val system = SystemValue(
            setOf(pa, pb)
        )
        val program = ContributionAnalysisProgram(
            system,
            pa,
        )

        // when
        val analysis = program.run()
        val actual = analysis.getPortContribution(a, c)

        // then
        assertEquals(
            QuantityValueFixture.twoKilograms,
            actual,
        )
    }

    @Test
    fun supplyOf_whenProduct() {
        // given
        val a = ProductValue("a", UnitValueFixture.kg<BasicNumber>())
        val b = ProductValue("b", UnitValueFixture.kg<BasicNumber>())
        val c = ProductValue("c", UnitValueFixture.kg<BasicNumber>())
        val pa = ProcessValue(
            name = "pa",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.twoKilograms, a,
                )
            ),
            inputs = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.twoKilograms, b,
                )
            ),
        )
        val pb = ProcessValue(
            name = "pb",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram, b,
                )
            ),
            inputs = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram, c,
                )
            ),
        )
        val system = SystemValue(
            setOf(pa, pb)
        )
        val program = ContributionAnalysisProgram(
            system,
            pa,
        )

        // when
        val analysis = program.run()
        val actual = analysis.supplyOf(a)

        // then
        assertEquals(
            QuantityValueFixture.twoKilograms,
            actual,
        )
    }

    @Test
    fun supplyOf_whenSubstance() {
        // given
        val a = ProductValue("a", UnitValueFixture.kg<BasicNumber>())
        val b = PartiallyQualifiedSubstanceValue("b", UnitValueFixture.kg<BasicNumber>())
        val c = IndicatorValue("c", UnitValueFixture.kg<BasicNumber>())
        val pa = ProcessValue(
            name = "pa",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.twoKilograms, a,
                )
            ),
            biosphere = listOf(
                BioExchangeValue(
                    QuantityValueFixture.twoKilograms, b,
                )
            ),
        )
        val sb = SubstanceCharacterizationValue(
            referenceExchange = BioExchangeValue(QuantityValueFixture.oneKilogram, b),
            impacts = listOf(
                ImpactValue(QuantityValueFixture.oneKilogram, c)
            )
        )
        val system = SystemValue(
            setOf(pa), setOf(sb),
        )
        val program = ContributionAnalysisProgram(
            system,
            pa,
        )

        // when
        val analysis = program.run()
        val actual = analysis.supplyOf(b)

        // then
        assertEquals(
            QuantityValueFixture.twoKilograms,
            actual,
        )
    }
}
