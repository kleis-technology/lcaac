package ch.kleis.lcaplugin.compute.matrix

import ch.kleis.lcaplugin.compute.matrix.impl.Matrix
import ch.kleis.lcaplugin.compute.matrix.impl.MatrixFactory
import ch.kleis.lcaplugin.compute.model.CharacterizationFactor
import ch.kleis.lcaplugin.compute.model.Indicator
import ch.kleis.lcaplugin.compute.model.IntermediaryFlow
import tech.units.indriya.quantity.Quantities.getQuantity
import java.util.stream.Collectors.toMap

class ControllableFactorMatrix(
    private val controllableFlows: IndexedCollection<IntermediaryFlow<*>>,
    private val indicators: IndexedCollection<Indicator<*>>,
    private val characterizationFactors: List<CharacterizationFactor>,
) {
    val matrix: Matrix = MatrixFactory.INSTANCE.zero(controllableFlows.size(), indicators.size())

    init {
        val cfs = characterizationFactors.stream()
            .collect(toMap(
                { cf -> Pair(cf.input.flow, cf.output.flow) },
                { cf -> cf.input.quantity.divide(cf.output.quantity) },
                { existing, _ -> existing } // TODO: Should warn user
            ))
        controllableFlows.getElements()
            .forEach { flow ->
                val row = controllableFlows.indexOf(flow)
                indicators.getElements()
                    .forEach { indicator ->
                        val col = indicators.indexOf(indicator)
                        val factor = cfs[Pair(indicator, flow)] ?: getQuantity(0.0, indicator.getUnit().divide(flow.getUnit()))
                        matrix.add(row, col, factor.referenceValue())
                    }
            }
    }
}
