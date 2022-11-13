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
}
