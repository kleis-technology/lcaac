package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.fixture.ProcessValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.ProductValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceCharacterizationValueFixture
import ch.kleis.lcaplugin.core.lang.value.TechnoExchangeValue
import org.junit.Test
import kotlin.test.assertEquals


class TraceTest {
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

        val trace = Trace()
        trace.add(p1)
        trace.commit()
        trace.add(p2)
        trace.add(p3)
        trace.add(sc)
        trace.commit()
        trace.add(p4)
        trace.commit()

        // when
        val actual = trace.getProductOrder()

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

        val trace = Trace()
        trace.add(p1)
        trace.commit()
        trace.add(p2)
        trace.add(p3)
        trace.add(sc)
        trace.commit()
        trace.add(p4)
        trace.commit()

        // when
        val actual = trace.getProductOrder()

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
        val trace = Trace()

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
        val trace = Trace()
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
        val trace = Trace()

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
