package ch.kleis.lcaplugin.compute.model

class Method(
    val name: String
) {
    private val characterizationFactors = ArrayList<CharacterizationFactor>()

    fun add(factor: CharacterizationFactor) {
        this.characterizationFactors.add(factor)
    }

    operator fun get(index: Int): CharacterizationFactor {
        return this.characterizationFactors[index]
    }

    fun getIndicators(): List<Indicator<*>> {
        return this.characterizationFactors.stream()
            .map { it.input.flow }
            .distinct()
            .toList()
    }

    fun getCharacterizationFactors(): List<CharacterizationFactor> {
        return this.characterizationFactors
    }
}
