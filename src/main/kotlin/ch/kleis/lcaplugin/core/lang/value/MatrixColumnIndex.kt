package ch.kleis.lcaplugin.core.lang.value

import arrow.optics.optics
import ch.kleis.lcaplugin.core.HasUID
import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.expression.SubstanceType

sealed interface MatrixColumnIndex : Value, HasUID {
    fun getDimension(): Dimension
    fun name(): String
    fun referenceUnit(): UnitValue
    override fun getUID(): String {
        return name()
    }
}

@optics
data class ProductValue(val name: String, val referenceUnit: UnitValue, val constraint: ConstraintValue = NoneValue) :
    Value, MatrixColumnIndex {
    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun name(): String {
        if (constraint is FromProcessRefValue) {
            return "$name from ${constraint.name}${constraint.arguments}"
        }
        return name
    }

    override fun referenceUnit(): UnitValue {
        return referenceUnit
    }

    fun withConstraint(constraint: ConstraintValue): ProductValue {
        return ProductValue(name, referenceUnit, constraint)
    }

    companion object
}

@optics
data class SubstanceValue(
    val name: String,
    val type: SubstanceType,
    val compartment: String,
    val subcompartment: String?,
    val referenceUnit: UnitValue,
) : Value, MatrixColumnIndex {
    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun name(): String {
        return name
    }

    override fun referenceUnit(): UnitValue {
        return referenceUnit
    }

    companion object
}

data class IndicatorValue(val name: String, val referenceUnit: UnitValue) : Value, MatrixColumnIndex {
    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun name(): String {
        return name
    }

    override fun referenceUnit(): UnitValue {
        return referenceUnit
    }

    companion object
}
