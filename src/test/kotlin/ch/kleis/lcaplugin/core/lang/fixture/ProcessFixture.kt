package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.EBioExchange
import ch.kleis.lcaplugin.core.lang.EProcess
import ch.kleis.lcaplugin.core.lang.ETechnoExchange

class ProcessFixture {
    companion object {
        val carrotProduction = EProcess(
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
