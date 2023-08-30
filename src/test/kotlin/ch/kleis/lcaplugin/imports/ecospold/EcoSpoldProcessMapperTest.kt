package ch.kleis.lcaplugin.imports.ecospold

import ch.kleis.lcaplugin.imports.ecospold.model.ActivityDataset
import ch.kleis.lcaplugin.imports.model.ImportedImpactExchange
import ch.kleis.lcaplugin.imports.util.ImportException
import com.intellij.testFramework.UsefulTestCase.assertThrows
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class EcoSpoldProcessMapperTest {

    private val sub: ActivityDataset = EcoSpold2Fixture.buildData()

    @Test
    fun map_ShouldMapMeta() {
        // Given

        // When
        val result = EcoSpoldProcessMapper.map(sub)

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

    @Test
    fun map_shouldMapEmissions() {
        // given
        // when
        val result = EcoSpoldProcessMapper.map(sub)

        // then
        assertEquals(1, result.emissionBlocks.size)
        assertEquals(1, result.emissionBlocks[0].exchanges.count())
        val e = result.emissionBlocks[0].exchanges.first()
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
        val result = EcoSpoldProcessMapper.map(sub)

        // then
        assertEquals(1, result.landUseBlocks.size)
        assertEquals(1, result.landUseBlocks[0].exchanges.count())
        val lu = result.landUseBlocks[0].exchanges.first()
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
        val result = EcoSpoldProcessMapper.map(sub)

        // then
        assertEquals(1, result.resourceBlocks.size)
        assertEquals(1, result.resourceBlocks[0].exchanges.count())
        val res = result.resourceBlocks[0].exchanges.first()
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
        val result = EcoSpoldProcessMapper.map(sub)

        // Then
        assertEquals(1, result.productBlocks.size)
        assertEquals(1, result.productBlocks[0].exchanges.count())
        val p = result.productBlocks[0].exchanges.first()
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
    fun map_ShouldMapInputs() {
        // given
        // when
        val result = EcoSpoldProcessMapper.map(sub)

        // then
        assertEquals(1, result.inputBlocks.size)
        assertEquals(2, result.inputBlocks[0].exchanges.count())
        val i = result.inputBlocks[0].exchanges.first()
        assertEquals("iname_ch", i.uid)
        assertEquals("3.0", i.qty)
        assertEquals("kg", i.unit)
        assertEquals(listOf("iName"), i.comments)
    }

    @Test
    fun map_ShouldThrowAnError_WhenInvalidInput() {
        // Given
        val falseSub = EcoSpold2Fixture.buildData(inputGroup = 4)

        // When
        val e = assertFailsWith(
            ImportException::class,
        ) { EcoSpoldProcessMapper.map(falseSub).productBlocks[0].exchanges.count() }
        assertEquals("Invalid inputGroup for intermediateExchange, expected in {1, 2, 3, 5}, found 4", e.message)
    }

    @Test
    fun map_ShouldThrowAnError_WhenInvalidProduct() {
        // Given
        val falseSub = EcoSpold2Fixture.buildData(1)
        assertEquals(1, falseSub.flowData.intermediateExchanges.first().outputGroup)

        // When
        assertThrows(
            ImportException::class.java,
            "Invalid outputGroup for product, expected 0, found 1"
        ) { EcoSpoldProcessMapper.map(falseSub).productBlocks[0].exchanges.count() }
    }

    @Test
    fun map_shouldMapImpacts() {
        // Given
        val methodName = "EF v3.1"

        // When
        val result = EcoSpoldProcessMapper.map(sub, methodName)

        // Then
        assertEquals(1, result.impactBlocks.size)
        assertEquals(2, result.impactBlocks[0].exchanges.count())

        // First impact in fixture - not included, wrong method
        assertNotEquals(
            ImportedImpactExchange(
                qty = "0.1188",
                unit = "m3_world_eq_deprived",
                uid = "deprivation",
                comments = listOf("water use"),
            ),
            result.impactBlocks[0].exchanges.first()
        )

        // Second impact in fixture - included, good method
        assertEquals(
            ImportedImpactExchange(
                qty = "0.0013",
                unit = "mol_H_p_Eq",
                uid = "accumulated_exceedance_ae",
                comments = listOf("acidification"),
            ),
            result.impactBlocks[0].exchanges.first()
        )
    }
}