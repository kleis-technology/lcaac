package ch.kleis.lcaplugin.compute.model

import ch.kleis.lcaplugin.compute.traits.HasUniqueId
import ch.kleis.lcaplugin.compute.traits.HasUnit
import javax.measure.Quantity
import javax.measure.Unit

data class Flow<D : Quantity<D>>(val name: String, private val unit: Unit<D>) : HasUniqueId, HasUnit<D> {
    override fun getUniqueId(): String {
        return name
    }

    override fun getUnit(): Unit<D> {
        return unit
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Flow<*>

        if (name != other.name) return false
        if (unit.dimension != other.unit.dimension) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + unit.dimension.hashCode()
        return result
    }
}
