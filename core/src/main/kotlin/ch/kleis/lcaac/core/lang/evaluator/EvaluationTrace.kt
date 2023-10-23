package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.value.*
import kotlin.math.max

class EvaluationTrace<Q> {
    private var nbStages = 0
    private var entryPoint: ProcessValue<Q>? = null
    private var currentStage = HashSet<MatrixRowIndex<Q>>()
    private val processes = HashSet<ProcessValue<Q>>()
    private val substanceCharacterizations = HashSet<SubstanceCharacterizationValue<Q>>()
    private val depthMap = HashMap<MatrixColumnIndex<Q>, Int>()

    companion object {
        fun <Q> empty(): EvaluationTrace<Q> {
            return EvaluationTrace()
        }
    }

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
        return nbStages
    }

    fun getNumberOfProcesses(): Int {
        return processes.size
    }

    fun getNumberOfSubstanceCharacterizations(): Int {
        return substanceCharacterizations.size
    }

    fun getEntryPoint(): ProcessValue<Q> {
        if (nbStages == 0) {
            throw EvaluatorException("execution trace is empty")
        }
        return entryPoint ?: throw EvaluatorException("missing entrypoint")
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

    fun contains(substanceCharacterization: SubstanceCharacterizationValue<Q>): Boolean {
        return substanceCharacterizations.contains(substanceCharacterization)
    }

    fun add(connection: MatrixRowIndex<Q>) {
        when(connection) {
            is ProcessValue -> addProcess(connection)
            is SubstanceCharacterizationValue -> addSubstanceCharacterization(connection)
        }
    }

    fun addProcess(process: ProcessValue<Q>) {
        if (nbStages == 0) {
            if (entryPoint == null) {
                entryPoint = process
            } else throw EvaluatorException("execution trace contains multiple entrypoint")
        }
        if (!processes.contains(process)) {
            processes.add(process)
            currentStage.add(process)
        }
    }

    fun addSubstanceCharacterization(substanceCharacterization: SubstanceCharacterizationValue<Q>) {
        if (!substanceCharacterizations.contains(substanceCharacterization)) {
            substanceCharacterizations.add(substanceCharacterization)
            currentStage.add(substanceCharacterization)
        }
    }

    fun commit() {
        if (currentStage.isEmpty()) {
            return
        }

        val currentDepth = nbStages
        currentStage
            .filterIsInstance<ProcessValue<Q>>()
            .forEach { process ->
                process.products.forEach { exchange ->
                    updateDepthMap(exchange.product, currentDepth)
                }
                val processProducts = process.products.map { it.product }
                process.inputs.forEach { exchange ->
                    if (!processProducts.contains(exchange.product)) { // avoid self loop
                        updateDepthMap(exchange.product, currentDepth + 1)
                    }
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

        nbStages += 1
        currentStage = HashSet()
    }

    private fun updateDepthMap(port: MatrixColumnIndex<Q>, depth: Int) {
        depthMap[port] = depthMap[port]?.let {
            if (it >= depth - 1) max(it, depth)
            else it
        } ?: depth
    }
}
