package ch.kleis.lcaac.core.datasource.cache

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class CachedConnectorTest {
    @Test
    fun getName() {
        // given
        val inner = mockk<DataSourceConnector<BasicNumber>>()
        val connector = CachedConnector(inner)

        every { inner.getName() } returns "source"

        // when
        val actual = connector.getName()

        // then
        assertEquals("source", actual)
    }

    @Test
    fun sumProduct() {
        // given
        val inner = mockk<DataSourceConnector<BasicNumber>>()
        val connector = CachedConnector(inner)
        val config = mockk<DataSourceConfig>()
        val source = mockk<DataSourceValue<BasicNumber>>()
        val columns = listOf("foo")

        val expected = QuantityFixture.oneKilogram
        every { inner.sumProduct(config, source, columns) } returns expected

        // when
        connector.sumProduct(config, source, columns)
        val actual = connector.sumProduct(config, source, columns)

        // then
        assertEquals(expected, actual)
        verify(exactly = 1) { inner.sumProduct(config, source, columns) }
    }

    @Test
    fun getAll() {
        // given
        val inner = mockk<DataSourceConnector<BasicNumber>>()
        val connector = CachedConnector(inner)
        val config = mockk<DataSourceConfig>()
        val source = mockk<DataSourceValue<BasicNumber>>()

        val expected = sequenceOf<ERecord<BasicNumber>>(
            ERecord(mapOf(
                "id" to EStringLiteral("foo"),
            ))
        )
        every { inner.getAll(config, source) } returns expected

        // when
        connector.getAll(config, source)
        val actual = connector.getAll(config, source)

        // then
        assertEquals(expected.toList(), actual.toList())
        verify(exactly = 1) { inner.getAll(config, source) }
    }

    @Test
    fun getFirst() {
        // given
        val inner = mockk<DataSourceConnector<BasicNumber>>()
        val connector = CachedConnector(inner)
        val config = mockk<DataSourceConfig>()
        val source = mockk<DataSourceValue<BasicNumber>>()

        val expected = ERecord<BasicNumber>(mapOf(
                "id" to EStringLiteral("foo"),
            ))
        every { inner.getFirst(config, source) } returns expected

        // when
        connector.getFirst(config, source)
        val actual = connector.getFirst(config, source)

        // then
        assertEquals(expected, actual)
        verify(exactly = 1) { inner.getFirst(config, source) }
    }
}
