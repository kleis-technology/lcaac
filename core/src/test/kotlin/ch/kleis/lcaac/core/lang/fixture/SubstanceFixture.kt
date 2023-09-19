package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.expression.ESubstanceSpec
import ch.kleis.lcaac.core.lang.expression.SubstanceType

class SubstanceFixture {
    companion object {
        val propanol = ESubstanceSpec(
            "propanol",
            "propanol",
            type = SubstanceType.RESOURCE,
            "air",
            null,
            QuantityFixture.oneKilogram,
        )
    }
}
