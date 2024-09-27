package ch.kleis.lcaac.core.datasource.cache

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class SourceOpsCacheTest {
    @Test
    fun recordGetFirst_safeGetOrPut() {
        // given
        val cache = SourceOpsCache<BasicNumber>(8, 8)
        val config = mockk<DataSourceConfig>()
        val record = mockk<ERecord<BasicNumber>>()
        val source = mockk<DataSourceValue<BasicNumber>>()

        // when
        try {
            cache.recordGetFirst(config, source) {
                throw IllegalArgumentException()
            }
        } catch (_: IllegalArgumentException) {
            val actual = cache.recordGetFirst(config, source) {
                record
            }

            // then
            assertEquals(actual, record)
        }
    }

    @Test
    fun recordSumProduct_safeGetOrPut() {
        // given
        val cache = SourceOpsCache<BasicNumber>(8, 8)
        val config = mockk<DataSourceConfig>()
        val column = listOf("power")
        val dataExpression = mockk<DataExpression<BasicNumber>>()
        val source = mockk<DataSourceValue<BasicNumber>>()

        // when
        try {
            cache.recordGetFirst(config, source) {
                throw IllegalArgumentException()
            }
        } catch (_: IllegalArgumentException) {
            val actual = cache.recordSumProduct(config, source, column) {
                dataExpression
            }

            // then
            assertEquals(actual, dataExpression)
        }
    }
}
