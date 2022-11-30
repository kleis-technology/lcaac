package ch.kleis.lcaplugin.compute.traits

import javax.measure.Quantity
import javax.measure.Unit

interface HasUnit<D : Quantity<D>> {
    fun getUnit(): Unit<D>
}
