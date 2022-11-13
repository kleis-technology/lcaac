package ch.kleis.lcaplugin.compute.matrix

import ch.kleis.lcaplugin.compute.matrix.impl.Matrix
import ch.kleis.lcaplugin.compute.matrix.impl.MatrixFactory
import ch.kleis.lcaplugin.compute.model.CharacterizationFactor
import ch.kleis.lcaplugin.compute.model.Indicator
import ch.kleis.lcaplugin.compute.model.IntermediaryFlow
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
                { cf -> cf.output.quantity.divide(cf.input.quantity) },
            ))
        controllableFlows.getElements()
            .forEach { flow ->
                val row = controllableFlows.indexOf(flow)
                indicators.getElements()
                    .forEach { indicator ->
                        val col = indicators.indexOf(indicator)
                        val factor = cfs[Pair(flow, indicator)] ?: throw NoSuchElementException()
                        matrix.add(row, col, factor.referenceValue())
                    }
            }
    }
}
