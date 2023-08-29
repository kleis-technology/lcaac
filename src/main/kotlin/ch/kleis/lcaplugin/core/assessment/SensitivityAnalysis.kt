package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.math.dual.DualMatrix
import ch.kleis.lcaplugin.core.math.dual.DualNumber
import ch.kleis.lcaplugin.core.matrix.ImpactFactorMatrix
import ch.kleis.lcaplugin.core.matrix.IndexedCollection
import ch.kleis.lcaplugin.core.matrix.ParameterVector

class SensitivityAnalysis(
    private val impactFactors: ImpactFactorMatrix<DualNumber, DualMatrix>,
    private val parameters: ParameterVector<DualNumber>,
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

    fun getImpactFactors(): ImpactFactorMatrix<DualNumber, DualMatrix> {
        return impactFactors
    }
}
