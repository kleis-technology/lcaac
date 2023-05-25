package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.EQuantityScale

class QuantityFixture {
    companion object {
        val oneKilogram = EQuantityScale(1.0, UnitFixture.kg)
        val twoKilograms = EQuantityScale(2.0, UnitFixture.kg)
        val oneLitre = EQuantityScale(1.0, UnitFixture.l)
        val twoLitres = EQuantityScale(2.0, UnitFixture.l)
        val hundredPercent = EQuantityScale(100.0, UnitFixture.percent)
    }
}
