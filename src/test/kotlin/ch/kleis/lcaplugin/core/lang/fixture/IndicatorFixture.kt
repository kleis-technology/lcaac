package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.EIndicator

class IndicatorFixture {
    companion object {
        val climateChange = EIndicator(
            "climate change",
            UnitFixture.kg,
        )
    }
}
