package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication

object EProcessTemplateApplicationFixture {
    val carrotTwoLiters =
        EProcessTemplateApplication(TemplateFixture.carrotProduction, mapOf("q_water" to QuantityFixture.twoLitres))
}