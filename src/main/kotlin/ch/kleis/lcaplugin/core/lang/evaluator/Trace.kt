package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.value.MatrixRowIndex
import ch.kleis.lcaplugin.core.lang.value.ProcessValue
import ch.kleis.lcaplugin.core.lang.value.SubstanceCharacterizationValue
import ch.kleis.lcaplugin.core.lang.value.SystemValue

class Trace {
    private val stages = ArrayList<HashSet<MatrixRowIndex>>()
    private var currentStage = HashSet<MatrixRowIndex>()
    private val processes = HashSet<ProcessValue>()
    private val substanceCharacterizations = HashSet<SubstanceCharacterizationValue>()

    companion object {
        fun empty(): Trace {
            return Trace()
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
        if (currentStage.isNotEmpty()) {
            stages.add(currentStage)
            currentStage = HashSet()
        }
    }
}
