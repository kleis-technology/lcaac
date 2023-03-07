package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.HasUID


sealed interface Value

class VProduct(val name: String, val referenceUnit: VUnit) : Value, HasUID {
    fun dimensionDefaultUnit(): VUnit {
        return VUnit(
            "default(${referenceUnit.dimension})",
            1.0,
            referenceUnit.dimension
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VProduct) return false

        if (name != other.name) return false

        return referenceUnit.dimension == other.referenceUnit.dimension
    }

    override fun hashCode(): Int {
        return 31 * name.hashCode() + referenceUnit.dimension.hashCode()
    }
}

data class VUnit(val symbol: String, val scale: Double, val dimension: Dimension) : Value {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VUnit

        if (scale != other.scale) return false
        if (dimension != other.dimension) return false

        return true
    }

    override fun hashCode(): Int {
        var result = scale.hashCode()
        result = 31 * result + dimension.hashCode()
        return result
    }
}

data class VQuantity(val amount: Double, val unit: VUnit) : Value {
    fun referenceValue(): Double {
        return amount * unit.scale
    }
}

data class VExchange(val quantity: VQuantity, val product: VProduct) : Value

data class VProcess(val exchanges: List<VExchange>) : Value, HasUID

data class VSystem(val processes: List<VProcess>) : Value

data class VCharacterizationFactor(val output: VExchange, val input: VExchange) : Value
