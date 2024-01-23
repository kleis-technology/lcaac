package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.expression.EImpact
import ch.kleis.lcaac.core.lang.expression.EImpactBlockEntry
import ch.kleis.lcaac.core.math.basic.BasicNumber

object ImpactFixture {
    val oneClimateChange: EImpact<BasicNumber> = EImpact(
        quantity = QuantityFixture.oneKilogram,
        indicator = IndicatorFixture.climateChange
    )
}

object ImpactBlockFixture {
    val oneClimateChange : EImpactBlockEntry<BasicNumber> = EImpactBlockEntry(
        entry = ImpactFixture.oneClimateChange
    )
}
