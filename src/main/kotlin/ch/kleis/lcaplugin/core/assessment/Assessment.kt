package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.lang.VProcess
import ch.kleis.lcaplugin.core.lang.VProduct
import ch.kleis.lcaplugin.core.lang.VSystem
import ch.kleis.lcaplugin.core.matrix.*
import ch.kleis.lcaplugin.core.matrix.impl.Solver

class Assessment(
    system: VSystem,
    private val solver: Solver = Solver.INSTANCE
) {
    private val processes: IndexedCollection<VProcess>
    private val observableProducts: IndexedCollection<VProduct>
    private val observable: ObservableMatrix
    private val controllableProducts: IndexedCollection<VProduct>
    private val controllable: ControllableMatrix

    init {
        this.processes = IndexedCollection(system.processes)
        val referenceProducts = this.processes.getElements()
            .filter { it.exchanges.isNotEmpty() }
            .map { it.exchanges[0] } // the 1st product is the reference product
            .map { it.product }

        this.observableProducts = IndexedCollection(referenceProducts)
        this.observable = ObservableMatrix(this.processes, this.observableProducts)

        val otherProducts = this.processes.getElements()
            .flatMap { it.exchanges }
            .map { it.product }
            .filter { !referenceProducts.contains(it) }
        this.controllableProducts = IndexedCollection(otherProducts)
        this.controllable = ControllableMatrix(processes, controllableProducts)
    }

    fun inventory(): InventoryResult {
        val data = solver.solve(this.observable.matrix, this.controllable.matrix) ?: return InventoryError("The system cannot be solved")
        return InventoryMatrix(this.observableProducts, this.controllableProducts, data)
    }
}
