package ch.kleis.lcaplugin.imports.ecospold.lcia

import ch.kleis.lcaplugin.imports.ecospold.EcoSpoldProcessMapper
import ch.kleis.lcaplugin.imports.ecospold.model.ActivityDataset
import ch.kleis.lcaplugin.imports.util.ImportException
import com.intellij.testFramework.UsefulTestCase.assertThrows
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EcoSpoldProcessMapperTest {

    private val sub: ActivityDataset = EcoSpold2Fixture.buildData()

    @Test
    fun map_ShouldMapMeta() {
        // Given

        // When
        val result = EcoSpoldProcessMapper(sub).map()

        // Then
        assertEquals("aname_ch", result.uid)
        assertEquals("aName", result.meta["name"])
        assertEquals("ageneralComment", result.meta["description"])
        assertEquals("123", result.meta["energyValues"])
        assertEquals("includedActivitiesStart", result.meta["includedActivitiesStart"])
        assertEquals("includedActivitiesEnd", result.meta["includedActivitiesEnd"])
        assertEquals("Value", result.meta["System"])
        assertEquals("ch", result.meta["geography-shortname"])
        assertEquals("comment", result.meta["geography-comment"])
    }

    // When closing #261, this test is going to fail - change the number of blocks to 1 then.
    @Test
    fun map_shouldMapEmissions() {
        // given
        // when
        val result = EcoSpoldProcessMapper(sub).map()

        // then
        assertEquals(2, result.emissionBlocks.size)
        assertEquals(1, result.emissionBlocks[0].exchanges.size)
        val e = result.emissionBlocks[0].exchanges[0]
        assertEquals("1.8326477008541038E-8", e.qty)
        assertEquals("_1_2_dichlorobenzene", e.uid)
        assertEquals("kg", e.unit)
        assertEquals("air", e.compartment)
        assertEquals("urban air close to ground", e.subCompartment)
        assertEquals(listOf(), e.comments)
    }

    @Test
    fun map_shouldMapLandUse() {
        // given
        // when
        val result = EcoSpoldProcessMapper(sub).map()

        // then
        assertEquals(1, result.landUseBlocks.size)
        assertEquals(1, result.landUseBlocks[0].exchanges.size)
        val lu = result.landUseBlocks[0].exchanges[0]
        assertEquals("0.04997982922431679", lu.qty)
        assertEquals("occupation_annual_crop_irrigated", lu.uid)
        assertEquals("m2*year", lu.unit)
        assertEquals("natural resource", lu.compartment)
        assertEquals("land", lu.subCompartment)
        assertEquals(listOf(), lu.comments)
    }

    @Test
    fun map_shouldMapResource() {
        // given
        // when
        val result = EcoSpoldProcessMapper(sub).map()

        // then
        assertEquals(1, result.resourceBlocks.size)
        assertEquals(1, result.resourceBlocks[0].exchanges.size)
        val res = result.resourceBlocks[0].exchanges[0]
        assertEquals("0.004413253823373581", res.qty)
        assertEquals("nitrogen", res.uid)
        assertEquals("kg", res.unit)
        assertEquals("natural resource", res.compartment)
        assertEquals("land", res.subCompartment)
        assertEquals(listOf(), res.comments)
    }

    @Test
    fun map_ShouldMapProduct() {
        // Given

        // When
        val result = EcoSpoldProcessMapper(sub).map()

        // Then
        assertEquals(1, result.productBlocks.size)
        assertEquals("Products", result.productBlocks[0].comment)
        assertEquals(1, result.productBlocks[0].exchanges.size)
        val p = result.productBlocks[0].exchanges[0]
        assertEquals("pname_ch", p.uid)
        assertEquals("1.0", p.qty)
        assertEquals("km", p.unit)
        assertEquals(100.0, p.allocation)
        assertEquals(
            listOf(
                "pName",
                "PSystem = PValue",
                "// uncertainty: logNormal mean=1.2, variance=2.3, mu=3.4",
                "synonym_0 = p1"
            ), p.comments
        )

    }

    @Test
    fun map_ShouldThrowAnError_WhenInvalidProduct() {
        // Given
        val falseSub = EcoSpold2Fixture.buildData(1)

        // When
        assertThrows(
            ImportException::class.java,
            "Invalid outputGroup for product, expected 0, found 1"
        ) { EcoSpoldProcessMapper(falseSub).map() }
    }

    @Test
    fun map_ShouldReturn_AnEmissionWithSameName() {
        // Given

        // When
        val result = EcoSpoldProcessMapper(sub).map()

        // Then
        assertEquals(2, result.emissionBlocks.size)
        assertEquals("Virtual Substance for Impact Factors", result.emissionBlocks[1].comment)
        assertEquals(1, result.emissionBlocks[1].exchanges.size)
        val substance = result.emissionBlocks[1].exchanges[0]
        assertEquals("aname_ch", substance.uid)
        assertEquals("1.0", substance.qty)
        assertEquals("u", substance.unit)
        assertEquals("", substance.compartment)
        assertNull(substance.subCompartment)
    }

}