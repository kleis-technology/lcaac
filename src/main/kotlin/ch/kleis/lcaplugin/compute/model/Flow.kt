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
    val substance: String,
    val compartment: String?,
    val subcompartment: String?,
    private val unit: Unit<D>
) : Flow<D> {
    override fun getUniqueId(): String {
        return listOf(substance, compartment, subcompartment)
            .joinToString(":")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ElementaryFlow<*>

        if (substance != other.substance) return false
        if (compartment != other.compartment) return false
        if (subcompartment != other.subcompartment) return false

        return true
    }

    override fun hashCode(): Int {
        var result = substance.hashCode()
        result = 31 * result + (compartment?.hashCode() ?: 0)
        result = 31 * result + (subcompartment?.hashCode() ?: 0)
        return result
    }

    override fun getUnit(): Unit<D> {
        return unit
    }
}
