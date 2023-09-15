package ch.kleis.lcaac.core.matrix

import ch.kleis.lcaac.core.lang.fixture.UnitFixture
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.math.basic.MatrixFixture
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ImpactFactorMatrixTest {
    private val ops = BasicOperations
    private val quantityOps = QuantityValueOperations(ops)

    private val kgValue = UnitValue<BasicNumber>(UnitFixture.kg.symbol, UnitFixture.kg.scale, UnitFixture.kg.dimension)
    private val literValue = UnitValue<BasicNumber>(UnitFixture.l.symbol, UnitFixture.l.scale, UnitFixture.l.dimension)
    private val output1 = ProductValue("co2 in kg", kgValue, null)
    private val output2 = ProductValue("meth in kg", kgValue, null)
    private val output3 = ProductValue("nitro in kg", kgValue, null)
    private val outputs: IndexedCollection<MatrixColumnIndex<BasicNumber>> =
        IndexedCollection(listOf(output1, output2, output3))
    private val input1 = ProductValue("oil", literValue, null)
    private val input2 = ProductValue("water", literValue, null)
    private val inputs: IndexedCollection<MatrixColumnIndex<BasicNumber>> =
        IndexedCollection(listOf(input1, input2))
    private val data = MatrixFixture.basic(
        3, 2,
        arrayOf(
            2E-3, 2E-3,
            2E-3, 2E-3,
            2E-3, 2E-3,
        )
    )
    val sut = ImpactFactorMatrix(outputs, inputs, data, ops)

    @Test
    fun characterizationFactor() {
        // When
        val actual = sut.characterizationFactor(output1, input1)

        // Then
        val expected = with(BasicOperations) {
            val numerator = QuantityValue(pure(2.0), literValue)
            val denominator = QuantityValue(pure(1.0), kgValue)
            with(QuantityValueOperations(BasicOperations)) {
                numerator / denominator
            }
        }
        with(quantityOps) {
            assertEquals(
                expected.absoluteScaleValue().value,
                actual.absoluteScaleValue().value,
            )

        }
    }

    @Test
    fun rowAsMap() {
        // When
        val result = sut.unitaryImpacts(output1)

        // Then
        val expected: Map<MatrixColumnIndex<BasicNumber>, QuantityValue<BasicNumber>> = mapOf(
            input1 to QuantityValue(ops.pure(2.0), literValue),
            input2 to QuantityValue(ops.pure(2.0), literValue),
        )
        assertEquals(expected, result)
    }

    @Test
    fun nbCells() {
        // When
        val result = sut.nbCells()

        // Then
        assertEquals(6, result)
    }
}
