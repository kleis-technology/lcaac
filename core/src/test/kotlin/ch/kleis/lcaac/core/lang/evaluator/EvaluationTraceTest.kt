package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.fixture.ProcessValueFixture
import ch.kleis.lcaac.core.lang.fixture.ProductValueFixture
import ch.kleis.lcaac.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaac.core.lang.fixture.SubstanceCharacterizationValueFixture
import ch.kleis.lcaac.core.lang.value.ProcessValue
import ch.kleis.lcaac.core.lang.value.TechnoExchangeValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class EvaluationTraceTest {
    @Test
    fun trace_productOrder() {
        // given
        val oneKg = QuantityValueFixture.oneKilogram
        val product1 = ProductValueFixture.product("product1")
        val product2 = ProductValueFixture.product("product2")
        val product3 = ProductValueFixture.product("product3")

        val p1 = ProcessValue(
            name = "p1",
            products = listOf(TechnoExchangeValue(oneKg, product1)),
            inputs = listOf(
                TechnoExchangeValue(oneKg, product2),
                TechnoExchangeValue(oneKg, product3),
            ),
        )
        val p2 = ProcessValue(
            name = "p2",
            products = listOf(TechnoExchangeValue(oneKg, product2)),
            inputs = listOf(
                TechnoExchangeValue(oneKg, product3),
            ),
        )
        val trace = EvaluationTrace<BasicNumber>()
        trace.add(p1)
        trace.commit()
        trace.add(p2)
        trace.commit()

        // when
        val actual = trace.getComparator()

        // then
        assert(actual.compare(product1, product2) < 0)
        assert(actual.compare(product1, product3) < 0)
        assert(actual.compare(product2, product3) < 0)
    }

    @Test
    fun trace_productOrder_withLongCycle() {
        // given
        val oneKg = QuantityValueFixture.oneKilogram
        val product1 = ProductValueFixture.product("product1")
        val product2 = ProductValueFixture.product("product2")
        val product3 = ProductValueFixture.product("product3")

        val p1 = ProcessValue(
            name = "p1",
            products = listOf(TechnoExchangeValue(oneKg, product1)),
            inputs = listOf(
                TechnoExchangeValue(oneKg, product2),
                TechnoExchangeValue(oneKg, product3),
            ),
        )
        val p2 = ProcessValue(
            name = "p2",
            products = listOf(TechnoExchangeValue(oneKg, product2)),
            inputs = listOf(
                TechnoExchangeValue(oneKg, product3),
            ),
        )
        val p3 = ProcessValue(
            name = "p2",
            products = listOf(TechnoExchangeValue(oneKg, product3)),
            inputs = listOf(
                TechnoExchangeValue(oneKg, product1),
            ),
        )
        val trace = EvaluationTrace<BasicNumber>()
        trace.add(p1)
        trace.commit()
        trace.add(p2)
        trace.add(p3)
        trace.commit()

        // when
        val actual = trace.getComparator()

        // then
        assert(actual.compare(product1, product2) < 0)
        assert(actual.compare(product1, product3) < 0)
        assert(actual.compare(product2, product3) < 0)
    }

    @Test
    fun trace_getEntryPoint() {
        // given
        val product1 = ProductValueFixture.product("product1")
        val p1 = ProcessValueFixture.carrotProcessValue
            .copy(products = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, product1)))
        val product2 = ProductValueFixture.product("product2")
        val p2 = ProcessValueFixture.carrotProcessValue
            .copy(products = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, product2)))
        val product3 = ProductValueFixture.product("product3")
        val p3 = ProcessValueFixture.carrotProcessValue
            .copy(products = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, product3)))

        val trace = EvaluationTrace<BasicNumber>()
        trace.add(p1)
        trace.commit()
        trace.add(p2)
        trace.add(p3)
        trace.commit()

        // when
        val actual = trace.getEntryPoint()

        // then
        assertEquals(p1, actual)
    }

    @Test
    fun trace_getEntryPoint_whenMultipleProcessesInFirstStage_shouldThrow() {
        // given
        val product1 = ProductValueFixture.product("product1")
        val p1 = ProcessValueFixture.carrotProcessValue
            .copy(products = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, product1)))
        val product2 = ProductValueFixture.product("product2")
        val p2 = ProcessValueFixture.carrotProcessValue
            .copy(products = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, product2)))
        val product3 = ProductValueFixture.product("product3")
        val p3 = ProcessValueFixture.carrotProcessValue
            .copy(products = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, product3)))

        val trace = EvaluationTrace.empty<BasicNumber>()
        trace.add(p1)
        trace.add(p2)
        trace.commit()
        trace.add(p3)
        trace.commit()

        // when/then
        val e = assertFailsWith(EvaluatorException::class) { trace.getEntryPoint() }
        assertEquals("execution trace contains multiple entrypoint", e.message)
    }

    @Test
    fun trace_getEntryPoint_whenEmptyTrace_shouldThrow() {
        // given
        val trace = EvaluationTrace.empty<BasicNumber>()

        // when/then
        val e = assertFailsWith(EvaluatorException::class) { trace.getEntryPoint() }
        assertEquals("execution trace is empty", e.message)
    }

    @Test
    fun trace_productOrder_isBFS() {
        // given
        val product1 = ProductValueFixture.product("product1")
        val p1 = ProcessValueFixture.carrotProcessValue
            .copy(products = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, product1)))
        val product2 = ProductValueFixture.product("product2")
        val p2 = ProcessValueFixture.carrotProcessValue
            .copy(products = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, product2)))
        val product3 = ProductValueFixture.product("product3")
        val p3 = ProcessValueFixture.carrotProcessValue
            .copy(products = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, product3)))
        val product4 = ProductValueFixture.product("product4")
        val p4 = ProcessValueFixture.carrotProcessValue
            .copy(products = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, product4)))

        val sc = SubstanceCharacterizationValueFixture.propanolCharacterization
        val substance = sc.referenceExchange.substance

        val trace = EvaluationTrace<BasicNumber>()
        trace.add(p1)
        trace.commit()
        trace.add(p2)
        trace.add(p3)
        trace.add(sc)
        trace.commit()
        trace.add(p4)
        trace.commit()

        // when
        val actual = trace.getComparator()

        // then
        assert(actual.compare(product1, product2) < 0)
        assert(actual.compare(product1, product3) < 0)
        assert(actual.compare(product1, substance) < 0)
        assert(actual.compare(product1, product4) < 0)
        assert(actual.compare(product2, product4) < 0)
        assert(actual.compare(product3, product4) < 0)
        assert(actual.compare(substance, product4) < 0)
    }

    @Test
    fun trace_productOrder_isAntiSymmetric() {
        // given
        val product1 = ProductValueFixture.product("product1")
        val p1 = ProcessValueFixture.carrotProcessValue
            .copy(products = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, product1)))
        val product2 = ProductValueFixture.product("product2")
        val p2 = ProcessValueFixture.carrotProcessValue
            .copy(products = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, product2)))
        val product3 = ProductValueFixture.product("product3")
        val p3 = ProcessValueFixture.carrotProcessValue
            .copy(products = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, product3)))
        val product4 = ProductValueFixture.product("product4")
        val p4 = ProcessValueFixture.carrotProcessValue
            .copy(products = listOf(TechnoExchangeValue(QuantityValueFixture.oneKilogram, product4)))

        val sc = SubstanceCharacterizationValueFixture.propanolCharacterization
        val substance = sc.referenceExchange.substance

        val trace = EvaluationTrace<BasicNumber>()
        trace.add(p1)
        trace.commit()
        trace.add(p2)
        trace.add(p3)
        trace.add(sc)
        trace.commit()
        trace.add(p4)
        trace.commit()

        // when
        val actual = trace.getComparator()

        // then
        assert(actual.compare(product1, product2) == -actual.compare(product2, product1))
        assert(actual.compare(product1, product3) == -actual.compare(product3, product1))
        assert(actual.compare(product1, substance) == -actual.compare(substance, product1))
        assert(actual.compare(product1, product4) == -actual.compare(product4, product1))
        assert(actual.compare(product2, product4) == -actual.compare(product4, product2))
        assert(actual.compare(product3, product4) == -actual.compare(product4, product3))
        assert(actual.compare(substance, product4) == -actual.compare(product4, substance))
    }

    @Test
    fun trace_getNumberOfStages() {
        // given
        val p1 = ProcessValueFixture.carrotProcessValue
        val p2 = p1.copy(name = "another_carrot_production")
        val sc = SubstanceCharacterizationValueFixture.propanolCharacterization
        val trace = EvaluationTrace<BasicNumber>()

        // when
        trace.add(p1)
        trace.commit()
        trace.add(p2)
        trace.add(sc)
        trace.commit()

        // then
        assertEquals(2, trace.getNumberOfStages())
    }

    @Test
    fun trace_getStages() {
        // given
        val p1 = ProcessValueFixture.carrotProcessValue
        val p2 = p1.copy(name = "another_carrot_production")
        val sc = SubstanceCharacterizationValueFixture.propanolCharacterization
        val trace = EvaluationTrace<BasicNumber>()
        trace.add(p1)
        trace.commit()
        trace.add(p2)
        trace.add(sc)
        trace.commit()

        // when
        val actual = trace.getStages()

        // then
        assertEquals(setOf(p1), actual[0])
        assertEquals(setOf(p2, sc), actual[1])
    }

    @Test
    fun trace_emptyCommit_shouldNotAddStages() {
        // given
        val p = ProcessValueFixture.carrotProcessValue
        val trace = EvaluationTrace<BasicNumber>()

        // when
        trace.commit()
        trace.commit()
        trace.add(p)
        trace.commit()
        trace.commit()

        // then
        assertEquals(listOf(setOf(p)), trace.getStages())
    }
}
