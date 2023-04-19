package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.core.lang.value.SubstanceValue

class SubstanceValueFixture {
    companion object {
        val propanol = SubstanceValue("propanol", SubstanceType.RESOURCE, "air", null, UnitValueFixture.kg)
    }
}
