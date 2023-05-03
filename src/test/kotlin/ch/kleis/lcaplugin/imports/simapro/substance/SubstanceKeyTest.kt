package ch.kleis.lcaplugin.imports.simapro.substance

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertNotEquals

class SubstanceKeyTest {

    @Test
    fun uid_ShouldCompactAndLowerCase_WithSubCompartment() {
        // Given + When
        val result = SubstanceKey("_nAme_", "Emission", "_coMp_", "_Sub_").uid()

        // Then
        assertEquals("_name", result)
    }

    @Test
    fun uid_ShouldCompactAndLowerCase_WithABlankType() { // Need for Simapro import
        // Given + When
        val result = SubstanceKey("_nAme_", "Emission", "_coMp_", null).uid()

        // Then
        assertEquals("_name", result)
    }

    @Test
    fun uid_ShouldCompactAndLowerCase_WithoutSubCompartment() {
        // Given + When
        val result = SubstanceKey("_nAme_", "Emission", "_coMp_", null, false)

        // Then
        assertEquals("_name", result.uid())
    }

    @Test
    fun sub_ShouldRemoveSubCompartment() {
        // Given
        val data = SubstanceKey("_nAme_", "Emission", "_coMp_", "sub")

        // When
        val result = data.withoutSub()

        // Then
        assertEquals(SubstanceKey("_nAme_", "Emission", "_coMp_", hasChanged = true), result)
    }

    @Test
    fun withoutSub_ShouldRemoveSubCompartment() {
        // Given
        val data = SubstanceKey("_nAme_", "Emission", "_coMp_", "sub")

        // When
        val result = data.withoutSub()

        // Then
        assertEquals(SubstanceKey("_nAme_", "Emission", "_coMp_", hasChanged = true), result)
    }

    @Test
    fun sub_ShouldReplaceSubCompartment() {
        // Given
        val data = SubstanceKey("_nAme_", "Emission", "_coMp_", "sub")

        // When
        val result = data.sub("another")

        // Then
        assertEquals(SubstanceKey("_nAme_", "Emission", "_coMp_", "another", true), result)
    }

    @Test
    fun removeFromName_ShouldReplaceSubCompartment() {
        // Given
        val data = SubstanceKey("_nAme_hoho_kg", "Emission", "_coMp_", "sub")

        // When
        val result = data.removeFromName("kg")

        // Then
        assertEquals(SubstanceKey("_nAme_hoho", "Emission", "_coMp_", "sub", true), result)
        assertEquals("_name_hoho", result.uid())
    }

    @Test
    fun equals_ShouldNotRelyOnNameCaseNorHasChangedField() {
        // Given
        val data1 = SubstanceKey("NAme", "Emission", "comp", "sub", hasChanged = true)
        val data2 = SubstanceKey("name", "Emission", "comp", "sub", hasChanged = false)

        // When + Then
        assertEquals(data1, data2)
        assertEquals(data1.hashCode(), data2.hashCode())
    }

    @Test
    fun equals_ShouldDependOnNameCompAndSub() {
        // Given
        val ref = SubstanceKey("Name", "Emission", "comp", "sub")
        val data1 = SubstanceKey("Nom", "Emission", "comp", "sub")
        val data2 = SubstanceKey("Name", "EmiSsion", "comp", "sub")
        val data3 = SubstanceKey("Name", "Emission", "CO", "sub")
        val data4 = SubstanceKey("Name", "Emission", "comp", "SUB")

        // When + Then
        assertNotEquals(ref, data1)
        assertNotEquals(ref, data2)
        assertNotEquals(ref, data3)
        assertNotEquals(ref, data4)
        assertNotEquals(ref.hashCode(), data1.hashCode())
        assertNotEquals(ref.hashCode(), data2.hashCode())
        assertNotEquals(ref.hashCode(), data3.hashCode())
        assertNotEquals(ref.hashCode(), data4.hashCode())
    }

    @Test
    fun name_ShouldBeSanitizedAndCompact() {
        // Given + When
        val ref = SubstanceKey("Name__40/kg", "comp", "sub")

        // Then
        assertEquals("name_40_kg", ref.name)
    }

}
