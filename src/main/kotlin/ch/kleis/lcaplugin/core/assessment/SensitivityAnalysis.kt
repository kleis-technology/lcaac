package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.ParameterName
import ch.kleis.lcaplugin.core.lang.value.*
import ch.kleis.lcaplugin.core.math.dual.DualMatrix
import ch.kleis.lcaplugin.core.math.dual.DualNumber
import ch.kleis.lcaplugin.core.math.dual.DualOperations
import ch.kleis.lcaplugin.core.matrix.ImpactFactorMatrix
import ch.kleis.lcaplugin.core.matrix.IndexedCollection
import ch.kleis.lcaplugin.core.matrix.IntensityMatrix
import ch.kleis.lcaplugin.core.matrix.ParameterVector
import org.jetbrains.kotlinx.multik.ndarray.data.get

class SensitivityAnalysis(
    private val entryPoint: ProcessValue<DualNumber>,
    private val impactFactors: ImpactFactorMatrix<DualNumber, DualMatrix>,
    intensity: IntensityMatrix<DualNumber, DualMatrix>,
    allocatedSystem: SystemValue<DualNumber>,
    private val parameters: ParameterVector<DualNumber>,
) {
    private val ops = DualOperations(parameters.size())
    private val contributionAnalysis = ContributionAnalysis(
        impactFactors,
        intensity,
        allocatedSystem,
        ops,
    )

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
        val impact = getPortContribution(target, indicator).amount
        val base = impact.zeroth
        val absoluteSensibility = impact.first[parameterIndex]
        return absoluteSensibility / base
    }

    fun getPortContribution(
        port: MatrixColumnIndex<DualNumber>,
        indicator: MatrixColumnIndex<DualNumber>,
    ): QuantityValue<DualNumber> {
        return contributionAnalysis.getPortContribution(port, indicator)
    }
}
