package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.EIndicatorSpec

class IndicatorFixture {
    companion object {
        val climateChange = EIndicatorSpec(
            "climate change",
            QuantityFixture.oneKilogram,
        )
    }
}
