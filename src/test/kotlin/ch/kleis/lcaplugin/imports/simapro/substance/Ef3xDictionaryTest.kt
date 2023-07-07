package ch.kleis.lcaplugin.imports.simapro.substance

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.imports.util.MissingLibraryFileException
import org.junit.Assert.*
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
        val expected = SubstanceKey("beta_chloronaphthalene", "Emission", "soil", "non-agricultural")

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
        assertEquals(expected, result)
        assertFalse(result.hasChanged)
    }

    @Test
    fun realKeyForSubstance_ShouldReturnExactKey_WhenSubCompartimentNotAskedButExists() {
        // Given
        val expected = SubstanceKey("beta_chloronaphthalene", "Emission", "soil")

        // When
        val result =
            sut.realKeyForSubstance("beta_chloronaphthalene", SubstanceType.EMISSION.value, "kg", "soil", null)

        // Then
        assertEquals(expected, result)
        assertFalse(result.hasChanged)
    }

    @Test
    fun realKeyForSubstance_ShouldReturnWithoutSubComp_WhenSubCompartimentDoesntExist() {
        // Given
        val expected = SubstanceKey("beta_chloronaphthalene", "Emission", "water")

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
        assertEquals(expected, result)
        assertTrue(result.hasChanged)
    }

    @Test
    fun realKeyForSubstance_ShouldReturnKey_WhenKeyDoesntExist() {
        // Given
        val expected = SubstanceKey("beta_chloronaphthalene", "Emission", "sky", "non-agricultural")

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
        assertEquals(expected, result)
        assertFalse(result.hasChanged)
    }


    @Test
    fun realKeyForSubstance_ShouldReturnKey_WithInvalidSubstanceType() {
        // Given
        val expected = SubstanceKey("beta_BAD", "BAD", "cloud", "non-agricultural")

        // When
        val result =
            sut.realKeyForSubstance("beta_BAD", "BAD", "kg", "cloud", "non-agricultural")

        // Then
        assertEquals(expected, result)
        assertFalse(result.hasChanged)
    }

    @Test
    fun realKeyForResource_ShouldReturnRelevantCompartment_ForResource_ShouldBeGround() {
        // Given
        val expected = SubstanceKey("Aluminium", "Resource", "ground")

        // When
        val result =
            sut.realKeyForSubstance("Aluminium", "Resource", "kg", "raw", "in ground")

        // Then
        assertEquals(expected, result)
        assertFalse(result.hasChanged)
    }

    @Test
    fun realKeyForResource_ShouldReturnRelevantCompartment_ForEmission_ShouldBeSoil() {
        // Given
        val expected = SubstanceKey("Aluminium", "Emission", "soil")

        // When
        val result =
            sut.realKeyForSubstance("Aluminium", "Emission", unit = "kg", "soil")

        // Then
        assertEquals(expected, result)
        assertFalse(result.hasChanged)
    }

    @Test
    fun realKeyForResource_ShouldReturnTheRenewable_WhenThereIsNoDefaultAndARenewable() {
        // Given
        val expected = SubstanceKey("dinitrogen", "Resource", "ground", RENEWABLE)

        // When
        val result =
            sut.realKeyForSubstance("dinitrogen", "Resource", "kg", "raw", "in ground")

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun realKeyForResource_ShouldReturnTheNonRenewable_WhenThereIsNoDefaultAndNoRenewable() {
        // Given
        val expected = SubstanceKey("diatomite", "Resource", "ground", NON_RENEWABLE)

        // When
        val result =
            sut.realKeyForSubstance("Diatomite", "Resource", "kg", "raw", "in ground")

        // Then
        assertEquals(expected, result)
        assertTrue(result.hasChanged)
    }

    @Test
    fun realKeyForResource_ShouldReturnTheRenewable_WithoutDefaultRenewableHasPriorityOnNonRenewable() {
        // Given
        val expected = SubstanceKey("dolomite", "Resource", "ground", RENEWABLE)

        // When
        val result =
            sut.realKeyForSubstance("dolomite", "Resource", "kg", "raw", "in ground")

        // Then
        assertEquals(expected, result)
        assertTrue(result.hasChanged)
    }

    @Test
    fun realKeyForResource_ShouldRemoveUnit_WhenNoKeyIsFound() {
        // Given
        val expected = SubstanceKey("argon_40", "Resource", "air", RENEWABLE)

        // When
        val result =
            sut.realKeyForSubstance("argon_40_kg", "Resource", "kg", "raw", "in air")

        // Then
        assertEquals(expected, result)
        assertTrue(result.hasChanged)
    }

    @Test
    fun realKeyForEmission_ShouldRemoveUnit_WhenNoKeyIsFound() {
        // Given
        val expected = SubstanceKey("argon_40", "Emission", "air")

        // When
        val result =
            sut.realKeyForSubstance("argon_40_kg", "Emission", "kg", "air", "")

        // Then
        assertEquals(expected, result)
        assertTrue(result.hasChanged)
    }


    @Test
    fun realKeyForLandUse_ReturnShorterNameAndType_WhenNameHasTypeButNoReplacement() {
        // Given
        val expected = SubstanceKey("from_forest_intensive", "Land_use", "Land transformation")

        // When
        val result = sut.realKeyForSubstance("Transformation, from forest, intensive", "Land_use", "m2", "raw", "land")

        // Then
        assertEquals(expected, result)
        assertFalse(result.hasChanged)
    }

    @Test
    fun realKeyForLandUse_ReturnShorterNameAndType_WhenNameHasTypeAndReplacement() {
        // Given
        val expected = SubstanceKey("arable_irrigated", "Land_use", "Land occupation")

        // When
        val result = sut.realKeyForSubstance("Occupation, annual crop, irrigated", "Land_use", "m2a", "raw", "land")

        // Then
        assertEquals(expected, result)
        assertTrue(result.hasChanged)
    }

    @Test
    fun realKeyForLandUse_ReplaceUnspecified_OnlyWhenNotAtTheBeginningOfShortName() {
        // Given
        val data = listOf(
            "Occupation, unspecified" to SubstanceKey("unspecified", "Land_use", "Land occupation"),
            "Occupation, unspecified, natural (non-use)" to
                    SubstanceKey("unspecified_natural", "Land_use", "Land occupation"),
            "Transformation, from arable land, unspecified use"
                    to SubstanceKey("from_arable", "Land_use", "Land transformation"),
        )
        data.forEach { (param, expected) ->
            // When
            val result = sut.realKeyForSubstance(param, "Land_use", "m2a", "raw", "land")

            // Then
            assertEquals(expected, result)
        }
    }

    @Test
    fun realKeyForLandUse_ShouldReturnKey_WithUnknownType() {
        // Given
        val expected = SubstanceKey("Plouf_annual_crop_irrigated", "Land_use", "unknown")

        // When
        val result = sut.realKeyForSubstance("Plouf, annual crop, irrigated", "Land_use", "m2a", "raw", "land")

        // Then
        assertEquals(expected, result)
        assertFalse(result.hasChanged)
    }


}