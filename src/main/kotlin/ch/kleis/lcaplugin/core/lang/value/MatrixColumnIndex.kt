package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.HasUID
import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.expression.SubstanceType

sealed interface MatrixColumnIndex<Q> : Value<Q>, HasUID {
    fun getDimension(): Dimension
    fun getDisplayName(): String
    fun getShortName(): String
    fun referenceUnit(): UnitValue<Q>
    override fun getUID(): String {
        return getDisplayName()
    }
}

data class ProductValue<Q>(
    val name: String,
    val referenceUnit: UnitValue<Q>,
    val fromProcessRef: FromProcessRefValue<Q>? = null
) : Value<Q>, MatrixColumnIndex<Q> {

    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun getDisplayName(): String {
        if (fromProcessRef is FromProcessRefValue) {
            return "$name from ${fromProcessRef.name}${fromProcessRef.matchLabels}${fromProcessRef.arguments}"
        }
        return name
    }

    override fun getShortName(): String {
        return name
    }

    override fun referenceUnit(): UnitValue<Q> {
        return referenceUnit
    }

    fun withFromProcessRef(fromProcessRef: FromProcessRefValue<Q>): ProductValue<Q> {
        return ProductValue(name, referenceUnit, fromProcessRef)
    }

    override fun toString(): String {
        return getDisplayName()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductValue<Q>

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
}

sealed interface SubstanceValue<Q> : Value<Q>, MatrixColumnIndex<Q>

data class PartiallyQualifiedSubstanceValue<Q>(
    val name: String,
    val referenceUnit: UnitValue<Q>,
) : SubstanceValue<Q> {
    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun getDisplayName(): String {
        return name
    }

    override fun getShortName(): String {
        return name
    }

    override fun referenceUnit(): UnitValue<Q> {
        return referenceUnit
    }

    override fun toString(): String {
        return getDisplayName()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PartiallyQualifiedSubstanceValue<Q>

        if (name != other.name) return false
        return referenceUnit.dimension == other.referenceUnit.dimension
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + referenceUnit.dimension.hashCode()
        return result
    }
}

data class FullyQualifiedSubstanceValue<Q>(
    val name: String,
    val type: SubstanceType,
    val compartment: String,
    val subcompartment: String?,
    val referenceUnit: UnitValue<Q>,
) : SubstanceValue<Q> {
    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun getShortName(): String {
        return name
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

    override fun referenceUnit(): UnitValue<Q> {
        return referenceUnit
    }

    override fun toString(): String {
        return getDisplayName()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FullyQualifiedSubstanceValue<Q>

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
}

data class IndicatorValue<Q>(val name: String, val referenceUnit: UnitValue<Q>) : Value<Q>, MatrixColumnIndex<Q> {
    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun getShortName(): String {
        return name
    }

    override fun getDisplayName(): String {
        return name
    }

    override fun referenceUnit(): UnitValue<Q> {
        return referenceUnit
    }

    override fun toString(): String {
        return getDisplayName()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IndicatorValue<Q>

        if (name != other.name) return false
        return referenceUnit.dimension == other.referenceUnit.dimension
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + referenceUnit.dimension.hashCode()
        return result
    }
}
