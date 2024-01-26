package ch.kleis.lcaac.core.lang.expression.optics

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import kotlin.test.Test
import kotlin.test.assertEquals

class DataExpressionTest {
    @Test
    fun defaultRecordOf_filteredDatasource() {
        // given
        val expression = EDefaultRecordOf<BasicNumber>(
            EFilter(EDataSourceRef("source"), mapOf("x" to EDataRef("y")))
        )

        // when
        val actual = everyDataRefInDataExpression<BasicNumber>().getAll(expression)

        // then
        val expected=  listOf(
            EDataRef<BasicNumber>("y")
        )
        assertEquals(expected, actual)
    }

    @Test
    fun defaultRecordOf_filteredDatasource_modify() {
        // given
        val expression = EDefaultRecordOf<BasicNumber>(
            EFilter(EDataSourceRef("source"), mapOf("x" to EDataRef("y")))
        )
        val value = EDataRef<BasicNumber>("z")

        // when
        val actual = everyDataRefInDataExpression<BasicNumber>().modify(expression) {
            value
        }

        // then
        val expected=  EDefaultRecordOf(
            EFilter(EDataSourceRef("source"), mapOf("x" to value)),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun firstRecordOf_filteredDatasource() {
        // given
        val expression = EFirstRecordOf<BasicNumber>(
            EFilter(EDataSourceRef("source"), mapOf("x" to EDataRef("y")))
        )

        // when
        val actual = everyDataRefInDataExpression<BasicNumber>().getAll(expression)

        // then
        val expected=  listOf(
            EDataRef<BasicNumber>("y")
        )
        assertEquals(expected, actual)
    }

    @Test
    fun firstRecordOf_filteredDatasource_modify() {
        // given
        val expression = EFirstRecordOf<BasicNumber>(
            EFilter(EDataSourceRef("source"), mapOf("x" to EDataRef("y")))
        )
        val value = EDataRef<BasicNumber>("z")

        // when
        val actual = everyDataRefInDataExpression<BasicNumber>().modify(expression) {
            value
        }

        // then
        val expected=  EFirstRecordOf(
            EFilter(EDataSourceRef("source"), mapOf("x" to value)),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun sumProduct_filteredDatasource() {
        // given
        val expression = ESumProduct<BasicNumber>(
            EFilter(EDataSourceRef("source"), mapOf("x" to EDataRef("y"))),
            listOf("abc", "xyz")
        )

        // when
        val actual = everyDataRefInDataExpression<BasicNumber>().getAll(expression)

        // then
        val expected=  listOf(
            EDataRef<BasicNumber>("y")
        )
        assertEquals(expected, actual)
    }

    @Test
    fun sumProduct_filteredDatasource_modify() {
        // given
        val expression = ESumProduct<BasicNumber>(
            EFilter(EDataSourceRef("source"), mapOf("x" to EDataRef("y"))),
            listOf("abc", "xyz")
        )
        val value = EDataRef<BasicNumber>("z")

        // when
        val actual = everyDataRefInDataExpression<BasicNumber>().modify(expression) {
            value
        }

        // then
        val expected=  ESumProduct(
            EFilter(EDataSourceRef("source"), mapOf("x" to value)),
            listOf("abc", "xyz")
        )
        assertEquals(expected, actual)
    }
}
