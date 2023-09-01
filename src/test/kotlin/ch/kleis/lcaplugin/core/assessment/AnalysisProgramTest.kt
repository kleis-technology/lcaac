package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnitValueFixture
import ch.kleis.lcaplugin.core.lang.value.*
import ch.kleis.lcaplugin.core.math.basic.BasicOperations
import org.junit.Test
import kotlin.test.assertEquals

class AnalysisProgramTest {

    @Test
    fun run_thenCorrectIntensity() {
        with(BasicOperations) {
            // given
            val a = ProductValue("a", UnitValueFixture.kg)
            val b = ProductValue("b", UnitValueFixture.kg)
            val c = ProductValue("c", UnitValueFixture.kg)
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
            val program = AnalysisProgram(
                system,
                pa,
                BasicOperations,
            )

            // when
            val analysis = program.run()
            val actual = analysis.intensity

            // then
            assertEquals(
                QuantityValue(pure(1.0), UnitValueFixture.unit),
                actual.intensityOf(pa),
            )
            assertEquals(
                QuantityValue(pure(2.0), UnitValueFixture.unit),
                actual.intensityOf(pb),
            )
        }
    }

    @Test
    fun run_thenCorrectImpact() {
        with(BasicOperations) {
            // given
            val a = ProductValue("a", UnitValueFixture.kg)
            val b = ProductValue("b", UnitValueFixture.kg)
            val c = ProductValue("c", UnitValueFixture.kg)
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
            val program = AnalysisProgram(
                system,
                pa,
                BasicOperations,
            )

            // when
            val analysis = program.run()
            val actual = analysis.impactFactors

            // then
            assertEquals(
                QuantityValue(
                    pure(1.0),
                    UnitValueFixture.kg,
                ),
                actual.unitaryImpact(a, c),
            )
        }
    }
}
