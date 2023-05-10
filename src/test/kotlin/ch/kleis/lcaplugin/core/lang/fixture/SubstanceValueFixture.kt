package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.core.lang.value.FullyQualifiedSubstanceValue

class SubstanceValueFixture {
    companion object {
        val propanol =
            FullyQualifiedSubstanceValue("propanol", SubstanceType.RESOURCE, "air", null, UnitValueFixture.kg)
    }
}
