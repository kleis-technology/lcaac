package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.value.*
import kotlin.math.max

class EvaluationTrace<Q> {
    private val stages = ArrayList<HashSet<MatrixRowIndex<Q>>>()
    private var currentStage = HashSet<MatrixRowIndex<Q>>()
    private val processes = HashSet<ProcessValue<Q>>()
    private val substanceCharacterizations = HashSet<SubstanceCharacterizationValue<Q>>()
    private val depthMap = HashMap<MatrixColumnIndex<Q>, Int>()

    companion object {
        fun <Q> empty(): EvaluationTrace<Q> {
            return EvaluationTrace()
        }
    }

    // TODO: Test mixed comparison with observable and controllable
    fun getComparator(): Comparator<MatrixColumnIndex<Q>> {
        return object : Comparator<MatrixColumnIndex<Q>> {
            override fun compare(o1: MatrixColumnIndex<Q>, o2: MatrixColumnIndex<Q>): Int {
                val d1 = depthMap[o1] ?: throw EvaluatorException("unknown $o1")
                val d2 = depthMap[o2] ?: throw EvaluatorException("unknown $o2")
                if (d1 < d2) {
                    return -1
                }
                if (d1 > d2) {
                    return 1
                }
                return o1.getUID().compareTo(o2.getUID())
            }
        }
    }

    fun getNumberOfStages(): Int {
        return stages.size
    }

    fun getNumberOfProcesses(): Int {
        return processes.size
    }

    fun getNumberOfSubstanceCharacterizations(): Int {
        return substanceCharacterizations.size
    }

    fun getStages(): List<Set<MatrixRowIndex<Q>>> {
        return stages
    }

    fun getEntryPoint(): ProcessValue<Q> {
        if (stages.isEmpty()) {
            throw EvaluatorException("execution trace is empty")
        }
        val candidates = stages.first()
            .filterIsInstance<ProcessValue<Q>>()
        if (candidates.size > 1) {
            throw EvaluatorException("execution trace contains multiple entrypoint")
        }
        return candidates.first()
    }

    fun getSystemValue(): SystemValue<Q> {
        return SystemValue(
            processes,
            substanceCharacterizations,
        )
    }

    fun contains(process: ProcessValue<Q>): Boolean {
        return processes.contains(process)
    }

    fun add(process: ProcessValue<Q>) {
        processes.add(process)
        currentStage.add(process)
    }

    fun add(substanceCharacterization: SubstanceCharacterizationValue<Q>) {
        substanceCharacterizations.add(substanceCharacterization)
        currentStage.add(substanceCharacterization)
    }

    fun commit() {
        if (currentStage.isEmpty()) {
            return
        }

        val currentDepth = stages.size
        currentStage
            .filterIsInstance<ProcessValue<Q>>()
            .forEach { process ->
                process.products.forEach { exchange ->
                    updateDepthMap(exchange.product, currentDepth)
                }
                process.inputs.forEach { exchange ->
                    updateDepthMap(exchange.product, currentDepth + 1)
                }
                process.biosphere.forEach { exchange ->
                    updateDepthMap(exchange.substance, currentDepth + 1)
                }
                process.impacts.forEach { exchange ->
                    updateDepthMap(exchange.indicator, currentDepth + 1)
                }
            }
        currentStage
            .filterIsInstance<SubstanceCharacterizationValue<Q>>()
            .forEach { sc ->
                updateDepthMap(sc.referenceExchange.substance, currentDepth)
                sc.impacts.forEach { exchange ->
                    updateDepthMap(exchange.indicator, currentDepth + 1)
                }
            }

        stages.add(currentStage)
        currentStage = HashSet()
    }

    private fun updateDepthMap(port: MatrixColumnIndex<Q>, depth: Int) {
        depthMap[port] = depthMap[port]?.let {
            if (it >= depth - 1) max(it, depth)
            else it
        } ?: depth
    }
}
