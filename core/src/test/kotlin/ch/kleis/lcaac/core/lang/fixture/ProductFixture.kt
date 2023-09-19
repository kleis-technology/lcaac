package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.expression.EProductSpec

class ProductFixture {
    companion object {
        val carrot = EProductSpec(
            "carrot",
            QuantityFixture.oneKilogram,
        )
        val salad = EProductSpec(
            "salad",
            QuantityFixture.oneKilogram,
        )
        val water = EProductSpec(
            "water",
            QuantityFixture.oneLitre,
        )
    }
}
