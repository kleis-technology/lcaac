package ch.kleis.lcaplugin.compute.model

import ch.kleis.lcaplugin.compute.traits.HasUnit
import ch.kleis.lcaplugin.compute.traits.HasUrn
import ch.kleis.lcaplugin.compute.urn.URN
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
