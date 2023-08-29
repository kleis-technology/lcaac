package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.allocation.Allocation
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import ch.kleis.lcaplugin.core.math.dual.DualNumber
import ch.kleis.lcaplugin.core.math.dual.DualOperations
import ch.kleis.lcaplugin.core.matrix.*

class SensitivityAnalysisProgram(
    private val system: SystemValue<DualNumber>,
    private val parameters: ParameterVector<DualNumber>,
) {
    private val ops = DualOperations(parameters.size())

    @Suppress("DuplicatedCode")
    fun run(): SensitivityAnalysis {
        val allocatedSystem = Allocation(ops).apply(system)

        val processes = allocatedSystem.processes
        val substanceCharacterizations = allocatedSystem.substanceCharacterizations
        val observableProducts = processes
            .flatMap { it.products }
            .map { it.product }
        val observableSubstances = substanceCharacterizations
            .map { it.referenceExchange.substance }
        val observablePorts = IndexedCollection(observableProducts.plus(observableSubstances))
        val observableMatrix = ObservableMatrix(
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
        val controllablePorts = IndexedCollection(terminalProducts.plus(terminalSubstances).plus(indicators))
        val controllableMatrix = ControllableMatrix(
            processes,
            substanceCharacterizations,
            terminalProducts,
            terminalSubstances,
            indicators,
            ops,
        )

        with(ops) {
            val impactFactorMatrix = controllableMatrix.data.negate()
                .matDiv(observableMatrix.data)
                ?.let { ImpactFactorMatrix(observablePorts, controllablePorts, it, ops) }
                ?: throw EvaluatorException("The system cannot be solved")
            return SensitivityAnalysis(impactFactorMatrix, parameters)
        }
    }
}
