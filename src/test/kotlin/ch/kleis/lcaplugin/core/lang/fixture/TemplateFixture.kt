package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.EDataRef
import ch.kleis.lcaplugin.core.lang.expression.EProcess
import ch.kleis.lcaplugin.core.lang.expression.EProcessTemplate
import ch.kleis.lcaplugin.core.lang.expression.ETechnoExchange

class TemplateFixture {
    companion object {
        val carrotProduction = EProcessTemplate(
            params = mapOf(
                Pair("q_water", QuantityFixture.oneLitre)
            ),
            locals = mapOf(
                Pair("q_carrot", QuantityFixture.oneKilogram)
            ),
            body = EProcess(
                name = "carrot_production",
                products = listOf(
                    ETechnoExchange(EDataRef("q_carrot"), ProductFixture.carrot),
                ),
                inputs = listOf(
                    ETechnoExchange(EDataRef("q_water"), ProductFixture.water),
                ),
            )
        )
        val withUnboundedRef = EProcessTemplate(
            body = EProcess(
                name = "with_unbounded_ref",
                products = listOf(
                    ETechnoExchange(EDataRef("q_carrot"), ProductFixture.carrot),
                ),
                inputs = listOf(
                    ETechnoExchange(EDataRef("q_water"), ProductFixture.water),
                ),
            )
        )
    }
}
