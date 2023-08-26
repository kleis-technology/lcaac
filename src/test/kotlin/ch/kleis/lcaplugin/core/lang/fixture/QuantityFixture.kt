package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.expression.EQuantityScale

object QuantityFixture {
    val oneUnit = EQuantityScale(1.0, UnitFixture.unit)
    val oneKilogram = EQuantityScale(1.0, UnitFixture.kg)
    val oneGram = EQuantityScale(1.0, UnitFixture.g)
    val twoKilograms = EQuantityScale(2.0, UnitFixture.kg)
    val oneLitre = EQuantityScale(1.0, UnitFixture.l)
    val twoLitres = EQuantityScale(2.0, UnitFixture.l)
}