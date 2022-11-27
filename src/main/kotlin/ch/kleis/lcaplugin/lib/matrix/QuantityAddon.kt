package ch.kleis.lcaplugin.lib.matrix

import javax.measure.Quantity

fun <V : Quantity<V>> Quantity<V>.referenceValue(): Double {
    return this.toSystemUnit().value.toDouble()
}
