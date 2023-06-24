package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.HasUID


class SystemValue(
    processes: Set<ProcessValue>,
    substanceCharacterizations: Set<SubstanceCharacterizationValue>,
) : Value, HasUID {
    private val processes  = HashSet<ProcessValue>(processes)
    private val substanceCharacterizations = HashSet<SubstanceCharacterizationValue>(substanceCharacterizations)

    companion object {
        fun empty(): SystemValue {
            return SystemValue(HashSet(), HashSet())
        }
    }

    fun getProcesses(): Set<ProcessValue> {
        return processes
    }

    fun getSubstanceCharacterizations(): Set<SubstanceCharacterizationValue> {
        return substanceCharacterizations
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SystemValue

        if (processes != other.processes) return false
        return substanceCharacterizations == other.substanceCharacterizations
    }

    override fun hashCode(): Int {
        var result = processes.hashCode()
        result = 31 * result + substanceCharacterizations.hashCode()
        return result
    }
}

data class CharacterizationFactorValue(
    val output: ExchangeValue,
    val input: ExchangeValue,
) : Value
