package ch.kleis.lcaac.core.lang.expression

import ch.kleis.lcaac.core.lang.fixture.UnitValueFixture
import ch.kleis.lcaac.core.lang.value.FromProcessRefValue
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.RecordValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import io.mockk.InternalPlatformDsl.toStr
import org.junit.jupiter.api.Nested
import kotlin.collections.mapOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.to

class EMapperTest {
    private val sut = EMapper()

    @Nested
    inner class ToDataExpression {
        @Test
        fun `when QuantityValue should map to EQuantityScale`()  {
            // Given
            val value = QuantityValue(BasicNumber(1.0), UnitValueFixture.kg())

            // When
            val actual = sut.toDataExpression(value)

            // Then
            assertEquals(value.toEQuantityScale(), actual)
        }

        @Test
        fun `when RecordValue should map to ERecord`()  {
            // Given
            val value = RecordValue(mapOf(
                "quantity" to QuantityValue(BasicNumber(100.0), UnitValueFixture.kg())
            ))

            // When
            val actual = sut.toDataExpression(value)

            // Then
            assertEquals(value.toERecord(), actual)
        }

        @Test
        fun `when StringValue should map to EStringLiteral`()  {
            // Given
            val value = StringValue<BasicNumber>("10")

            // When
            val actual = sut.toDataExpression(value)

            // Then
            assertEquals(value.toEStringLiteral(), actual)
        }
    }

    @Nested
    inner class ToFromProcess {
        @Test
        fun `should map`() {
            // given
            val value = FromProcessRefValue(
                "aaa",
                mapOf("bbb" to StringValue<BasicNumber>("100")),
                mapOf("ccc" to StringValue<BasicNumber>("200"))
            )

            // when
            val actual = sut.toFromProcess(value)

            // then
            assertEquals(value.name, actual.name)

            assertEquals(value.matchLabels.size, actual.matchLabels.elements.size)
            assertEquals("100", actual.matchLabels.elements["bbb"].toStr())

            assertEquals(value.arguments.size, actual.arguments.size)
            assertEquals("200", actual.arguments["ccc"].toStr())
        }
    }

    @Nested
    inner class ToETechnoExchange() {
        @Test
        fun `should map`()  {
            // given
            val qty = QuantityValue(BasicNumber(1.0), UnitValueFixture.kg())
            val processRef = FromProcessRefValue<BasicNumber>("process name")
            val product = ProductValue("name", UnitValueFixture.kg(), processRef)
            // when
            val actual = sut.toETechnoExchange(qty, product)

            // then
            assertEquals(qty.toEQuantityScale(), actual.quantity)
            assertEquals(product.name, actual.product.name)
            assertEquals("kg", actual.product.referenceUnit.toStr())
            assertEquals("process name", actual.product.fromProcess?.name)
        }
    }
}