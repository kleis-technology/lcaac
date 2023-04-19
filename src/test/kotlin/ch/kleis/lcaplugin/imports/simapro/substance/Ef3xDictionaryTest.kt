package ch.kleis.lcaplugin.imports.simapro.substance

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.imports.MissingLibraryFileException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import kotlin.test.assertFailsWith

class Ef3xDictionaryTest {

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
        val sut = Ef3xDictionary.fromClassPath("", "import/META-INF/dictionary.csv")
        val excepted = SubstanceKey("beta_chloronaphthalene", SubstanceType.EMISSION.value, "soil", "non-agricultural")

        // When
        val result =
            sut.realKeyForSubstance("beta_chloronaphthalene", SubstanceType.EMISSION.value, "soil", "non-agricultural")

        // Then
        assertEquals(excepted, result)
    }

    @Test
    fun realKeyForSubstance_ShouldReturnExactKey_WhenSubCompartimentNotAskedButExists() {
        // Given
        val sut = Ef3xDictionary.fromClassPath("", "import/META-INF/dictionary.csv")
        val excepted = SubstanceKey("beta_chloronaphthalene", SubstanceType.EMISSION.value, "soil", null)

        // When
        val result =
            sut.realKeyForSubstance("beta_chloronaphthalene", SubstanceType.EMISSION.value, "soil", null)

        // Then
        assertEquals(excepted, result)
    }

    @Test
    fun realKeyForSubstance_ShouldReturnWithoutSubComp_WhenSubCompartimentDoesntExist() {
        // Given
        val sut = Ef3xDictionary.fromClassPath("", "import/META-INF/dictionary.csv")
        val excepted = SubstanceKey("beta_chloronaphthalene", SubstanceType.EMISSION.value, "water", null)

        // When
        val result =
            sut.realKeyForSubstance("beta_chloronaphthalene", SubstanceType.EMISSION.value, "water", "non-agricultural")

        // Then
        assertEquals(excepted, result)
    }

    @Test
    fun realKeyForSubstance_ShouldReturnKey_WhenKeyDoesntExist() {
        // Given
        val sut = Ef3xDictionary.fromClassPath("", "import/META-INF/dictionary.csv")
        val excepted = SubstanceKey("beta_chloronaphthalene", SubstanceType.EMISSION.value, "sky", "non-agricultural")

        // When
        val result =
            sut.realKeyForSubstance("beta_chloronaphthalene", SubstanceType.EMISSION.value, "sky", "non-agricultural")

        // Then
        assertEquals(excepted, result)
    }

    @Test
    fun realKeyForSubstance_ShouldReturnKey_WhitInvalidSubstanceType() {
        // Given
        val sut = Ef3xDictionary.fromClassPath("", "import/META-INF/dictionary.csv")
        val excepted = SubstanceKey("beta_chloronaphthalene", "BAD", "water", "non-agricultural")

        // When
        val result =
            sut.realKeyForSubstance("beta_chloronaphthalene", "BAD", "water", "non-agricultural")

        // Then
        assertEquals(excepted, result)
    }
}