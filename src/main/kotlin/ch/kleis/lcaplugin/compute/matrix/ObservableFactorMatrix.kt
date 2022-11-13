package ch.kleis.lcaplugin.compute.matrix

import ch.kleis.lcaplugin.compute.matrix.impl.Matrix
import ch.kleis.lcaplugin.compute.model.CharacterizationFactor
import ch.kleis.lcaplugin.compute.model.Exchange
import ch.kleis.lcaplugin.compute.model.Indicator
import ch.kleis.lcaplugin.compute.model.IntermediaryFlow
import tech.units.indriya.quantity.Quantities.getQuantity
import javax.measure.Quantity

class ObservableFactorMatrix(
    private val observableFlows: IndexedCollection<IntermediaryFlow<*>>,
    private val indicators: IndexedCollection<Indicator<*>>,
    val matrix: Matrix
) {
    fun <Din : Quantity<Din>, Dout : Quantity<Dout>> value(
        flow: IntermediaryFlow<Din>,
        indicator: Indicator<Dout>,
    ): CharacterizationFactor {
        val input = Exchange(flow, getQuantity(1.0, flow.getUnit().systemUnit))
        val amount = matrix.value(observableFlows.indexOf(flow), indicators.indexOf(indicator))
        val output = Exchange(indicator, getQuantity(amount, indicator.getUnit()))
        return CharacterizationFactor(output, input)
    }
}
