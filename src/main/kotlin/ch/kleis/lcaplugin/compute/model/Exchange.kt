package ch.kleis.lcaplugin.compute.model

import javax.measure.Quantity

data class IntermediaryExchange<T : Quantity<T>?>(val flow: IntermediaryFlow, val quantity: Quantity<T>)

data class ElementaryExchange<T : Quantity<T>?>(val flow: ElementaryFlow, val quantity: Quantity<T>) {
    fun negate(): ElementaryExchange<T> {
        return ElementaryExchange(flow, quantity.negate())
    }
}
