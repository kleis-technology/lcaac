package ch.kleis.lcaac.core.assessment

import ch.kleis.lcaac.core.ParameterName
import ch.kleis.lcaac.core.lang.fixture.UnitValueFixture
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.dual.DualNumber
import ch.kleis.lcaac.core.math.dual.DualOperations
import ch.kleis.lcaac.core.matrix.IndexedCollection
import ch.kleis.lcaac.core.matrix.ParameterVector
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SensitivityAnalysisTest {
    private val ops = DualOperations(2)
    private val quantityOps = QuantityValueOperations(ops)
    private val dx = ops.basis(0)
    private val dy = ops.basis(1)

    @Test
    fun getRelativeSensibility_whenAveraging_shouldAnalyzeTotalImpact() {
        // given
        val a = ProductValue("a", UnitValueFixture.kg<DualNumber>())
        val b = ProductValue("b", UnitValueFixture.kg<DualNumber>())
        val c = ProductValue("c", UnitValueFixture.kg<DualNumber>())
        val oneKilogram = QuantityValue(ops.pure(1.0), UnitValueFixture.kg())
        val twoKilograms = QuantityValue(ops.pure(2.0), UnitValueFixture.kg())
        val x = with(ops) {
            QuantityValue(
                pure(1.0) + dx,
                UnitValueFixture.unit(),
            )
        }
        val y = with(ops) {
            QuantityValue(
                pure(1.0) + dy,
                UnitValueFixture.unit(),
            )
        }
        val qb = with(QuantityValueOperations(ops)) {
            oneKilogram * x
        }
        val qc = with(QuantityValueOperations(ops)) {
            twoKilograms * y
        }
        val qa = with(QuantityValueOperations(ops)) {
            qb + qc
        }
        val pa = ProcessValue(
            name = "pa",
            products = listOf(
                TechnoExchangeValue(
                    qa, a,
                )
            ),
            inputs = listOf(
                TechnoExchangeValue(
                    qb, b,
                ),
                TechnoExchangeValue(
                    qc, c,
                ),
            ),
        )
        val system = SystemValue(
            setOf(pa)
        )
        val xName = ParameterName("x")
        val yName = ParameterName("y")
        val program = SensitivityAnalysisProgram(
            system,
            pa,
            ParameterVector(
                names = IndexedCollection(listOf(xName, yName)),
                data = listOf(x, y),
            )
        )

        // when
        val analysis = program.run()
        val sb = analysis.getRelativeSensibility(
            a, b, xName,
        )
        val sc = analysis.getRelativeSensibility(
            a, c, xName,
        )

        // then
        val tolerance = 1e-3
        assertEquals(1.0, sb, tolerance)
        assertEquals(0.0, sc, tolerance)
    }

    @Test
    fun getRelativeSensibility() {
        // given
        val a = ProductValue("a", UnitValueFixture.kg<DualNumber>())
        val b = ProductValue("b", UnitValueFixture.kg<DualNumber>())
        val c = ProductValue("c", UnitValueFixture.kg<DualNumber>())
        val oneKilogram = QuantityValue(ops.pure(1.0), UnitValueFixture.kg())
        val twoKilograms = QuantityValue(ops.pure(2.0), UnitValueFixture.kg())
        val x = with(ops) {
            QuantityValue(
                pure(1.0) + dx,
                UnitValueFixture.unit(),
            )
        }
        val y = with(ops) {
            QuantityValue(
                pure(1.0) + dy,
                UnitValueFixture.unit(),
            )
        }
        val pa = ProcessValue(
            name = "pa",
            products = listOf(
                TechnoExchangeValue(
                    twoKilograms, a,
                )
            ),
            inputs = listOf(
                TechnoExchangeValue(
                    twoKilograms, b,
                ),
            ),
        )
        val pb = ProcessValue(
            name = "pb",
            products = listOf(
                TechnoExchangeValue(
                    oneKilogram, b,
                )
            ),
            inputs = listOf(
                TechnoExchangeValue(
                    with(quantityOps) {
                        x.pow(2.0) * oneKilogram
                    },
                    c,
                )
            ),
        )
        val system = SystemValue(
            setOf(pa, pb)
        )
        val xName = ParameterName("x")
        val yName = ParameterName("y")
        val program = SensitivityAnalysisProgram(
            system,
            pa,
            ParameterVector(
                names = IndexedCollection(listOf(xName, yName)),
                data = listOf(x, y),
            )
        )

        // when
        val analysis = program.run()
        val actual = analysis.getRelativeSensibility(
            a, c, xName,
        )

        // then
        assertEquals(
            2.0,
            actual,
        )
    }
}
