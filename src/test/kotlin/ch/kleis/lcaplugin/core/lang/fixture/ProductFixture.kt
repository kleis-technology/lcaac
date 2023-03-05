package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.EProduct

class ProductFixture {
    companion object {
        val carrot = EProduct(
            "carrot",
            UnitFixture.kg,
        )
        val water = EProduct(
            "water",
            UnitFixture.l,
        )
    }
}
