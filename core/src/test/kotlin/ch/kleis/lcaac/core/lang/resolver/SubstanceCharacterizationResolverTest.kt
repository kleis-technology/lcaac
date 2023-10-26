package ch.kleis.lcaac.core.lang.resolver

import ch.kleis.lcaac.core.lang.register.SubstanceCharacterizationRegister
import ch.kleis.lcaac.core.lang.register.SubstanceKey
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.expression.SubstanceType
import ch.kleis.lcaac.core.lang.fixture.SubstanceCharacterizationFixture
import ch.kleis.lcaac.core.lang.fixture.SubstanceFixture
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
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
        val substanceCharacterizations =
            SubstanceCharacterizationRegister(mapOf("propanol" to substanceCharacterization).mapKeys { SubstanceKey(it.key) }
            )

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
        val substanceCharacterizations =
            SubstanceCharacterizationRegister(mapOf("propanol" to substanceCharacterization).mapKeys { SubstanceKey(it.key) }
            )

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
        val substanceCharacterizations =
            SubstanceCharacterizationRegister(
                mapOf(
                    SubstanceKey("propanol", SubstanceType.RESOURCE, "air") to substanceCharacterization
                )
            )

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

        val substanceCharacterizations = SubstanceCharacterizationRegister(
            mapOf(
                SubstanceKey("propanol", SubstanceType.RESOURCE, "air") to propanolAirCharacterization,
                SubstanceKey("propanol", SubstanceType.RESOURCE, "water") to propanolWaterCharacterization,
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

        val substanceCharacterizations = SubstanceCharacterizationRegister(
            mapOf(
                SubstanceKey("propanol", SubstanceType.RESOURCE, "air") to propanolAirCharacterization,
                SubstanceKey("propanol", SubstanceType.RESOURCE, "air", "airspace E") to propanolAirECharacterization
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

        val substanceCharacterizations = SubstanceCharacterizationRegister(
            mapOf(
                SubstanceKey("propanol", SubstanceType.RESOURCE, "air", "airspace G") to propanolAirSpaceGCharacterization,
                SubstanceKey("propanol", SubstanceType.RESOURCE, "air", "airspace E") to propanolAirSpaceECharacterization,
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
