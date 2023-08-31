package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.allocation.Allocation
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.ProcessValue
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import ch.kleis.lcaplugin.core.math.Operations
import ch.kleis.lcaplugin.core.matrix.*
import org.mozilla.javascript.EvaluatorException

class Assessment<Q, M>(
    system: SystemValue<Q>,
    targetProcess: ProcessValue<Q>,
    private val ops: Operations<Q, M>,
) {
    private val observableMatrix: ObservableMatrix<Q, M>
    private val controllableMatrix: ControllableMatrix<Q, M>
    private val demandMatrix: DemandMatrix<Q, M>
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

    fun inventory(): Inventory<Q, M> {
        val controllableMatrix = controllableMatrix
        val observableMatrix = observableMatrix
        val demandMatrix = demandMatrix

        with(ops) {
            val impactFactorMatrix = controllableMatrix.data.negate()
                .matDiv(observableMatrix.data)
                ?.let { ImpactFactorMatrix(observablePorts, controllablePorts, it, ops) }
                ?: throw EvaluatorException("The system cannot be solved")
            val supplyMatrix = demandMatrix.data
                .matTransposeDiv(observableMatrix.data)
                ?.let { SupplyMatrix(observablePorts, it, ops) }
                ?: throw EvaluatorException("The system cannot be solved")
            return Inventory(impactFactorMatrix, supplyMatrix)
        }
    }
}
