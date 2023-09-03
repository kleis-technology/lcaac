package ch.kleis.lcaplugin.core.lang.fixture

import ch.kleis.lcaplugin.core.lang.value.QuantityValue
import ch.kleis.lcaplugin.core.math.basic.BasicOperations

class QuantityValueFixture {
    companion object {
        private val ops = BasicOperations
        val oneUnit = QuantityValue(ops.pure(1.0), UnitValueFixture.unit())
        val oneKilogram = QuantityValue(ops.pure(1.0), UnitValueFixture.kg())
        val oneGram = QuantityValue(ops.pure(1.0), UnitValueFixture.g())
        val twoKilograms = QuantityValue(ops.pure(2.0), UnitValueFixture.kg())
        val oneLitre = QuantityValue(ops.pure(1.0), UnitValueFixture.l())
        val twoLitres = QuantityValue(ops.pure(2.0), UnitValueFixture.l())
        val twentyPercent = QuantityValue(ops.pure(20.0), UnitValueFixture.percent())
        val eightyPercent = QuantityValue(ops.pure(80.0), UnitValueFixture.percent())
        val fiftyPercent = QuantityValue(ops.pure(50.0), UnitValueFixture.percent())
        val hundredPercent = QuantityValue(ops.pure(100.0), UnitValueFixture.percent())
        val twentyPiece = QuantityValue(ops.pure(20.0), UnitValueFixture.piece())
        val hundredPiece = QuantityValue(ops.pure(100.0), UnitValueFixture.piece())
    }
}
