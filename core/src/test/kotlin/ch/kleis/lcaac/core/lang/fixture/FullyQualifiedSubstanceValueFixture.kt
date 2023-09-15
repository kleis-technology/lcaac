package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.expression.SubstanceType
import ch.kleis.lcaac.core.lang.value.FullyQualifiedSubstanceValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

class FullyQualifiedSubstanceValueFixture {
    companion object {
        val propanol =
            FullyQualifiedSubstanceValue("propanol", SubstanceType.RESOURCE, "air", null, UnitValueFixture.kg<BasicNumber>())
    }
}
