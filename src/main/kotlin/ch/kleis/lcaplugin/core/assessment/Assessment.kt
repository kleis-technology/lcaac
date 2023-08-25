package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.allocation.Allocation
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.ProcessValue
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import ch.kleis.lcaplugin.core.math.QuantityOperations
import ch.kleis.lcaplugin.core.matrix.*
import ch.kleis.lcaplugin.core.matrix.impl.Solver

class Assessment<Q>(
    system: SystemValue<Q>,
    targetProcess: ProcessValue<Q>,
    private val ops: QuantityOperations<Q>,
    private val solver: Solver = Solver.INSTANCE // TODO: use ops instead of solver stuff here
) {
    private val observableMatrix: ObservableMatrix<Q>
    private val controllableMatrix: ControllableMatrix<Q>
    private val demandMatrix: DemandMatrix<Q>
    private val observablePorts: IndexedCollection<MatrixColumnIndex<Q>>
    private val controllablePorts: IndexedCollection<MatrixColumnIndex<Q>>

    val allocatedSystem: SystemValue<Q>

    init {
        allocatedSystem = Allocation(ops).apply(system)
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
            observableSubstances,
            ops,
        )

        val terminalProducts = processes
            .flatMap { it.inputs }
            .map { it.product }
            .filter { !observableProducts.contains(it) }
        val terminalSubstances = processes
            .flatMap { it.biosphere }
            .map { it.substance }
            .filter { !observableSubstances.contains(it) }
        val indicators = (processes + substanceCharacterizations)
            .flatMap { it.impacts }
            .map { it.indicator }
        controllablePorts = IndexedCollection(terminalProducts.plus(terminalSubstances).plus(indicators))
        controllableMatrix = ControllableMatrix(
            processes,
            substanceCharacterizations,
            terminalProducts,
            terminalSubstances,
            indicators,
            ops,
        )

        demandMatrix = DemandMatrix(
            targetProcess,
            observablePorts,
            ops,
        )
    }

    fun inventory(): Inventory<Q> {
        val impactFactorMatrix = solver.solve(this.observableMatrix.matrix, this.controllableMatrix.matrix.negate())
            ?.let { ImpactFactorMatrix(observablePorts, controllablePorts, it, ops) }
            ?: throw EvaluatorException("The system cannot be solved")
        val supplyMatrix = solver.solve(this.observableMatrix.matrix.transpose(), demandMatrix.matrix.transpose())
            ?.transpose()
            ?.let { SupplyMatrix(observablePorts, it, ops) }
            ?: throw EvaluatorException("The system cannot be solved")
        return Inventory(impactFactorMatrix, supplyMatrix)
    }
}
