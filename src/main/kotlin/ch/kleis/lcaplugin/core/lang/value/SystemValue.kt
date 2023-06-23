package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.HasUID


data class SystemValue(
    val processes: MutableSet<ProcessValue>,
    val substanceCharacterizations: MutableSet<SubstanceCharacterizationValue>,
) : Value, HasUID {
    companion object {
        fun empty(): SystemValue {
            return SystemValue(HashSet(), HashSet())
        }
    }

    fun containsProcess(process: ProcessValue): Boolean {
        return processes.contains(process)
    }

    fun add(process: ProcessValue): SystemValue {
        processes.add(process)
        return this
    }

    fun add(substanceCharacterization: SubstanceCharacterizationValue): SystemValue {
        substanceCharacterizations.add(substanceCharacterization)
        return this
    }

    fun add(unlinkedSystem: SystemValue): SystemValue {
        unlinkedSystem.processes.forEach { processes.add(it) }
        unlinkedSystem.substanceCharacterizations.forEach { substanceCharacterizations.add(it) }
        return this
    }
}

data class CharacterizationFactorValue(
    val output: ExchangeValue,
    val input: ExchangeValue,
) : Value
