package ch.kleis.lcaac.core.assessment

import ch.kleis.lcaac.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaac.core.lang.fixture.UnitValueFixture
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ContributionAnalysisTest {
    @Test
    fun allocatedSupply() {
        // given
        val a1 = ProductValue("a1", UnitValueFixture.kg<BasicNumber>())
        val a2 = ProductValue("a2", UnitValueFixture.kg<BasicNumber>())
        val b = ProductValue("b", UnitValueFixture.kg<BasicNumber>())
        val c = ProductValue("c", UnitValueFixture.kg<BasicNumber>())
        val pa = ProcessValue(
            name = "pa",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.twoKilograms, a1,
                    QuantityValueFixture.eightyPercent,
                ),
                TechnoExchangeValue(
                    QuantityValueFixture.twoKilograms, a2,
                    QuantityValueFixture.twentyPercent,
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
        val actual1 = analysis.allocatedSupplyOf(b, a1)
        val actual2 = analysis.allocatedSupplyOf(b, a2)

        // then
        with(QuantityValueOperations(BasicOperations)) {
            val total = QuantityValueFixture.twoKilograms
            val expected1 = pure(0.8) * total
            val expected2 = pure(0.2) * total
            assertEquals(expected1, actual1)
            assertEquals(expected2, actual2)
        }
    }

    @Test
    fun supplyOf_whenControllable() {
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
        val actual = analysis.supplyOf(c)

        // then
        assertEquals(
            QuantityValueFixture.threeKilograms,
            actual,
        )
    }

    @Test
    fun supplyOf_whenObservable() {
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
        val actual = analysis.supplyOf(b)

        // then
        assertEquals(
            QuantityValueFixture.twoKilograms,
            actual,
        )
    }

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
