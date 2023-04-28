package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.EProductSpec

class ProductFixture {
    companion object {
        val carrot = EProductSpec(
            "carrot",
            UnitFixture.kg,
        )
        val salad = EProductSpec(
            "salad",
            UnitFixture.kg,
        )
        val water = EProductSpec(
            "water",
            UnitFixture.l,
        )
    }
}
