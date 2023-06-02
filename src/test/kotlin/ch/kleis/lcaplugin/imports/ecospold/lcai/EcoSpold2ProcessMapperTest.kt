package ch.kleis.lcaplugin.imports.ecospold.lcai

import ch.kleis.lcaplugin.imports.ImportException
import com.intellij.testFramework.UsefulTestCase.assertThrows
import org.junit.Test
import spold2.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EcoSpold2ProcessMapperTest {

    private val sub = DataSet()

    init {
        val activity = Activity()
        activity.id = "aId"
        activity.name = "aName"
        activity.synonyms.add("aSynonyms")
        activity.generalComment = RichText.of("ageneralComment")
        activity.energyValues = 123
        activity.includedActivitiesStart = "includedActivitiesStart"
        activity.includedActivitiesEnd = "includedActivitiesEnd"
        sub.description = ActivityDescription()
        sub.description.activity = activity
        val c = Classification()
        c.system = "System"
        c.value = "Value"
        sub.description.classifications.add(c)
        val geo = Geography()
        geo.shortName = "geography-shortname"
        geo.comment = RichText.of("comment")
        sub.description.geography = geo

        val prod = IntermediateExchange()
        prod.name = "pName"
        prod.outputGroup = 0
        val cProd = Classification()
        cProd.system = "PSystem"
        cProd.value = "PValue"
        prod.classifications.add(cProd)
        val log = LogNormal()
        log.meanValue = 1.2
        log.variance = 2.3
        log.mu = 3.4
        log.varianceWithPedigreeUncertainty = 4.5
        val uncertainty = Uncertainty()
        prod.uncertainty = uncertainty
        prod.amount = 1.0
        prod.unit = "km"

        sub.flowData = FlowData()
        sub.flowData.intermediateExchanges.add(prod)

    }


    @Test
    fun map_ShouldMapMeta() {
        // Given

        // When
        val result = EcoSpold2ProcessMapper.map(sub)

        // Then
        assertEquals("aname_geography_shortname", result.uid)
        assertEquals("aName", result.meta["name"])
        assertEquals("aSynonyms", result.meta["synonym_0"])
        assertEquals("ageneralComment", result.meta["description"])
        assertEquals("123", result.meta["energyValues"])
        assertEquals("includedActivitiesStart", result.meta["includedActivitiesStart"])
        assertEquals("includedActivitiesEnd", result.meta["includedActivitiesEnd"])
        assertEquals("Value", result.meta["System"])
        assertEquals("geography-shortname", result.meta["geography-shortname"])
        assertEquals("comment", result.meta["geography-comment"])
    }

    @Test
    fun map_ShouldMapProduct() {
        // Given

        // When
        val result = EcoSpold2ProcessMapper.map(sub)

        // Then
        assertEquals(1, result.productBlocks.size)
        assertEquals("Products", result.productBlocks[0].comment)
        assertEquals(1, result.productBlocks[0].exchanges.size)
        val p = result.productBlocks[0].exchanges[0]
        assertEquals("pname_geography_shortname", p.uid)
        assertEquals("1.0", p.qty)
        assertEquals("km", p.unit)
        assertEquals(100.0, p.allocation)
        assertEquals(listOf("pName", "PSystem = PValue"), p.comments)
    }

    @Test
    fun map_ShouldThrowAnError_WhenInvalidProduct() {
        // Given
        sub.flowData.intermediateExchanges[0].outputGroup = 1

        // When
        assertThrows(
            ImportException::class.java,
            "Invalid outputGroup for product, expected 0, found 1"
        ) { -> EcoSpold2ProcessMapper.map(sub) }
    }

    @Test
    fun map_ShouldReturn_AnEmissionWithSameName() {
        // Given

        // When
        val result = EcoSpold2ProcessMapper.map(sub)

        // Then
        assertEquals(1, result.emissionBlocks.size)
        assertEquals("Virtual Substance for Impact Factors", result.emissionBlocks[0].comment)
        assertEquals(1, result.emissionBlocks[0].exchanges.size)
        val substance = result.emissionBlocks[0].exchanges[0]
        assertEquals("aname_geography_shortname", substance.uid)
        assertEquals("1.0", substance.qty)
        assertEquals("u", substance.unit)
        assertEquals("", substance.compartment)
        assertNull(substance.subCompartment)
    }

}