package ch.kleis.lcaplugin.compute.model

import tech.units.indriya.AbstractUnit.ONE
import javax.measure.Unit
import javax.measure.quantity.Dimensionless

typealias Indicator<D> = Flow<D>

data class ImpactCategory(val name: String) : Indicator<Dimensionless> {
    override fun getUniqueId(): String {
        return name
    }

    override fun getUnit(): Unit<Dimensionless> {
        return ONE
    }
}
