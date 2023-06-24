package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.matrix.ImpactFactorMatrix
import ch.kleis.lcaplugin.core.matrix.SupplyMatrix

data class Inventory(
    val impactFactors: ImpactFactorMatrix,
    val supply: SupplyMatrix,
)
