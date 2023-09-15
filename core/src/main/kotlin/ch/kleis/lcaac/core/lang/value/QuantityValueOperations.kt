package ch.kleis.lcaac.core.lang.value

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.math.QuantityOperations

class QuantityValueOperations<Q>(
    private val ops: QuantityOperations<Q>
) : QuantityOperations<QuantityValue<Q>> {
    override fun QuantityValue<Q>.plus(other: QuantityValue<Q>): QuantityValue<Q> {
        val left = this
        with(ops) {
            val resultUnit = checkAndReturnUnitForAddition(left.unit, other.unit)
            val resultAmount =
                (left.amount * pure(left.unit.scale) + other.amount * pure(other.unit.scale)) / pure(resultUnit.scale)
            return QuantityValue(resultAmount, resultUnit)
        }
    }

    override fun QuantityValue<Q>.minus(other: QuantityValue<Q>): QuantityValue<Q> {
        val left = this
        with(ops) {
            val resultUnit = checkAndReturnUnitForAddition(left.unit, other.unit)
            val resultAmount =
                (left.amount * pure(left.unit.scale) - other.amount * pure(other.unit.scale)) / pure(resultUnit.scale)
            return QuantityValue(resultAmount, resultUnit)
        }
    }


    private fun checkAndReturnUnitForAddition(left: UnitValue<Q>, right: UnitValue<Q>): UnitValue<Q> {
        if (left.dimension != right.dimension) {
            throw EvaluatorException("incompatible dimensions: ${left.dimension} vs ${right.dimension} in left=$left and right=$right")
        }

        return if (left.scale > right.scale) left else right
    }

    override fun QuantityValue<Q>.times(other: QuantityValue<Q>): QuantityValue<Q> {
        val left = this
        with(ops) {
            return QuantityValue(
                left.amount * other.amount,
                left.unit * other.unit,
            )
        }
    }

    override fun QuantityValue<Q>.div(other: QuantityValue<Q>): QuantityValue<Q> {
        val left = this
        with(ops) {
            return QuantityValue(
                left.amount / other.amount,
                left.unit / other.unit,
            )
        }
    }

    override fun QuantityValue<Q>.unaryMinus(): QuantityValue<Q> {
        val self = this
        with(ops) {
            return QuantityValue(
                -self.amount,
                self.unit,
            )
        }
    }

    override fun QuantityValue<Q>.pow(other: Double): QuantityValue<Q> {
        val self = this
        with(ops) {
            return QuantityValue(
                self.amount.pow(other),
                self.unit.pow(other),
            )
        }
    }

    fun QuantityValue<Q>.absoluteScaleValue(): Q {
        val amount = this.amount
        val scale = this.unit.scale
        with(ops) {
            return amount * pure(scale)
        }
    }

    override fun QuantityValue<Q>.toDouble(): Double {
        val quantity = this
        with(ops) {
            return quantity.absoluteScaleValue().toDouble()
        }
    }

    override fun pure(value: Double): QuantityValue<Q> {
        with(ops) {
            return QuantityValue(
                pure(value),
                UnitValue.none(),
            )
        }
    }
}
