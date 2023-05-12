package ch.kleis.lcaplugin.core.lang.value

import arrow.optics.optics
import ch.kleis.lcaplugin.core.HasUID
import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.expression.SubstanceType

sealed interface MatrixColumnIndex : Value, HasUID {
    fun getDimension(): Dimension
    fun getDisplayName(): String
    fun referenceUnit(): UnitValue
    override fun getUID(): String {
        return getDisplayName()
    }
}

@optics
data class ProductValue(
    val name: String,
    val referenceUnit: UnitValue,
    val fromProcessRef: FromProcessRefValue? = null
) :
    Value, MatrixColumnIndex {
    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun getDisplayName(): String {
        if (fromProcessRef is FromProcessRefValue) {
            return "$name from ${fromProcessRef.name}${fromProcessRef.arguments}"
        }
        return name
    }

    override fun referenceUnit(): UnitValue {
        return referenceUnit
    }

    fun withFromProcessRef(fromProcessRef: FromProcessRefValue): ProductValue {
        return ProductValue(name, referenceUnit, fromProcessRef)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductValue

        if (name != other.name) return false
        if (referenceUnit.dimension != other.referenceUnit.dimension) return false
        return fromProcessRef == other.fromProcessRef
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + referenceUnit.dimension.hashCode()
        result = 31 * result + (fromProcessRef?.hashCode() ?: 0)
        return result
    }


    companion object

}

@optics
sealed interface SubstanceValue : Value, MatrixColumnIndex {
    companion object
}

@optics
data class PartiallyQualifiedSubstanceValue(
    val name: String,
    val referenceUnit: UnitValue,
) : SubstanceValue {
    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun getDisplayName(): String {
        return name
    }

    override fun referenceUnit(): UnitValue {
        return referenceUnit
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PartiallyQualifiedSubstanceValue

        if (name != other.name) return false
        return referenceUnit.dimension == other.referenceUnit.dimension
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + referenceUnit.dimension.hashCode()
        return result
    }


    companion object
}

@optics
data class FullyQualifiedSubstanceValue(
    val name: String,
    val type: SubstanceType,
    val compartment: String,
    val subcompartment: String?,
    val referenceUnit: UnitValue,
) : SubstanceValue {
    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun getDisplayName(): String {
        if (compartment.isBlank()) {
            return """[${type.value}] $name"""
        }

        val args = listOfNotNull(
            compartment,
            subcompartment
        ).joinToString()
        return """[${type.value}] $name($args)"""
    }

    override fun referenceUnit(): UnitValue {
        return referenceUnit
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FullyQualifiedSubstanceValue

        if (name != other.name) return false
        if (type != other.type) return false
        if (compartment != other.compartment) return false
        if (subcompartment != other.subcompartment) return false
        return referenceUnit.dimension == other.referenceUnit.dimension
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + compartment.hashCode()
        result = 31 * result + (subcompartment?.hashCode() ?: 0)
        result = 31 * result + referenceUnit.dimension.hashCode()
        return result
    }

    companion object
}

data class IndicatorValue(val name: String, val referenceUnit: UnitValue) : Value, MatrixColumnIndex {
    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun getDisplayName(): String {
        return name
    }

    override fun referenceUnit(): UnitValue {
        return referenceUnit
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IndicatorValue

        if (name != other.name) return false
        return referenceUnit.dimension == other.referenceUnit.dimension
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + referenceUnit.dimension.hashCode()
        return result
    }


}
