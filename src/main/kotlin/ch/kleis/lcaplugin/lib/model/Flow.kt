package ch.kleis.lcaplugin.lib.model

import ch.kleis.lcaplugin.lib.registry.URN
import ch.kleis.lcaplugin.lib.traits.HasUnit
import ch.kleis.lcaplugin.lib.traits.HasUrn
import javax.measure.Quantity
import javax.measure.Unit

data class Flow<D : Quantity<D>>(
    private val urn: URN,
    private val unit: Unit<D>
) : HasUrn, HasUnit<D> {

    override fun getUnit(): Unit<D> {
        return unit
    }

    override fun getUrn(): URN {
        return urn
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Flow<*>

        if (urn != other.urn) return false
        if (unit.dimension != other.unit.dimension) return false

        return true
    }

    override fun hashCode(): Int {
        var result = urn.hashCode()
        result = 31 * result + unit.dimension.hashCode()
        return result
    }
}
