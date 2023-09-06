package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.allocation.Allocation
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.value.ProcessValue
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import ch.kleis.lcaplugin.core.math.Operations
import ch.kleis.lcaplugin.core.matrix.*

data class AnalysisResults<Q, M>(
    val allocatedSystem: SystemValue<Q>,
    val impactFactors: ImpactFactorMatrix<Q, M>,
    val intensity: IntensityMatrix<Q, M>,
)

class AnalysisProgram<Q, M>(
    private val system: SystemValue<Q>,
    private val targetProcess: ProcessValue<Q>,
    private val ops: Operations<Q, M>,
) {
    fun run(): AnalysisResults<Q, M> {
        val allocatedSystem = Allocation(ops).apply(system)

        val processes = allocatedSystem.processes
        val substanceCharacterizations = allocatedSystem.substanceCharacterizations

        val observableProducts = processes
            .flatMap { it.products }
            .map { it.product }
        val observableSubstances = substanceCharacterizations
            .map { it.referenceExchange.substance }

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
        val controllableMatrix = ControllableMatrix(
            processes,
            substanceCharacterizations,
            terminalProducts,
            terminalSubstances,
            indicators,
            ops,
        )

        val demandMatrix = DemandMatrix(
            targetProcess,
            observableMatrix.ports,
            ops,
        )

        /*
            D = 1 x observables
            O = processes x observables
            C = processes x controllables

            Emission factors F is s.t.
                O . F = -C
            Intensity J is s.t.
                J . O = D
         */

        with(ops) {
            val impactFactorMatrix = controllableMatrix.data.negate()
                .matDiv(observableMatrix.data)
                ?.let { ImpactFactorMatrix(observableMatrix.ports, controllableMatrix.ports, it, ops) }
                ?: throw EvaluatorException("The system cannot be solved")
            val intensityMatrix = demandMatrix.data
                .matTransposeDiv(observableMatrix.data)
                ?.let { IntensityMatrix(observableMatrix.connections, it, ops) }
                ?: throw EvaluatorException("The system cannot be solved")
            return AnalysisResults(allocatedSystem, impactFactorMatrix, intensityMatrix)
        }
    }
}
