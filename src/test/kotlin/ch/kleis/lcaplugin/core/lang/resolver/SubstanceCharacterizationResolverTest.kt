package ch.kleis.lcaplugin.core.lang.resolver

import ch.kleis.lcaplugin.core.lang.Register
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.expression.ESubstanceCharacterization
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceCharacterizationFixture
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceFixture
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertNull


class SubstanceCharacterizationResolverTest {
    @Test
    fun resolve_whenEmptyTable_thenNull() {
        // given
        val symbolTable = SymbolTable.empty<BasicNumber>()
        val resolver = SubstanceCharacterizationResolver(symbolTable)
        val substance = SubstanceFixture.propanol

        // when
        val actual = resolver.resolve(substance)

        // then
        assertNull(actual)
    }

    @Test
    fun resolve_whenNoCompartment_thenNull() {
        // given
        val substance = SubstanceFixture.propanol.copy(compartment = null)
        val substanceCharacterization = SubstanceCharacterizationFixture.propanolCharacterization
        val substanceCharacterizations = Register.from(mapOf("propanol" to substanceCharacterization))

        val symbolTable = SymbolTable(
            substanceCharacterizations = substanceCharacterizations,
        )
        val resolver = SubstanceCharacterizationResolver(symbolTable)

        // when
        val actual = resolver.resolve(substance)

        // then
        assertNull(actual)
    }

    @Test
    fun resolve_whenNoType_thenNull() {
        // given
        val substance = SubstanceFixture.propanol.copy(type = null)
        val substanceCharacterization = SubstanceCharacterizationFixture.propanolCharacterization
        val substanceCharacterizations = Register.from(mapOf("propanol" to substanceCharacterization))

        val symbolTable = SymbolTable(
            substanceCharacterizations = substanceCharacterizations,
        )
        val resolver = SubstanceCharacterizationResolver(symbolTable)

        // when
        val actual = resolver.resolve(substance)

        // then
        assertNull(actual)
    }

    @Test
    fun resolve_whenSingleExactMatch_shouldFindMatch() {
        // given
        val substance = SubstanceFixture.propanol
        val substanceCharacterization = SubstanceCharacterizationFixture.propanolCharacterization
        val substanceCharacterizations = Register.from(mapOf("propanol" to substanceCharacterization))

        val symbolTable = SymbolTable(
            substanceCharacterizations = substanceCharacterizations,
        )
        val resolver = SubstanceCharacterizationResolver(symbolTable)

        // when
        val actual = resolver.resolve(substance)

        // then
        assertEquals(substanceCharacterization, actual)
    }

    @Test
    fun resolve_whenTwoDifferentCompartments() {
        // given
        val propanolAir = SubstanceFixture.propanol.copy(compartment = "air")
        val propanolAirCharacterization = SubstanceCharacterizationFixture.substanceCharacterizationFor(propanolAir)

        val propanolWater = SubstanceFixture.propanol.copy(compartment = "water")
        val propanolWaterCharacterization = SubstanceCharacterizationFixture.substanceCharacterizationFor(propanolWater)

        val substanceCharacterizations: Register<ESubstanceCharacterization<BasicNumber>> = Register.from(
            mapOf(
                "a" to propanolAirCharacterization,
                "b" to propanolWaterCharacterization,
            )
        )

        val symbolTable = SymbolTable(
            substanceCharacterizations = substanceCharacterizations,
        )
        val resolver = SubstanceCharacterizationResolver(symbolTable)

        // when
        val actual = resolver.resolve(propanolAir)

        // then
        assertEquals(propanolAirCharacterization, actual)
    }

    @Test
    fun resolve_whenNoSubCompartment_shouldResolveDefault() {
        // Given
        val propanolAir = SubstanceFixture.propanol.copy(compartment = "air")
        val propanolAirCharacterization = SubstanceCharacterizationFixture.substanceCharacterizationFor(propanolAir)

        val propanolAirE = SubstanceFixture.propanol.copy(compartment = "air", subCompartment = "airspace E")
        val propanolAirECharacterization = SubstanceCharacterizationFixture.substanceCharacterizationFor(propanolAirE)

        val query = propanolAir.copy(subCompartment = "airspace G")

        val substanceCharacterizations: Register<ESubstanceCharacterization<BasicNumber>> = Register.from(
            mapOf(
                "G" to propanolAirCharacterization,
                "E" to propanolAirECharacterization
            )
        )

        val symbolTable = SymbolTable(
            substanceCharacterizations = substanceCharacterizations,
        )
        val resolver = SubstanceCharacterizationResolver(symbolTable)

        // when
        val actual = resolver.resolve(query)

        // then
        assertEquals(propanolAirCharacterization, actual)
    }

    @Test
    fun resolve_whenTwoDifferentSubCompartments() {
        // given
        val propanolAirSpaceG = SubstanceFixture.propanol.copy(compartment = "air", subCompartment = "airspace G")
        val propanolAirSpaceGCharacterization =
            SubstanceCharacterizationFixture.substanceCharacterizationFor(propanolAirSpaceG)

        val propanolAirSpaceE = SubstanceFixture.propanol.copy(compartment = "air", subCompartment = "airspace E")
        val propanolAirSpaceECharacterization =
            SubstanceCharacterizationFixture.substanceCharacterizationFor(propanolAirSpaceE)

        val substanceCharacterizations: Register<ESubstanceCharacterization<BasicNumber>> = Register.from(
            mapOf(
                "a" to propanolAirSpaceGCharacterization,
                "b" to propanolAirSpaceECharacterization,
            )
        )

        val symbolTable = SymbolTable(
            substanceCharacterizations = substanceCharacterizations,
        )
        val resolver = SubstanceCharacterizationResolver(symbolTable)

        // when
        val actual = resolver.resolve(propanolAirSpaceG)

        // then
        assertEquals(propanolAirSpaceGCharacterization, actual)
    }
}
