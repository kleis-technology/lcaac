package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.EConstrainedProduct
import ch.kleis.lcaplugin.core.lang.expression.EProduct
import ch.kleis.lcaplugin.core.lang.expression.None

class ProductFixture {
    companion object {
        val carrot = EConstrainedProduct(
            EProduct(
                "carrot",
                UnitFixture.kg,
            ),
            None,
        )
        val salad = EConstrainedProduct(
            EProduct(
                "salad",
                UnitFixture.kg,
            ),
            None,
        )
        val water = EConstrainedProduct(
            EProduct(
                "water",
                UnitFixture.l,
            ),
            None,
        )
    }
}
