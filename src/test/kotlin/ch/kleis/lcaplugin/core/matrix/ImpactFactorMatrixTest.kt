package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.fixture.UnitFixture
import ch.kleis.lcaplugin.core.lang.value.*
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.core.math.basic.BasicOperations
import ch.kleis.lcaplugin.core.math.basic.MatrixFixture
import org.junit.Test
import kotlin.test.assertEquals

class ImpactFactorMatrixTest {
    private val ops = BasicOperations

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
    private val data = MatrixFixture.basic(3, 2, Array(3 * 2) { 2E-3 })
    val sut = ImpactFactorMatrix(outputs, inputs, data, ops)

    @Test
    fun value_shouldReturnExchange() {
        // When
        val result = sut.characterizationFactor(output1, input1)

        // Then
        assertEquals(
            GenericExchangeValue(QuantityValue(ops.pure(2E-3), literValue.dimension.getDefaultUnitValue()), input1),
            result.input
        )
    }

    @Test
    fun valueRatio_shouldReturnTheRatioWithScale_UnitIsNotTheReferenceForDimension() {
        // When
        val result = sut.unitaryImpact(output1, input1)

        // Then
        assertEquals(QuantityValue(ops.pure(2.0), literValue), result)
    }

    @Test
    fun rowAsMap() {
        // When
        val result = sut.rowAsMap(output1)

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
