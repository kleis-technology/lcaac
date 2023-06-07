package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.*

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
                labels = emptyMap(),
                products = listOf(
                    ETechnoExchange(EDataRef("q_carrot"), ProductFixture.carrot),
                ),
                inputs = listOf(
                    ETechnoExchange(EDataRef("q_water"), ProductFixture.water),
                ),
                biosphere = emptyList(),
            )
        )
        val withUnboundedRef = EProcessTemplate(
            params = emptyMap(),
            locals = emptyMap(),
            body = EProcess(
                name = "with_unbounded_ref",
                labels = emptyMap(),
                products = listOf(
                    ETechnoExchange(EDataRef("q_carrot"), ProductFixture.carrot),
                ),
                inputs = listOf(
                    ETechnoExchange(EDataRef("q_water"), ProductFixture.water),
                ),
                biosphere = emptyList(),
            )
        )
    }
}
