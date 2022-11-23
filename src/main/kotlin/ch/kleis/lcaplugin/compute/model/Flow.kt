package ch.kleis.lcaplugin.compute.model

import ch.kleis.lcaplugin.compute.traits.HasUniqueId
import ch.kleis.lcaplugin.compute.traits.HasUnit
import javax.measure.Quantity
import javax.measure.Unit

interface Flow<D : Quantity<D>> : HasUniqueId, HasUnit<D>

data class IntermediaryFlow<D : Quantity<D>>(val name: String, private val unit: Unit<D>) : Flow<D> {

    override fun getUniqueId(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntermediaryFlow<*>

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun getUnit(): Unit<D> {
        return unit
    }

}

data class ElementaryFlow<D : Quantity<D>>(
    private val name: String,
    private val unit: Unit<D>
) : Flow<D> {

    override fun getUniqueId(): String {
        return name;
    }

    override fun getUnit(): Unit<D> {
        return unit
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ElementaryFlow<*>

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
