package ch.kleis.lcaplugin.lib.model

import tech.units.indriya.ComparableQuantity
import javax.measure.Quantity

data class Exchange<D : Quantity<D>>(val flow: Flow<D>, val quantity: ComparableQuantity<D>)
