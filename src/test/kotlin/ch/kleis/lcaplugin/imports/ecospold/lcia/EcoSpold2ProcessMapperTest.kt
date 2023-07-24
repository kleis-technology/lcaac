package ch.kleis.lcaplugin.imports.ecospold.lcia

import ch.kleis.lcaplugin.imports.ecospold.EcoSpold2ProcessMapper
import ch.kleis.lcaplugin.imports.ecospold.model.ActivityDataset
import ch.kleis.lcaplugin.imports.util.ImportException
import com.intellij.testFramework.UsefulTestCase.assertThrows
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EcoSpold2ProcessMapperTest {

    private val sub: ActivityDataset = EcoSpold2Fixture.buildData()

    @Test
    fun map_ShouldMapMeta() {
        // Given

        // When
        val result = EcoSpold2ProcessMapper(sub).map()

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
    fun map_ShouldMapProduct() {
        // Given

        // When
        val result = EcoSpold2ProcessMapper(sub).map()

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
        ) { EcoSpold2ProcessMapper(falseSub).map() }
    }

    @Test
    fun map_ShouldReturn_AnEmissionWithSameName() {
        // Given

        // When
        val result = EcoSpold2ProcessMapper(sub).map()

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