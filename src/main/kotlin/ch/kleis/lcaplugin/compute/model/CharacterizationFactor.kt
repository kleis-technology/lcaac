package ch.kleis.lcaplugin.compute.model

import javax.measure.Quantity

data class CharacterizationFactor(val flow: Flow, val indicator: Indicator, val numerator: Quantity<*>, val denominator: Quantity<*>)
