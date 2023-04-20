package ch.kleis.lcaplugin.imports.simapro.substance

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.imports.MissingLibraryFileException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import kotlin.test.assertFailsWith

/** The EcoInvent process EI3ARUNI000011519604128 is a very good candidate to find cases */
class Ef3xDictionaryTest {
    companion object {
        val sut: Dictionary = Ef3xDictionary.fromClassPath("", "import/META-INF/dictionary.csv")
    }

    @Test
    fun fromClassPath_ShouldFoundFile_WithValidFilter() {
        // Given

        // When
        val result = Ef3xDictionary.fromClassPath("", "import/META-INF/dictionary.csv")

        // Then
        assertNotNull(result)
    }

    @Test
    fun fromClassPath_ShouldThrowAnError_WithInvalidName() {
        // Given

        // Then
        assertFailsWith<MissingLibraryFileException> {
            // When
            Ef3xDictionary.fromClassPath("", "BAD/dictionary.csv")
        }
    }

    @Test
    fun fromClassPath_ShouldThrowAnError_WithInvalidPath() {
        // Given

        // Then
        assertFailsWith<MissingLibraryFileException> {
            // When
            Ef3xDictionary.fromClassPath("bad_jar", "import/META-INF/dictionary.csv")
        }
    }

    @Test
    fun realKeyForSubstance_ShouldReturnExactKey_WhenSubCompartimentExist() {
        // Given
        val excepted = SubstanceKey("beta_chloronaphthalene", "Emission", "soil", "non-agricultural")

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
    fun realKeyForSubstance_ShouldReturnExactKey_WhenSubCompartimentNotAskedButExists() {
        // Given
        val excepted = SubstanceKey("beta_chloronaphthalene", "Emission", "soil", null)

        // When
        val result =
            sut.realKeyForSubstance("beta_chloronaphthalene", SubstanceType.EMISSION.value, "kg", "soil", null)

        // Then
        assertEquals(excepted, result)
    }

    @Test
    fun realKeyForSubstance_ShouldReturnWithoutSubComp_WhenSubCompartimentDoesntExist() {
        // Given
        val excepted = SubstanceKey("beta_chloronaphthalene", "Emission", "water", null)

        // When
        val result =
            sut.realKeyForSubstance(
                "beta_chloronaphthalene",
                SubstanceType.EMISSION.value,
                "kg",
                "water",
                "non-agricultural"
            )

        // Then
        assertEquals(excepted, result)
    }

    @Test
    fun realKeyForSubstance_ShouldReturnKey_WhenKeyDoesntExist() {
        // Given
        val excepted = SubstanceKey("beta_chloronaphthalene", "Emission", "sky", "non-agricultural")

        // When
        val result =
            sut.realKeyForSubstance(
                "beta_chloronaphthalene",
                SubstanceType.EMISSION.value,
                "kg",
                "sky",
                "non-agricultural"
            )

        // Then
        assertEquals(excepted, result)
    }


    @Test
    fun realKeyForSubstance_ShouldReturnKey_WhitInvalidSubstanceType() {
        // Given
        val excepted = SubstanceKey("beta_BAD", "BAD", "cloud", "non-agricultural")

        // When
        val result =
            sut.realKeyForSubstance("beta_BAD", "BAD", "kg", "cloud", "non-agricultural")

        // Then
        assertEquals(excepted, result)
    }

    @Test
    fun realKeyForResource_ShouldReturnRelevantCompartment_ForResource_ShouldBeGround() {
        // Given
        val excepted = SubstanceKey("Aluminium", "Resource", "ground", null)

        // When
        val result =
            sut.realKeyForSubstance("Aluminium", "Resource", "kg", "raw", "in ground")

        // Then
        assertEquals(excepted, result)
    }

    @Test
    fun realKeyForResource_ShouldReturnRelevantCompartment_ForEmission_ShouldBeSoil() {
        // Given
        val excepted = SubstanceKey("Aluminium", "Emission", "soil", null)

        // When
        val result =
            sut.realKeyForSubstance("Aluminium", "Emission", unit = "kg", "soil")

        // Then
        assertEquals(excepted, result)
    }

    @Test
    fun realKeyForResource_ShouldReturnTheRenewable_WhenThereIsNoDefaultAndARenewable() {
        // Given
        val excepted = SubstanceKey("dinitrogen", "Resource", "ground", RENEWABLE)

        // When
        val result =
            sut.realKeyForSubstance("dinitrogen", "Resource", "kg", "raw", "in ground")

        // Then
        assertEquals(excepted, result)
    }

    @Test
    fun realKeyForResource_ShouldReturnTheNonRenewable_WhenThereIsNoDefaultAndNoRenewable() {
        // Given
        val excepted = SubstanceKey("diatomite", "Resource", "ground", NON_RENEWABLE)

        // When
        val result =
            sut.realKeyForSubstance("Diatomite", "Resource", "kg", "raw", "in ground")

        // Then
        assertEquals(excepted, result)
    }

    @Test
    fun realKeyForResource_ShouldReturnTheRenewable_WithoutDefaultRenewableHasPriorityOnNonRenewable() {
        // Given
        val excepted = SubstanceKey("dolomite", "Resource", "ground", RENEWABLE)

        // When
        val result =
            sut.realKeyForSubstance("dolomite", "Resource", "kg", "raw", "in ground")

        // Then
        assertEquals(excepted, result)
    }

    @Test
    fun realKeyForResource_ShouldRemoveUnit_WhenNoKeyIsFound() {
        // Given
        val excepted = SubstanceKey("argon_40", "Resource", "air", RENEWABLE)

        // When
        val result =
            sut.realKeyForSubstance("argon_40_kg", "Resource", "kg", "raw", "in air")

        // Then
        assertEquals(excepted, result)
    }

    @Test
    fun realKeyForEmission_ShouldRemoveUnit_WhenNoKeyIsFound() {
        // Given
        val excepted = SubstanceKey("argon_40", "Emission", "air")

        // When
        val result =
            sut.realKeyForSubstance("argon_40_kg", "Emission", "kg", "air", "")

        // Then
        assertEquals(excepted, result)
    }


}