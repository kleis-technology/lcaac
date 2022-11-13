package ch.kleis.lcaplugin.compute.model

import ch.kleis.lcaplugin.compute.matrix.*
import ch.kleis.lcaplugin.compute.matrix.impl.Solver

class System(processes: List<Process>) {
    private val processes: IndexedCollection<Process>

    private val observableFlows: IndexedCollection<IntermediaryFlow>
    private val observable: ObservableMatrix

    private val controllableFlows: IndexedCollection<IntermediaryFlow>
    private val controllable: ControllableMatrix

    private val elementaryFlows: IndexedCollection<ElementaryFlow>
    private val bio: BioMatrix

    init {
        this.processes = IndexedCollection(processes)
        val products = this.processes.getElements()
            .flatMap { it.products }
            .map { it.flow }
        val inputs = this.processes.getElements()
            .flatMap { it.inputs }
            .map { it.flow }
        val controllables = inputs // by default : control input flows that are not produced by anything
            .filter { !products.contains(it) }
        val observables = this.processes.getElements()
            .flatMap { it.products + it.inputs }
            .map { it.flow }
            .filter { !controllables.contains(it) }

        this.observableFlows = IndexedCollection(observables)
        this.observable = ObservableMatrix(this.processes, this.observableFlows)

        this.controllableFlows = IndexedCollection(controllables)
        this.controllable = ControllableMatrix(this.processes, this.controllableFlows)

        this.elementaryFlows = IndexedCollection(this.processes.getElements()
            .flatMap { it.emissions + it.resources }
            .map { it.flow })
        this.bio = BioMatrix(this.processes, this.elementaryFlows)
    }

    fun getProcess(name: String): Process {
        return processes.get(name)
    }

    fun observe(
        indicators: List<Indicator>,
        controllableCfs: List<CharacterizationFactor>,
        elementaryCfs: List<CharacterizationFactor>,
    ): ObservableFactorMatrix {
        val outputs = IndexedCollection(indicators)
        val controllableFactor = ControllableFactorMatrix(this.controllableFlows, outputs, controllableCfs)
        val bioFactor = BioFactorMatrix(this.elementaryFlows, outputs, elementaryCfs)

        val rhs = rhs(bioFactor, controllableFactor)
        val solution = Solver.INSTANCE.solve(this.observable.matrix, rhs)
        return ObservableFactorMatrix(this.observableFlows, outputs, solution)
    }

    private fun rhs(
        bioFactor: BioFactorMatrix,
        controllableFactor: ControllableFactorMatrix
    ) = bio.matrix.multiply(bioFactor.matrix).sub(controllable.matrix.multiply(controllableFactor.matrix))
}
