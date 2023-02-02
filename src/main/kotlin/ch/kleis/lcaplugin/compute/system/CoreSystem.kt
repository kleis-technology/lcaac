package ch.kleis.lcaplugin.compute.system

import ch.kleis.lcaplugin.compute.matrix.ControllableMatrix
import ch.kleis.lcaplugin.compute.matrix.IndexedCollection
import ch.kleis.lcaplugin.compute.matrix.InventoryMatrix
import ch.kleis.lcaplugin.compute.matrix.ObservableMatrix
import ch.kleis.lcaplugin.compute.matrix.impl.Solver
import ch.kleis.lcaplugin.compute.model.Flow
import ch.kleis.lcaplugin.compute.model.UnitProcess

class CoreSystem(
    processes: List<UnitProcess>
) {
    private val processes: IndexedCollection<UnitProcess>

    private val observableFlows: IndexedCollection<Flow<*>>
    private val observable: ObservableMatrix

    private val controllableFlows: IndexedCollection<Flow<*>>
    private val controllable: ControllableMatrix

    init {
        this.processes = IndexedCollection(processes)
        val outputs = this.processes.getElements()
            .flatMap { it.outputs }
            .map { it.flow }
        val inputs = this.processes.getElements()
            .flatMap { it.inputs }
            .map { it.flow }

        // default: observables -> all the outputs
        this.observableFlows = IndexedCollection(outputs)
        this.observable = ObservableMatrix(this.processes, this.observableFlows)

        // default: controllables -> all inputs which are not outputs
        this.controllableFlows = IndexedCollection(inputs.filter { !outputs.contains(it) })
        this.controllable = ControllableMatrix(this.processes, this.controllableFlows)
    }

    fun getProcess(name: String): UnitProcess {
        return processes.get(name)
    }

    fun inventory(): InventoryMatrix {
        val data = Solver.INSTANCE.solve(this.observable.matrix, this.controllable.matrix)
        return InventoryMatrix(this.observableFlows, this.controllableFlows, data)
    }
}
