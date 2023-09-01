package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.QuantityValue
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import ch.kleis.lcaplugin.core.lang.value.UnitValue
import ch.kleis.lcaplugin.core.math.basic.BasicMatrix
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.core.math.basic.BasicOperations
import ch.kleis.lcaplugin.core.matrix.ImpactFactorMatrix
import ch.kleis.lcaplugin.core.matrix.IndexedCollection
import ch.kleis.lcaplugin.core.matrix.SupplyMatrix

class ContributionAnalysis(
    private val impactFactors: ImpactFactorMatrix<BasicNumber, BasicMatrix>,
    private val supply: SupplyMatrix<BasicNumber, BasicMatrix>,
    private val allocatedSystem: SystemValue<BasicNumber>,
) {
    fun getImpactFactors(): ImpactFactorMatrix<BasicNumber, BasicMatrix> {
        return impactFactors
    }

    fun getAllocatedSystem(): SystemValue<BasicNumber> {
        return allocatedSystem
    }

    fun getImpactFactorsOf(target: MatrixColumnIndex<BasicNumber>): Map<MatrixColumnIndex<BasicNumber>, QuantityValue<BasicNumber>> {
        return impactFactors.rowAsMap(target)
    }

    fun getNumberOfImpactFactors(): Int {
        return impactFactors.nbCells()
    }

    fun getObservablePorts(): IndexedCollection<MatrixColumnIndex<BasicNumber>> {
        return impactFactors.observablePorts
    }

    fun getControllablePorts(): IndexedCollection<MatrixColumnIndex<BasicNumber>> {
        return impactFactors.controllablePorts
    }

    fun supplyOf(target: MatrixColumnIndex<BasicNumber>): QuantityValue<BasicNumber> {
        return supply.quantityOf(target)
    }

    fun getContribution(
        target: MatrixColumnIndex<BasicNumber>,
        indicator: MatrixColumnIndex<BasicNumber>,
    ): QuantityValue<BasicNumber> {
        val quantity = supply.quantityOf(target)
        val ratio = impactFactors.valueRatio(target, indicator)
        return with(BasicOperations) {
            QuantityValue(
                quantity.amount * ratio.amount,
                UnitValue(
                    quantity.unit.symbol.multiply(ratio.unit.symbol),
                    quantity.unit.scale * ratio.unit.scale,
                    quantity.unit.dimension.multiply(ratio.unit.dimension),
                )
            )
        }
    }
}
