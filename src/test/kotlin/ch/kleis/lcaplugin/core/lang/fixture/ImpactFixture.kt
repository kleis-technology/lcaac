package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.EImpact
import ch.kleis.lcaplugin.core.math.basic.BasicNumber

object ImpactFixture {
    val oneClimateChange: EImpact<BasicNumber> = EImpact(
        quantity = QuantityFixture.oneKilogram,
        indicator = IndicatorFixture.climateChange
    )
}
