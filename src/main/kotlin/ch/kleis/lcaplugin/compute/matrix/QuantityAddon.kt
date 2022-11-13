package ch.kleis.lcaplugin.compute.matrix

import javax.measure.Quantity

fun <V : Quantity<V>> Quantity<V>.referenceValue(): Double {
    return this.toSystemUnit().value.toDouble()
}
