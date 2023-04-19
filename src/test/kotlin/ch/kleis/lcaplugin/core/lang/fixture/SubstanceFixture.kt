package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.ESubstance
import ch.kleis.lcaplugin.core.lang.expression.SubstanceType

class SubstanceFixture {
    companion object {
        val propanol = ESubstance(
            "propanol",
            "propanol",
            type = SubstanceType.RESOURCE,
            "air",
            null,
            UnitFixture.kg,
        )
    }
}
