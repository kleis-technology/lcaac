package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.prelude.Prelude

class QuantityValueFixture {
    companion object {
        private val ops = BasicOperations
        val oneUnit = QuantityValue(ops.pure(1.0), UnitValueFixture.unit())
        val oneKilogram = QuantityValue(ops.pure(1.0), UnitValueFixture.kg())
        val oneGram = QuantityValue(ops.pure(1.0), UnitValueFixture.g())
        val twoKilograms = QuantityValue(ops.pure(2.0), UnitValueFixture.kg())
        val threeKilograms = QuantityValue(ops.pure(3.0), UnitValueFixture.kg())
        val oneLitre = QuantityValue(ops.pure(1.0), UnitValueFixture.l())
        val twoLitres = QuantityValue(ops.pure(2.0), UnitValueFixture.l())
        val twentyPercent = QuantityValue(ops.pure(20.0), UnitValueFixture.percent())
        val eightyPercent = QuantityValue(ops.pure(80.0), UnitValueFixture.percent())
        val fiftyPercent = QuantityValue(ops.pure(50.0), UnitValueFixture.percent())
        val hundredPercent = QuantityValue(ops.pure(100.0), UnitValueFixture.percent())
        val twentyPiece = QuantityValue(ops.pure(20.0), UnitValueFixture.piece())
        val hundredPiece = QuantityValue(ops.pure(100.0), UnitValueFixture.piece())
        val oneGb = QuantityValue(ops.pure(1.0), UnitValueFixture.gb())
        val oneTb = QuantityValue(ops.pure(1.0), UnitValueFixture.tb())
        val oneHour = QuantityValue(ops.pure(1.0), UnitValueFixture.hour())
        private val watt = with(ToValue(ops)) { Prelude.unitMap<BasicNumber>()["W"]!!.toUnitValue() }
        val oneWatt = QuantityValue(ops.pure(1.0), watt)
        val hundredWatt = QuantityValue(ops.pure(100.0), watt)
    }
}
