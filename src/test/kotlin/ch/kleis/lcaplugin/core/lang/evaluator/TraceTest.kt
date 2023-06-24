package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.fixture.ProcessValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceCharacterizationValueFixture
import org.junit.Test
import kotlin.test.assertEquals


class TraceTest {
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
