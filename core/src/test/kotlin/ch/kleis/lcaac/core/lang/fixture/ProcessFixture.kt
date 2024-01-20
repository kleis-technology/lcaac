package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.expression.*

class ProcessFixture {
    companion object {
        val carrotProduction = EProcess(
            name = "carrot_production",
            labels = emptyMap(),
            products = listOf(
                ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot),
            ),
            inputs = listOf(
                ETechnoBlockEntry(ETechnoExchange(QuantityFixture.oneLitre, ProductFixture.water)),
            ),
            biosphere = listOf(
                EBioBlockEntry(EBioExchange(QuantityFixture.oneKilogram, SubstanceFixture.propanol)),
            ),
            impacts = listOf(
                EImpactBlockEntry(ImpactFixture.oneClimateChange),
            )
        )
    }
}
