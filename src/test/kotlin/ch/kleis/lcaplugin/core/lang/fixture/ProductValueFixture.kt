package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.value.ProductValue
import ch.kleis.lcaplugin.core.lang.value.UnitValue
import ch.kleis.lcaplugin.core.math.basic.BasicNumber

class ProductValueFixture {
    companion object {
        fun product(s: String, unit: UnitValue<BasicNumber> = UnitValueFixture.kg()): ProductValue<BasicNumber> {
            return ProductValue(s, unit)
        }

        val salad = ProductValue("salad", UnitValueFixture.kg<BasicNumber>())
        val carrot = ProductValue("carrot", UnitValueFixture.kg<BasicNumber>())
        val water = ProductValue("water", UnitValueFixture.l<BasicNumber>())
    }
}
