package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.value.QuantityValue

class QuantityValueFixture {
    companion object {
        val oneKilogram = QuantityValue(1.0, UnitValueFixture.kg)
        val twoKilograms = QuantityValue(2.0, UnitValueFixture.kg)
        val oneLitre = QuantityValue(1.0, UnitValueFixture.l)
        val twoLitres = QuantityValue(2.0, UnitValueFixture.l)
        val hundredPercent = QuantityValue(100.0, UnitValueFixture.percent)
        val fiftyPercent = QuantityValue(50.0, UnitValueFixture.percent)
        val twenyPiece = QuantityValue(20.0, UnitValueFixture.piece)
        val thirtyPiece = QuantityValue(30.0, UnitValueFixture.piece)
    }
}
