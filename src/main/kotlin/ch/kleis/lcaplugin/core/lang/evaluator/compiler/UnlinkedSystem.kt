package ch.kleis.lcaplugin.core.lang.evaluator.compiler

import ch.kleis.lcaplugin.core.lang.value.ProcessValue
import ch.kleis.lcaplugin.core.lang.value.SubstanceCharacterizationValue

class UnlinkedSystem(
    private val processes: Set<ProcessValue>,
    private val substanceCharacterizations: Set<SubstanceCharacterizationValue>,
) {

    companion object {
        fun empty(): UnlinkedSystem {
            return UnlinkedSystem(emptySet(), emptySet())
        }
    }

    fun containsProcess(process: ProcessValue): Boolean {
        return processes.contains(process)
    }

    fun plus(process: ProcessValue): UnlinkedSystem {
        return UnlinkedSystem(
            processes.plus(process),
            substanceCharacterizations,
        )
    }

    fun plus(substanceCharacterization: SubstanceCharacterizationValue): UnlinkedSystem {
        return UnlinkedSystem(
            processes,
            substanceCharacterizations.plus(substanceCharacterization),
        )
    }

    fun plus(unlinkedSystem: UnlinkedSystem): UnlinkedSystem {
        return UnlinkedSystem(
            processes.plus(unlinkedSystem.processes),
            substanceCharacterizations.plus(unlinkedSystem.substanceCharacterizations),
        )
    }

    fun getProcesses(): Set<ProcessValue> {
        return processes
    }

    fun getSubstanceCharacterizations(): Set<SubstanceCharacterizationValue> {
        return substanceCharacterizations
    }
}
