package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.allocation.Allocation
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.ProcessValue
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import ch.kleis.lcaplugin.core.matrix.*
import ch.kleis.lcaplugin.core.matrix.impl.Solver

class Assessment(
    system: SystemValue,
    targetProcess: ProcessValue,
    private val solver: Solver = Solver.INSTANCE
) {
    private val observableMatrix: ObservableMatrix
    private val controllableMatrix: ControllableMatrix
    private val demandMatrix: DemandMatrix
    private val observablePorts: IndexedCollection<MatrixColumnIndex>
    private val controllablePorts: IndexedCollection<MatrixColumnIndex>

    val allocatedSystem: SystemValue

    init {
        allocatedSystem = Allocation().apply(system)
        val processes = allocatedSystem.processes
        val substanceCharacterizations = allocatedSystem.substanceCharacterizations

        val observableProducts = processes
            .flatMap { it.products }
            .map { it.product }
        val observableSubstances = substanceCharacterizations
            .map { it.referenceExchange.substance }
        observablePorts = IndexedCollection(observableProducts.plus(observableSubstances))
        observableMatrix = ObservableMatrix(
            processes,
            substanceCharacterizations,
            observableProducts,
            observableSubstances
        )

        val terminalProducts = processes
            .flatMap { it.inputs }
            .map { it.product }
            .filter { !observableProducts.contains(it) }
        val terminalSubstances = processes
            .flatMap { it.biosphere }
            .map { it.substance }
            .filter { !observableSubstances.contains(it) }
        val indicators = substanceCharacterizations
            .flatMap { it.impacts }
            .map { it.indicator }
        controllablePorts = IndexedCollection(terminalProducts.plus(terminalSubstances).plus(indicators))
        controllableMatrix = ControllableMatrix(
            processes,
            substanceCharacterizations,
            terminalProducts,
            terminalSubstances,
            indicators
        )

        demandMatrix = DemandMatrix(
            targetProcess,
            observablePorts,
        )
    }

    fun inventory(): Inventory {
        val impactFactorMatrix = solver.solve(this.observableMatrix.matrix, this.controllableMatrix.matrix.negate())
            ?.let { ImpactFactorMatrix(observablePorts, controllablePorts, it) }
            ?: throw EvaluatorException("The system cannot be solved")
        val supplyMatrix = solver.solve(this.observableMatrix.matrix.transpose(), demandMatrix.matrix.transpose())
            ?.transpose()
            ?.let { SupplyMatrix(observablePorts, it) }
            ?: throw EvaluatorException("The system cannot be solved")
        return Inventory(impactFactorMatrix, supplyMatrix)
    }
}
