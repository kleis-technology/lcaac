package ch.kleis.lcaplugin.imports.shared

import ch.kleis.lcaplugin.imports.model.ImpactImported
import ch.kleis.lcaplugin.imports.model.SubstanceImported
import org.junit.Test
import kotlin.test.assertEquals

class SubstanceSerializerTest {
    @Suppress("JoinDeclarationAndAssignment")
    private val sub: SubstanceImported

    init {
        sub = SubstanceImported("Aluminium", "Resource", "kg", "raw")
        sub.impacts.add(ImpactImported(1000.0, "g", "alu_tox"))
        sub.meta["description"] = "Formula: Al\nAl\n"
    }

    @Test
    fun testRender_WithoutSubcompartment() {
        // Given

        // When
        val result = SubstanceSerializer.serialize(sub)

        // Then
        val expected = """

substance aluminium {

    name = "Aluminium"
    type = Resource
    compartment = "raw"
    reference_unit = kg

    impacts {
        1000.0 g alu_tox
    }

    meta {
        "description" = "Formula: Al
            Al"
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

substance aluminium {

    name = "Aluminium"
    type = Resource
    compartment = "raw"
    sub_compartment = "sub"
    reference_unit = kg

    impacts {
        1000.0 g alu_tox
    }

    meta {
        "description" = "Formula: Al
            Al"
    }
}"""
        assertEquals(expected, result.toString())

    }

}