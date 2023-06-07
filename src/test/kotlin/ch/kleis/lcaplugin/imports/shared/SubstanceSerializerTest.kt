package ch.kleis.lcaplugin.imports.shared

import ch.kleis.lcaplugin.imports.model.ImportedImpact
import ch.kleis.lcaplugin.imports.model.ImportedSubstance
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import kotlin.test.assertEquals

class SubstanceSerializerTest {
    @Suppress("JoinDeclarationAndAssignment")
    private val sub: ImportedSubstance

    init {
        sub = ImportedSubstance("Aluminium", "Resource", "CO2-Eq", "raw", pUid = "Aluminium_ch")
        sub.impacts.add(ImportedImpact(1000.0, "P-Eq", "alu_tox"))
        sub.meta["description"] = "Formula: Al\nAl\n"
    }

    @Test
    fun testRender_WithoutSubcompartment() {
        // Given

        // When
        val result = SubstanceSerializer.serialize(sub)

        // Then
        val expected = """

substance aluminium_ch {

    name = "Aluminium"
    type = Resource
    compartment = "raw"
    reference_unit = CO2_Eq

    meta {
        "description" = "Formula: Al
            Al"
    }

    impacts {
        1000.0 P_Eq alu_tox
    }
}"""
        assertEquals(expected, result.toString())

    }

    @Test
    fun testRender_WithSubcompartment() {
        // Given
        sub.subCompartment = "sub"

        // When
        val result = SubstanceSerializer.serialize(sub)

        // Then
        val expected = """

substance aluminium_ch {

    name = "Aluminium"
    type = Resource
    compartment = "raw"
    sub_compartment = "sub"
    reference_unit = CO2_Eq

    meta {
        "description" = "Formula: Al
            Al"
    }

    impacts {
        1000.0 P_Eq alu_tox
    }
}"""
        assertEquals(expected, result.toString())

    }

    @Test
    fun testRender_WithoutComment() {
        // Given
        sub.impacts.clear()
        sub.impacts.add(ImportedImpact(1000.0, "P-Eq", "alu_tox", "Comment"))

        // When
        val result = SubstanceSerializer.serialize(sub)

        // Then
        val expected = """
    impacts {
        // Comment
        1000.0 P_Eq alu_tox
    }
}"""
        assertThat(result.toString(), containsString(expected))

    }

}