package ch.kleis.lcaplugin.lib.traits

import javax.measure.Quantity
import javax.measure.Unit

interface HasUnit<D : Quantity<D>> {
    fun getUnit(): Unit<D>
}
