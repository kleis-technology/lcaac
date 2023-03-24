package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.value.ProductValue
import ch.kleis.lcaplugin.core.lang.value.UnitValue

class ProductValueFixture {
    companion object {
        fun product(s: String, unit: UnitValue = UnitValueFixture.kg): ProductValue {
            return ProductValue(s, unit)
        }

        val salad = ProductValue("salad", UnitValueFixture.kg)
        val carrot = ProductValue("carrot", UnitValueFixture.kg)
        val water = ProductValue("water", UnitValueFixture.l)
    }
}
