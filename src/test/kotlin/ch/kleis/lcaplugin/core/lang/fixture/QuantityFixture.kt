package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.EQuantityLiteral

class QuantityFixture {
    companion object {
        val oneKilogram = EQuantityLiteral(1.0, UnitFixture.kg)
        val twoKilograms = EQuantityLiteral(2.0, UnitFixture.kg)
        val oneLitre = EQuantityLiteral(1.0, UnitFixture.l)
    }
}
