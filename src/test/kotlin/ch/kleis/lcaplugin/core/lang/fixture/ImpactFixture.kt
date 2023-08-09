package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.EImpact

object ImpactFixture {
    val oneClimateChange: EImpact = EImpact(
        quantity = QuantityFixture.oneUnit,
        indicator = IndicatorFixture.climateChange
    )
}
