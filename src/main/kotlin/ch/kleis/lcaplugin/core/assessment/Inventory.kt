package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.matrix.ImpactFactorMatrix
import ch.kleis.lcaplugin.core.matrix.IndexedCollection
import ch.kleis.lcaplugin.core.matrix.SupplyMatrix

data class Inventory<Q, M>(
    val impactFactors: ImpactFactorMatrix<Q, M>,
    val supply: SupplyMatrix<Q, M>,
) {
    fun getObservablePorts(): IndexedCollection<MatrixColumnIndex<Q>> {
        return impactFactors.observablePorts
    }

    fun getControllablePorts(): IndexedCollection<MatrixColumnIndex<Q>> {
        return impactFactors.controllablePorts
    }
}
