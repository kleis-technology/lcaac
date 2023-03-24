package ch.kleis.lcaplugin.core.lang.evaluator.linker

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.evaluator.compiler.UnlinkedSystem
import ch.kleis.lcaplugin.core.lang.expression.ConstraintFlag
import ch.kleis.lcaplugin.core.lang.fixture.ProductValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnitValueFixture
import ch.kleis.lcaplugin.core.lang.value.*
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.*
import org.junit.Test


class ProductMatcherTest {

    @Test
    fun match_whenOneMatches_shouldReturnCandidate() {
        // given
        val expected = ProductValueFixture.product("product").withConstraint(
            FromProcessRefValue("production", emptyMap())
        )
        val systemObject = systemObjectFromProducts(
            listOf(
                expected,
                ProductValueFixture.salad,
            )
        )

        val product = ProductValueFixture.product("product")

        val productPartialOrder = mockk<ProductPartialOrder>()
        every { productPartialOrder.leq(any(), any()) } returns false
        every { productPartialOrder.leq(expected, product) } returns true
        every { productPartialOrder.minimal(any()) } answers { it.invocation.args[0] as List<ProductValue> }

        val matcher = ProductMatcher(systemObject, productPartialOrder)

        // when
        val actual = matcher.match(product)

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun match_whenTwoMatchAndNoDefault_shouldThrow() {
        // given
        val systemObject = systemObjectFromProducts(
            listOf(
                ProductValueFixture.product("product", UnitValueFixture.kg),
                ProductValueFixture.product("product", UnitValueFixture.ton),
            )
        )
        val product = ProductValueFixture.product("product")

        val productPartialOrder = mockk<ProductPartialOrder>()
        every { productPartialOrder.leq(any(), any()) } returns false
        every { productPartialOrder.leq(any(), eq(product)) } returns true
        every { productPartialOrder.minimal(any()) } answers { it.invocation.args[0] as List<ProductValue> }

        val matcher = ProductMatcher(systemObject, productPartialOrder)

        // when/then
        try {
            matcher.match(product)
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            assertEquals("too many matches without default for product", e.message)
        }
    }

    @Test
    fun match_whenOneDefault_shouldReturnDefault() {
        // given
        val expected = ProductValueFixture.carrot.withConstraint(
            FromProcessRefValue("carrot_production", emptyMap(), ConstraintFlag.IS_DEFAULT)
        )
        val systemObject = systemObjectFromProducts(
            listOf(
                expected,
                ProductValueFixture.carrot,
            )
        )
        val product = ProductValueFixture.product("carrot")

        val productPartialOrder = mockk<ProductPartialOrder>()
        every { productPartialOrder.leq(any(), any()) } returns false
        every { productPartialOrder.leq(any(), eq(product)) } returns true
        every { productPartialOrder.minimal(any()) } answers { it.invocation.args[0] as List<ProductValue> }

        val matcher = ProductMatcher(systemObject, productPartialOrder)

        // when
        val actual = matcher.match(product)

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun match_whenMoreThanOneDefault_shouldThrow() {
        // given
        val systemObject = systemObjectFromProducts(
            listOf(
                ProductValueFixture.product("product").withConstraint(
                    FromProcessRefValue("carrot_production", emptyMap(), ConstraintFlag.IS_DEFAULT)
                ),
                ProductValueFixture.product("product").withConstraint(
                    FromProcessRefValue("salad_production", emptyMap(), ConstraintFlag.IS_DEFAULT)
                ),
            )
        )
        val product = ProductValueFixture.product("product")

        val productPartialOrder = mockk<ProductPartialOrder>()
        every { productPartialOrder.leq(any(), any()) } returns false
        every { productPartialOrder.leq(any(), eq(product)) } returns true
        every { productPartialOrder.minimal(any()) } answers { it.invocation.args[0] as List<ProductValue> }

        val matcher = ProductMatcher(systemObject, productPartialOrder)

        // when/then
        try {
            matcher.match(product)
            fail("should have thrown")
        } catch (e: EvaluatorException) {
            assertEquals("more than one default matches for product", e.message)
        }
    }

    @Test
    fun match_whenNoMatch_shouldReturnNull() {
        // given
        val expected = ProductValueFixture.carrot
        val systemObject = systemObjectFromProducts(
            listOf(
                ProductValueFixture.salad,
            )
        )

        val product = ProductValueFixture.product("product")

        val productPartialOrder = mockk<ProductPartialOrder>()
        every { productPartialOrder.leq(any(), any()) } returns false
        every { productPartialOrder.leq(expected, product) } returns true
        every { productPartialOrder.minimal(any()) } answers { it.invocation.args[0] as List<ProductValue> }

        val matcher = ProductMatcher(systemObject, productPartialOrder)

        // when
        val actual = matcher.match(product)

        // then
        assertNull(actual)
    }

    private fun systemObjectFromProducts(
        products: List<ProductValue>
    ): UnlinkedSystem {
        return systemObjectFromProcesses(
            products.map { processWith(it) }
        )
    }

    private fun systemObjectFromProcesses(
        processes: List<ProcessValue>
    ): UnlinkedSystem {
        val result = UnlinkedSystem()
        processes.forEach { result.addProcess(it) }
        return result
    }

    private fun processWith(product: ProductValue): ProcessValue {
        return ProcessValue(
            "process",
            listOf(
                TechnoExchangeValue(QuantityValueFixture.oneKilogram, product)
            ),
            emptyList(),
            emptyList(),
        )
    }
}
