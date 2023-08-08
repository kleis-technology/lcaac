package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.EImpact
import ch.kleis.lcaplugin.core.lang.expression.EIndicatorSpec

object ImpactFixture {
    val oneClimateChange: EImpact = EImpact(
        quantity = QuantityFixture.oneUnit,
        indicator = EIndicatorSpec("Climate Change", UnitFixture.unit)
    )
}
