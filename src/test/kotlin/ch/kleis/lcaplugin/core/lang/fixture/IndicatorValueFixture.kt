package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.value.IndicatorValue
import ch.kleis.lcaplugin.core.math.basic.BasicNumber

class IndicatorValueFixture {
    companion object {
        val climateChange = IndicatorValue("Climate Change", UnitValueFixture.kg<BasicNumber>())
    }
}
