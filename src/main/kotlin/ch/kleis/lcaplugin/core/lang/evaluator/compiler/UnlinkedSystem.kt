package ch.kleis.lcaplugin.core.lang.evaluator.compiler

import ch.kleis.lcaplugin.core.lang.value.ProcessValue
import ch.kleis.lcaplugin.core.lang.value.SubstanceCharacterizationValue

class UnlinkedSystem {
    private val processes: MutableSet<ProcessValue> = HashSet()
    private val substanceCharacterizations: MutableSet<SubstanceCharacterizationValue> = HashSet()

    companion object {
        fun empty(): UnlinkedSystem {
            return UnlinkedSystem()
        }
    }

    fun containsProcess(process: ProcessValue): Boolean {
        return processes.contains(process)
    }

    fun addProcess(process: ProcessValue) {
        processes.add(process)
    }

    fun addSubstanceCharacterization(substanceCharacterization: SubstanceCharacterizationValue) {
        substanceCharacterizations.add(substanceCharacterization)
    }

    fun getProcesses(): Set<ProcessValue> {
        return processes
    }

    fun getSubstanceCharacterizations(): Set<SubstanceCharacterizationValue> {
        return substanceCharacterizations
    }
}
