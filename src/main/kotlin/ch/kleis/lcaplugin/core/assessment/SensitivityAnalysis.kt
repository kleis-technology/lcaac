package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.ParameterName
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.math.dual.DualMatrix
import ch.kleis.lcaplugin.core.math.dual.DualNumber
import ch.kleis.lcaplugin.core.math.dual.DualOperations
import ch.kleis.lcaplugin.core.matrix.ImpactFactorMatrix
import ch.kleis.lcaplugin.core.matrix.IndexedCollection
import ch.kleis.lcaplugin.core.matrix.ParameterVector
import ch.kleis.lcaplugin.core.matrix.SupplyMatrix
import org.jetbrains.kotlinx.multik.ndarray.data.get

class SensitivityAnalysis(
    private val impactFactors: ImpactFactorMatrix<DualNumber, DualMatrix>,
    private val supply: SupplyMatrix<DualNumber, DualMatrix>,
    private val parameters: ParameterVector<DualNumber>,
    private val ops: DualOperations
) {
    fun getControllablePorts(): IndexedCollection<MatrixColumnIndex<DualNumber>> {
        return impactFactors.controllablePorts
    }

    fun getObservablePorts(): IndexedCollection<MatrixColumnIndex<DualNumber>> {
        return impactFactors.observablePorts
    }

    fun getParameters(): ParameterVector<DualNumber> {
        return parameters
    }

    fun getRelativeSensibility(
        target: MatrixColumnIndex<DualNumber>,
        indicator: MatrixColumnIndex<DualNumber>,
        parameter: ParameterName,
    ): Double {
        val parameterIndex = parameters.indexOf(parameter)
        val impactFactor = impactFactors.valueRatio(target, indicator).amount
        val base = impactFactor.zeroth
        val absoluteSensibility = impactFactor.first[parameterIndex]
        return absoluteSensibility / base
    }

    fun getContribution(
        target: MatrixColumnIndex<DualNumber>,
        indicator: MatrixColumnIndex<DualNumber>,
    ): DualNumber {
        val quantity = supply.quantityOf(target).amount
        val ratio = impactFactors.valueRatio(target, indicator).amount
        return with(ops) {
            quantity * ratio
        }
    }
}
