package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.ESubstance

class SubstanceFixture {
    companion object {
        val propanol = ESubstance(
            "propanol",
            "air",
            null,
            UnitFixture.kg,
        )
    }
}
