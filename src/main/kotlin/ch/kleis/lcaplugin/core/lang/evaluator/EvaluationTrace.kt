package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.value.*

class EvaluationTrace {
    private val stages = ArrayList<HashSet<MatrixRowIndex>>()
    private var currentStage = HashSet<MatrixRowIndex>()
    private val processes = HashSet<ProcessValue>()
    private val substanceCharacterizations = HashSet<SubstanceCharacterizationValue>()
    private val productDepthMap = HashMap<MatrixColumnIndex, Int>()

    companion object {
        fun empty(): EvaluationTrace {
            return EvaluationTrace()
        }
    }

    fun getProductOrder(): Comparator<MatrixColumnIndex> {
        return object : Comparator<MatrixColumnIndex> {
            override fun compare(o1: MatrixColumnIndex, o2: MatrixColumnIndex): Int {
                val d1 = productDepthMap[o1] ?: throw EvaluatorException("unknown ${o1}")
                val d2 = productDepthMap[o2] ?: throw EvaluatorException("unknown ${o2}")
                if (d1 < d2) {
                    return -1
                }
                if (d1 > d2) {
                    return 1
                }
                return when {
                    (o1 is ProductValue) && (o2 is SubstanceValue) -> -1
                    (o1 is SubstanceValue) && (o2 is ProductValue) -> 1
                    else -> o1.getUID().compareTo(o2.getUID())
                }
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

    fun getStages(): List<Set<MatrixRowIndex>> {
        return stages
    }

    fun getEntryPoint(): ProcessValue {
        if (stages.isEmpty()) {
            throw EvaluatorException("execution trace is empty")
        }
        val candidates = stages.first()
            .filterIsInstance<ProcessValue>()
        if (candidates.size > 1) {
            throw EvaluatorException("execution trace contains multiple entrypoint")
        }
        return candidates.first()
    }

    fun getSystemValue(): SystemValue {
        return SystemValue(
            processes,
            substanceCharacterizations,
        )
    }

    fun contains(process: ProcessValue): Boolean {
        return processes.contains(process)
    }

    fun add(process: ProcessValue) {
        processes.add(process)
        currentStage.add(process)
    }

    fun add(substanceCharacterization: SubstanceCharacterizationValue) {
        substanceCharacterizations.add(substanceCharacterization)
        currentStage.add(substanceCharacterization)
    }

    fun commit() {
        if (currentStage.isEmpty()) {
            return
        }

        val currentDepth = stages.size
        currentStage
            .filterIsInstance<ProcessValue>()
            .forEach { process ->
                process.products.forEach { exchange ->
                    productDepthMap[exchange.product] = currentDepth
                }
            }
        currentStage
            .filterIsInstance<SubstanceCharacterizationValue>()
            .forEach { sc ->
                productDepthMap[sc.referenceExchange.substance] = currentDepth
            }

        stages.add(currentStage)
        currentStage = HashSet()
    }
}
