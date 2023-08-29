package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import ch.kleis.lcaplugin.core.math.basic.BasicMatrix
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.core.matrix.ImpactFactorMatrix
import ch.kleis.lcaplugin.core.matrix.IndexedCollection
import ch.kleis.lcaplugin.core.matrix.SupplyMatrix

class ContributionAnalysis(
    val impactFactors: ImpactFactorMatrix<BasicNumber, BasicMatrix>,
    val supply: SupplyMatrix<BasicNumber, BasicMatrix>,
    val system: SystemValue<BasicNumber>,
    val allocatedSystem: SystemValue<BasicNumber>,
) {
    fun getObservablePorts(): IndexedCollection<MatrixColumnIndex<BasicNumber>> {
        return impactFactors.observablePorts
    }

    fun getControllablePorts(): IndexedCollection<MatrixColumnIndex<BasicNumber>> {
        return impactFactors.controllablePorts
    }
}
