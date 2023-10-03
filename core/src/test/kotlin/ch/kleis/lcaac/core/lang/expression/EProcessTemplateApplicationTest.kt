package ch.kleis.lcaac.core.lang.expression

import ch.kleis.lcaac.core.lang.fixture.EProcessTemplateApplicationFixture
import ch.kleis.lcaac.core.lang.fixture.TemplateFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EProcessTemplateApplicationTest {
    // Should we do this with property-based testing ?

    @Test
    fun testHashCode() {
        // given
        val fixture = EProcessTemplateApplicationFixture.carrotTwoLiters
        val different = EProcessTemplateApplication(TemplateFixture.carrotProduction, emptyMap())

        // then
        assertEquals(fixture.hashCode(), fixture.hashCode())
        assertNotEquals(fixture.hashCode(), different.hashCode())
    }

    @Test
    fun testEquals() {
        // given
        val fixture = EProcessTemplateApplicationFixture.carrotTwoLiters
        val different = EProcessTemplateApplication(TemplateFixture.carrotProduction, emptyMap())

        // then
        assert(fixture == fixture)
        assert(fixture != different)
    }
}