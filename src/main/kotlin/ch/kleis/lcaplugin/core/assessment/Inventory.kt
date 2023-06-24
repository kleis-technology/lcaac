package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.MatrixRowIndex
import ch.kleis.lcaplugin.core.matrix.ImpactFactorMatrix
import ch.kleis.lcaplugin.core.matrix.IndexedCollection
import ch.kleis.lcaplugin.core.matrix.SupplyMatrix

data class Inventory(
    val impactFactors: ImpactFactorMatrix,
    val supply: SupplyMatrix,
) {
    fun getObservablePorts(): IndexedCollection<MatrixColumnIndex> {
        return impactFactors.observablePorts
    }

    fun getControllablePorts(): IndexedCollection<MatrixColumnIndex> {
        return impactFactors.controllablePorts
    }
}
