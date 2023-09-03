package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.ParameterName
import ch.kleis.lcaplugin.core.lang.fixture.UnitValueFixture
import ch.kleis.lcaplugin.core.lang.value.*
import ch.kleis.lcaplugin.core.math.dual.DualNumber
import ch.kleis.lcaplugin.core.math.dual.DualOperations
import ch.kleis.lcaplugin.core.matrix.IndexedCollection
import ch.kleis.lcaplugin.core.matrix.ParameterVector
import org.junit.Test
import kotlin.test.assertEquals

class SensitivityAnalysisTest {
    private val ops = DualOperations(2)
    private val quantityOps = QuantityValueOperations(ops)
    private val dx = ops.basis(0)
    private val dy = ops.basis(1)

    @Test
    fun getRelativeSensibility() {
        // given
        val a = ProductValue("a", UnitValueFixture.kg<DualNumber>())
        val b = ProductValue("b", UnitValueFixture.kg<DualNumber>())
        val c = ProductValue("c", UnitValueFixture.kg<DualNumber>())
        val oneKilogram = QuantityValue(ops.pure(1.0), UnitValueFixture.kg())
        val twoKilograms = QuantityValue(ops.pure(1.0), UnitValueFixture.kg())
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
