package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.ESubstanceSpec
import ch.kleis.lcaplugin.core.lang.expression.SubstanceType

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
