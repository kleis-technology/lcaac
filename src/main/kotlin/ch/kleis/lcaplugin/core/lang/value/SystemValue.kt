package ch.kleis.lcaplugin.core.lang.value

import arrow.optics.optics
import ch.kleis.lcaplugin.core.HasUID


@optics
data class SystemValue(
    val processes: Set<ProcessValue>,
    val substanceCharacterizations: Set<SubstanceCharacterizationValue>,
) : Value, HasUID {
    companion object {
        fun empty(): SystemValue {
            return SystemValue(emptySet(), emptySet())
        }
    }

    fun containsProcess(process: ProcessValue): Boolean {
        return processes.contains(process)
    }

    fun plus(process: ProcessValue): SystemValue {
        return SystemValue(
            processes.plus(process),
            substanceCharacterizations,
        )
    }

    fun plus(substanceCharacterization: SubstanceCharacterizationValue): SystemValue {
        return SystemValue(
            processes,
            substanceCharacterizations.plus(substanceCharacterization),
        )
    }

    fun plus(unlinkedSystem: SystemValue): SystemValue {
        return SystemValue(
            processes.plus(unlinkedSystem.processes),
            substanceCharacterizations.plus(unlinkedSystem.substanceCharacterizations),
        )
    }
}

@optics
data class CharacterizationFactorValue(
    val output: ExchangeValue,
    val input: ExchangeValue,
) : Value {
    companion object
}
