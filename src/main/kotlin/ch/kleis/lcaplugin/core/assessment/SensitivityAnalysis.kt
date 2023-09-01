package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.ParameterName
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.ProcessValue
import ch.kleis.lcaplugin.core.lang.value.ProductValue
import ch.kleis.lcaplugin.core.math.dual.DualMatrix
import ch.kleis.lcaplugin.core.math.dual.DualNumber
import ch.kleis.lcaplugin.core.matrix.ImpactFactorMatrix
import ch.kleis.lcaplugin.core.matrix.IndexedCollection
import ch.kleis.lcaplugin.core.matrix.IntensityMatrix
import ch.kleis.lcaplugin.core.matrix.ParameterVector
import org.jetbrains.kotlinx.multik.ndarray.data.get

class SensitivityAnalysis(
    private val entryPoint: ProcessValue<DualNumber>,
    private val impactFactors: ImpactFactorMatrix<DualNumber, DualMatrix>,
    private val supply: IntensityMatrix<DualNumber, DualMatrix>,
    private val parameters: ParameterVector<DualNumber>,
) {
    fun getEntryPointProducts(): List<ProductValue<DualNumber>> {
        return entryPoint.products.map { it.product }
    }

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
        val impactFactor = impactFactors.unitaryImpact(target, indicator).amount
        val base = impactFactor.zeroth
        val absoluteSensibility = impactFactor.first[parameterIndex]
        return absoluteSensibility / base
    }

    fun getContribution(
        target: MatrixColumnIndex<DualNumber>,
        indicator: MatrixColumnIndex<DualNumber>,
    ): DualNumber {
        TODO()
//        val quantity = supply.intensityOf(target).amount
//        val ratio = impactFactors.valueRatio(target, indicator).amount
//        return with(DualOperations(parameters.size())) {
//            quantity * ratio
//        }
    }
}
