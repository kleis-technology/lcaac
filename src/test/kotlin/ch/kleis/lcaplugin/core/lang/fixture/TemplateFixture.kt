package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.EProcess
import ch.kleis.lcaplugin.core.lang.EProcessTemplate
import ch.kleis.lcaplugin.core.lang.EQuantityRef
import ch.kleis.lcaplugin.core.lang.ETechnoExchange

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
                products = listOf(
                    ETechnoExchange(EQuantityRef("q_carrot"), ProductFixture.carrot),
                ),
                inputs = listOf(
                    ETechnoExchange(EQuantityRef("q_water"), ProductFixture.water),
                ),
                biosphere = emptyList(),
            )
        )
    }
}
