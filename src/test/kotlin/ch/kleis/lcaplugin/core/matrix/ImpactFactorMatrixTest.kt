package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.fixture.UnitFixture
import ch.kleis.lcaplugin.core.lang.value.*
import ch.kleis.lcaplugin.core.matrix.impl.Matrix
import org.junit.Test
import kotlin.test.assertEquals

class ImpactFactorMatrixTest {

    private val kgValue = UnitValue(UnitFixture.kg.symbol, UnitFixture.kg.scale, UnitFixture.kg.dimension)
    private val literValue = UnitValue(UnitFixture.l.symbol, UnitFixture.l.scale, UnitFixture.l.dimension)
    private val output1 = ProductValue("co2 in kg", kgValue, null)
    private val output2 = ProductValue("meth in kg", kgValue, null)
    private val output3 = ProductValue("nitro in kg", kgValue, null)
    private val outputs: IndexedCollection<MatrixColumnIndex> =
        IndexedCollection(listOf(output1, output2, output3))
    private val input1 = ProductValue("oil", literValue, null)
    private val input2 = ProductValue("water", literValue, null)
    private val inputs: IndexedCollection<MatrixColumnIndex> =
        IndexedCollection(listOf(input1, input2))
    private val matrix = object : Matrix {
        override fun value(row: Int, col: Int): Double {
            return 2E-3 // 2 liter in m3
        }

        override fun set(row: Int, col: Int, value: Double) { // Ignore
        }

        override fun negate(): Matrix {// Ignore
            return this
        }

        override fun transpose(): Matrix { // Ignore
            return this
        }

        override fun rowDim(): Int {
            return 1
        }

        override fun colDim(): Int {
            return 1
        }

    }
    val sut = ImpactFactorMatrix(outputs, inputs, matrix)

    @Test
    fun value_shouldReturnExchange() {
        // When
        val result = sut.value(output1, input1)

        // Then
        assertEquals(
            GenericExchangeValue(QuantityValue(2E-3, literValue.dimension.getDefaultUnitValue()), input1),
            result.input
        )
    }

    @Test
    fun valueRatio_shouldReturnTheRatioWithScale_UnitIsNotTheReferenceForDimension() {
        // When
        val result = sut.valueRatio(output1, input1)

        // Then
        assertEquals(QuantityValue(2.0, literValue), result)
    }

    @Test
    fun rowAsMap() {
        // When
        val result = sut.rowAsMap(output1)

        // Then
        val expected: Map<MatrixColumnIndex, QuantityValue> = mapOf(
            input1 to QuantityValue(2.0, literValue),
            input2 to QuantityValue(2.0, literValue),
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
