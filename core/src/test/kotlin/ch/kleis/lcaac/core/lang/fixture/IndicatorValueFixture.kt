package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.value.IndicatorValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

class IndicatorValueFixture {
    companion object {
        val climateChange = IndicatorValue("Climate Change", UnitValueFixture.kg<BasicNumber>())
    }
}
