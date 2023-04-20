package ch.kleis.lcaplugin.imports.simapro.substance

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import org.junit.Assert.assertEquals
import org.junit.Test

class SimaproDictionaryTest {
    private val sut = SimaproDictionary()

    @Test
    fun realKeyForSubstance_ShouldReturnTruncatedKey_WithSubCompartiment() {
        // Simapro substance dont have subCompartment
        // Given
        val excepted = SubstanceKey("beta_chloronaphthalene", "soil")

        // When
        val result =
            sut.realKeyForSubstance(
                "beta_chloronaphthalene",
                SubstanceType.EMISSION.value,
                "kg",
                "soil",
                "non-agricultural"
            )

        // Then
        assertEquals(excepted, result)
    }

    @Test
    fun realKeyForSubstance_ShouldReturnExactKey_WithoutCompartment() {
        // Given
        val excepted = SubstanceKey("beta_chloronaphthalene", "soil", null)

        // When
        val result =
            sut.realKeyForSubstance("beta_chloronaphthalene", SubstanceType.EMISSION.value, "kg", "soil", null)

        // Then
        assertEquals(excepted, result)
    }
}