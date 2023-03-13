package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.ProductValue

class ProductValueFixture {
    companion object {
        val salad = ProductValue("salad", UnitValueFixture.kg)
        val carrot = ProductValue("carrot", UnitValueFixture.kg)
        val water = ProductValue("water", UnitValueFixture.l)
    }
}
