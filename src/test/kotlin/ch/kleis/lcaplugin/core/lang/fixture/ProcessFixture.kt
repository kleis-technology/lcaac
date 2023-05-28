package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.EBioExchange
import ch.kleis.lcaplugin.core.lang.expression.EProcess
import ch.kleis.lcaplugin.core.lang.expression.ETechnoExchange

class ProcessFixture {
    companion object {
        val carrotProduction = EProcess(
            name = "carrot_production",
            labels = emptyMap(),
            products = listOf(
                ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot),
            ),
            inputs = listOf(
                ETechnoExchange(QuantityFixture.oneLitre, ProductFixture.water),
            ),
            biosphere = listOf(
                EBioExchange(QuantityFixture.oneKilogram, SubstanceFixture.propanol),
            ),
        )
    }
}
