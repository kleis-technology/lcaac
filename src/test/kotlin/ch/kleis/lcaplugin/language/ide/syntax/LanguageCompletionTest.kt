package ch.kleis.lcaplugin.language.ide.syntax

import com.intellij.codeInsight.completion.CompletionType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LanguageCompletionTest : LcaCompletionTestCase() {

    @Test
    fun lookup_ShouldReturnAllRootKeyword_WithoutPrefix() {
        // Given
        fixture.configureByFiles("empty_all_keywords.lca")
        fixture.complete(CompletionType.BASIC)

        // When
        val lookupElementStrings = fixture.lookupElementStrings

        // Then
        assertNotNull(lookupElementStrings)
        assertSameElements(lookupElementStrings!!, "import", "package", "process", "substance", "unit", "variables")
    }

    @Test
    fun lookup_ShouldReturnFilterRootKeyword_WithPrefix() {
        // Given
        fixture.configureByText("empty_only_filtered.lca", "p<caret>")
        fixture.complete(CompletionType.BASIC)

        // When
        val lookupElementStrings = fixture.lookupElementStrings

        // Then
        assertNotNull(lookupElementStrings)
        assertSameElements(lookupElementStrings!!, "import", "package", "process")
    }

    @Test
    fun lookup_ShouldReturnEmpty_ForName() {
        // Given
        fixture.configureByText(
            "sampleShouldReturnEmpty_ForName.lca", """
substance <caret> {
    
}
"""
        )
        fixture.complete(CompletionType.BASIC)

        // When
        val lookupElementStrings = fixture.lookupElementStrings

        // Then
        assertNotNull(lookupElementStrings)
        assertSameElements(lookupElementStrings!!)
    }

    @Test
    fun lookup_ShouldReturnSubstanceKeyWord_ForUnitBlockAndSymbol() {
        // Given
        fixture.configureByText(
            "sampleShouldReturnSubstanceKeyWord_ForSubstanceBlock.lca", """
unit myUnit {
    <caret>
}
"""
        )
        fixture.complete(CompletionType.BASIC)

        // When
        val lookupElementStrings = fixture.lookupElementStrings

        // Then
        assertNotNull(lookupElementStrings)
        assertSameElements(lookupElementStrings!!, "symbol")
    }

    @Test
    fun lookup_ShouldReturnSubstanceKeyWord_ForUnitBlockAndAliasFor() {
        // Given
        fixture.configureByText(
            "sampleShouldReturnSubstanceKeyWord_ForSubstanceBlock.lca", """
unit myUnit {
    symbol = "Symbole"
    <caret>
}
"""
        )
        fixture.complete(CompletionType.BASIC)

        // When
        val lookupElementStrings = fixture.lookupElementStrings

        // Then
        assertNotNull(lookupElementStrings)
        assertSameElements(lookupElementStrings!!, "dimension", "alias_for")
    }

    @Test
    fun lookup_ShouldReturnSubstanceKeyWord_ForSubstanceBlock() {
        // Given
        fixture.configureByText(
            "sampleShouldReturnSubstanceKeyWord_ForSubstanceBlock.lca", """
substance mySubstance {
    <caret>
    meta {
    }

    impacts {
    }
}
"""
        )
        fixture.complete(CompletionType.BASIC)

        // When
        val lookupElementStrings = fixture.lookupElementStrings

        // Then
        assertNotNull(lookupElementStrings)
        assertSameElements(lookupElementStrings!!, "name")
    }

    @Test
    fun lookup_ShouldReturnEmpty_ForSubstanceBlockAndStringLiteral() {
        // Given
        fixture.configureByText(
            "sampleShouldReturnSubstanceKeyWord_ForSubstanceBlock.lca", """
substance mySubstance {
    name = "<caret>"
    meta {
    }

    impacts {
    }
}
"""
        )
        fixture.complete(CompletionType.BASIC)

        // When
        val lookupElementStrings = fixture.lookupElementStrings

        // Then
        assertNotNull(lookupElementStrings)
        assertSameElements(lookupElementStrings!!)
    }

    @Test
    fun lookup_ShouldReturnSubstanceKeyWord_ForSubstanceBlockAnd2ndKeyword() {
        // Given
        fixture.configureByText(
            "sampleShouldReturnEmpty_ForSubstanceMetaBlock.lca", """
substance mySubstance {
    name = "myName"
    <caret>
    meta {
    }

    impacts {
    }
}
"""
        )
        fixture.complete(CompletionType.BASIC)

        // When
        val lookupElementStrings = fixture.lookupElementStrings

        // Then
        assertNotNull(lookupElementStrings)
        assertSameElements(lookupElementStrings!!, "type")
    }

    @Test
    fun lookup_ShouldReturnMetaAndSubstance_ForSubstanceMetaBlock() {
        // Given
        fixture.configureByText(
            "sampleShouldReturnEmpty_ForSubstanceMetaBlock.lca", """
substance mySubstance {
    name = "mySubstanceName"
    type = Emission
    compartment = "water"
    sub_compartment = "sea water"
    reference_unit = kg
    <caret>
}
"""
        )
        fixture.complete(CompletionType.BASIC)

        // When
        val lookupElementStrings = fixture.lookupElementStrings

        // Then
        assertNotNull(lookupElementStrings)
        assertSameElements(lookupElementStrings!!, "meta", "impacts")
    }

    fun lookup_ShouldReturnEmpty_ForSubstanceMetaBlock() {
        // Given
        fixture.configureByText(
            "sampleShouldReturnEmpty_ForSubstanceMetaBlock.lca", """
substance mySubstance {
    name = "mySubstanceName"
    type = Emission
    compartment = "water"
    sub_compartment = "sea water"
    reference_unit = kg
    <caret>
}
"""
        )
        fixture.complete(CompletionType.BASIC)

        // When
        val lookupElementStrings = fixture.lookupElementStrings

        // Then
        assertNotNull(lookupElementStrings)
        assertSameElements(lookupElementStrings!!, "meta", "impacts")
    }

    @Test
    fun lookup_ShouldReturnSubstanceType_ForSubstanceType() {
        // Given
        fixture.configureByText(
            "sampleShouldReturnEmpty_ForSubstanceMetaBlock.lca", """
substance mySubstance {
    name = "myname"
    type =<caret>
    compartment = "water"
}
"""
        )
        fixture.complete(CompletionType.BASIC)

        // When
        val lookupElementStrings = fixture.lookupElementStrings

        // Then
        assertNotNull(lookupElementStrings)
        assertSameElements(lookupElementStrings!!, "Emission", "Resource", "Land_use")
    }

    @Test
    fun lookup_ShouldReturnSubstanceSubCompartment_ForSubstance() {
        // Given
        fixture.configureByText(
            "sampleShouldReturnEmpty_ForSubstanceMetaBlock.lca", """
substance mySubstance {
    name = "myname"
    type = Emission
    compartment = "water"
    <caret>
}
"""
        )
        fixture.complete(CompletionType.BASIC)

        // When
        val lookupElementStrings = fixture.lookupElementStrings

        // Then
        assertNotNull(lookupElementStrings)
        assertSameElements(lookupElementStrings!!, "reference_unit", "sub_compartment")
    }

    @Test
    fun lookup_ShouldReturnEmpty_ForSubstanceImpactBlock() {
        // Given
        fixture.configureByText(
            "sampleShouldReturnEmpty_ForSubstanceImpactBlock.lca", """
substance name {

    impacts {
       <caret>
    }
}
"""
        )
        fixture.complete(CompletionType.BASIC)

        // When
        val lookupElementStrings = fixture.lookupElementStrings

        // Then
        assertNotNull(lookupElementStrings)
        assertSameElements(lookupElementStrings!!)
    }

    @Test
    fun lookup_ShouldReturnProcess_ForProcess() {
        // Given
        fixture.configureByText(
            "sampleShouldReturnEmpty_ForSubstanceImpactBlock.lca", """
process p3 {

       <caret>
    
}
"""
        )
        fixture.complete(CompletionType.BASIC)

        // When
        val lookupElementStrings = fixture.lookupElementStrings

        // Then
        assertNotNull(lookupElementStrings)
        assertSameElements(
            lookupElementStrings!!,
            "emissions", "impacts", "inputs", "labels", "land_use", "meta", "params",
            "products", "resources", "variables",

            )
    }


}