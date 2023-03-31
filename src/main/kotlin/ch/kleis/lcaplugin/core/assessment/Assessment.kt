package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.value.*
import ch.kleis.lcaplugin.core.matrix.*
import ch.kleis.lcaplugin.core.matrix.impl.Solver

class Assessment(
    system: SystemValue,
    private val solver: Solver = Solver.INSTANCE
) {
    private val observableMatrix: ObservableMatrix
    private val controllableMatrix: ControllableMatrix
    private val observablePorts: IndexedCollection<MatrixColumnIndex>
    private val controllablePorts: IndexedCollection<MatrixColumnIndex>

    init {
        val allocatedSystem = applyAllocation(system)
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
    }

    fun inventory(): InventoryResult {
        val data = solver.solve(this.observableMatrix.matrix, this.controllableMatrix.matrix.negate()) ?: return InventoryError("The system cannot be solved")
        return InventoryMatrix(this.observablePorts, this.controllablePorts, data)
    }

    fun applyAllocation(system: SystemValue): SystemValue {
        var allocatedSystem = SystemValue.empty()
        system.processes.forEach { processValue ->
            run {
                val totalAllocation = totalAllocation(processValue)
                processValue.products.forEach { technoExchangeValue ->
                    run {
                        val allocatedProcess = ProcessValue(
                            processValue.name,
                            listOf(technoExchangeValue),
                            applyAllocationToInputs(processValue.inputs, technoExchangeValue.allocation, totalAllocation),
                            applyAllocationToBioSphere(processValue.biosphere, technoExchangeValue.allocation, totalAllocation)
                        )
                        allocatedSystem = allocatedSystem.plus(allocatedProcess)
                    }
                }
            }
        }
        return allocatedSystem
    }

    fun totalAllocation(processValue: ProcessValue): Double {
        allocationUnitCheck(processValue)
        return processValue.products.sumOf { it.allocation.referenceValue() }
    }

    fun allocationUnitCheck(processValue: ProcessValue) {
        if (processValue.products.map { it.allocation.unit.dimension }.distinct().count() > 1){
            throw EvaluatorException("non-consistent allocation units for process ${processValue.name}")
        }
    }

    private fun applyAllocationToInputs(inputs: List<TechnoExchangeValue>, allocation: QuantityValue, totalAllocation: Double): List<TechnoExchangeValue>{
        return inputs.map { applyAllocationToInput(it, allocation, totalAllocation) }
    }

    private fun applyAllocationToInput(technoExchangeValue: TechnoExchangeValue, allocation: QuantityValue, totalAllocation: Double): TechnoExchangeValue{
        return TechnoExchangeValue(
            QuantityValue(
        technoExchangeValue.quantity.amount*allocation.referenceValue()/totalAllocation,
                technoExchangeValue.quantity.unit
            ),
            technoExchangeValue.product,
            technoExchangeValue.allocation
        )
    }

    private fun applyAllocationToBioSphere(biosphere: List<BioExchangeValue>, allocation: QuantityValue, totalAllocation: Double): List<BioExchangeValue>{
        return biosphere.map { applyAllocationToBioExchange(it, allocation, totalAllocation) }
    }

    private fun applyAllocationToBioExchange(bioExchange: BioExchangeValue, allocation: QuantityValue, totalAllocation: Double): BioExchangeValue{
        return BioExchangeValue(
            QuantityValue(
                bioExchange.quantity.amount*allocation.referenceValue()/totalAllocation,
                bioExchange.quantity.unit
            ),
            bioExchange.substance
        )
    }
}
