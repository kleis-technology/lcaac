package ch.kleis.lcaplugin.compute.model

import tech.units.indriya.ComparableQuantity
import javax.measure.Quantity
import javax.measure.quantity.Dimensionless

data class Exchange<D : Quantity<D>, F : Flow<D>>(val flow: F, val quantity: ComparableQuantity<D>)

typealias IntermediaryExchange<D> = Exchange<D, IntermediaryFlow<D>>
typealias ElementaryExchange<D> = Exchange<D, ElementaryFlow<D>>
typealias ImpactCategoryExchange = Exchange<Dimensionless, ImpactCategory>
