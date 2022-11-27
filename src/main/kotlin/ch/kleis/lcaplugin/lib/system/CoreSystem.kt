package ch.kleis.lcaplugin.lib.system

import ch.kleis.lcaplugin.lib.matrix.ControllableMatrix
import ch.kleis.lcaplugin.lib.matrix.InventoryMatrix
import ch.kleis.lcaplugin.lib.matrix.ObservableMatrix
import ch.kleis.lcaplugin.lib.matrix.impl.Solver
import ch.kleis.lcaplugin.lib.model.Flow
import ch.kleis.lcaplugin.lib.model.UnitProcess
import ch.kleis.lcaplugin.lib.registry.IndexedCollection

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

    fun getControllableFlows(): List<Flow<*>> {
        return this.controllableFlows.getElements()
    }

    fun inventory(): InventoryMatrix {
        val data = Solver.INSTANCE.solve(this.observable.matrix, this.controllable.matrix)
        return InventoryMatrix(this.observableFlows, this.controllableFlows, data)
    }
}
