package ch.kleis.lcaplugin.imports.ecospold.lcia

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EcoSpold2SubstanceMapperTest {

    @Test
    fun map_ShouldMapSubstance_WithTheRightMethod() {
        // Given
        val sub = EcoSpold2Fixture.buildData()

        // When
        val result = EcoSpold2SubstanceMapper.map(sub, "EF v3.1")

        // Then
        assertEquals("aName", result.name)
        assertEquals("aname_ch", result.uid)
        assertEquals("Emission", result.type)
        assertEquals("u", result.referenceUnit)
        assertEquals("", result.compartment)
        assertNull(result.subCompartment)
        assertEquals(2, result.impacts.size)
        val climateChange = result.impacts
            .firstOrNull() { it.name == "climate change" }
        assertEquals("climate change", climateChange?.name)
        assertEquals("global warming potential (GWP100)", climateChange?.comment)
        assertEquals(0.6, climateChange?.value ?: 0.0, 1E-12)
        assertEquals("kg_CO2_Eq", climateChange?.unitSymbol)
    }

}