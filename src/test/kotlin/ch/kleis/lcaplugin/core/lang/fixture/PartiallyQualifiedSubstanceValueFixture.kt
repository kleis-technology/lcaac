package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.value.PartiallyQualifiedSubstanceValue
import ch.kleis.lcaplugin.core.math.basic.BasicNumber

class PartiallyQualifiedSubstanceValueFixture {
    companion object {
        val propanol =
            PartiallyQualifiedSubstanceValue("propanol", UnitValueFixture.kg<BasicNumber>())
    }
}
