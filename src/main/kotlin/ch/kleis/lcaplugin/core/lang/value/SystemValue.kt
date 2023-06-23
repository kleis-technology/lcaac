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

    fun plus(process: ProcessValue): SystemValue {
        processes.add(process)
        return this
    }

    fun plus(substanceCharacterization: SubstanceCharacterizationValue): SystemValue {
        substanceCharacterizations.add(substanceCharacterization)
        return this
    }

    fun plus(unlinkedSystem: SystemValue): SystemValue {
        unlinkedSystem.processes.forEach { processes.add(it) }
        unlinkedSystem.substanceCharacterizations.forEach { substanceCharacterizations.add(it) }
        return this
    }
}

data class CharacterizationFactorValue(
    val output: ExchangeValue,
    val input: ExchangeValue,
) : Value
