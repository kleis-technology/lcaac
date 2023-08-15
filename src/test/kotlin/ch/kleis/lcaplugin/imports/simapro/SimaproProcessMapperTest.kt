package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.ide.imports.simapro.SubstanceImportMode
import ch.kleis.lcaplugin.imports.ModelWriter
import io.mockk.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.openlca.simapro.csv.Numeric
import org.openlca.simapro.csv.UncertaintyRecord
import org.openlca.simapro.csv.enums.ProcessCategory
import org.openlca.simapro.csv.enums.ProcessType
import org.openlca.simapro.csv.enums.Status
import org.openlca.simapro.csv.process.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.test.assertEquals

class SimaproProcessMapperTest {
    private val writer = mockk<ModelWriter>()

    private val pathSlot = slot<String>()
    private val bodySlot = slot<String>()
    private val indexSlot = slot<Boolean>()
    private val closeSlot = slot<Boolean>()

    private val process = initProcess()
    private val sut = SimaproProcessMapper.of(SubstanceImportMode.SIMAPRO)

    @Before
    fun before() {
        every {
            writer.write(
                capture(pathSlot),
                capture(bodySlot),
                capture(indexSlot),
                capture(closeSlot)
            )
        } returns Unit
        mockkObject(ModelWriter)
        every { ModelWriter.sanitizeAndCompact("kg") } returns "kg"
        every { ModelWriter.sanitizeAndCompact("MJ") } returns "MJ"
        every { ModelWriter.sanitizeAndCompact("m2a") } returns "m2a"
        every { ModelWriter.sanitizeAndCompact("m3") } returns "m3"
    }

    @After
    fun after() {
        unmockkAll()
    }


    @Test
    fun map_ShouldMapAllMetas_ForClassicalProcess() {
        // Given

        // When
        val actual = sut.map(process)

        // Then
        Assert.assertEquals("acai_berry_at_farm_sl_br_6343_4814", actual.uid)
        Assert.assertEquals(
            mutableMapOf(
                "description" to "\"Reference flow: The functional\n\nAllocation: No allocation\n\nSystem boundaries: Cradle-to-gate\n\n",
                "category" to "material",
                "identifier" to "LAUSMILA000241671000001",
                "date" to "2018-09-20",
                "generator" to "Kleis,\nTelephone: 0041 21 211 21 21; E-mail: lca@kleis.ch;\n",
                "collectionMethod" to "Sampling procedure: Principles\n",
                "dataTreatment" to "Data traitement",
                "verification" to "Proof reading validation: Passed internally.\nValidator: KleisAgro\nE-mail: lca@agroscope.admin.ch; Company: Agroscope; Country: CH",
                "systemDescription" to "name: Desc",
                "allocationRules" to "allocationRules",
                "processType" to "Unit process",
                "status" to "To be reviewed",
                "infrastructure" to "false",
                "record" to "data entry by: Kerny@kleis.ch",
                "platformId" to "platformId",
                "literatures" to "\n    * Methodological Guidelines for the Life Cycle Inventory of Agricultural Products.\n    * Another Methodological."

            ), actual.meta
        )
    }

    @Test
    fun map_ShouldMapProducts_ForClassicalProcess() {
        // Given

        // When
        val actual = sut.map(process)

        // Then
        assertEquals(2, actual.productBlocks.count())
        assertEquals("Products", actual.productBlocks[0].comment)
        assertEquals(1, actual.productBlocks[0].exchanges.count())
        val pi = actual.productBlocks[0].exchanges.first()
        assertEquals(
            listOf(
                "name: Acai berry, at farm (WFLDB 3.7)/BR U",
                "category: _WFLDB 3.7 (Phase 2)\\Plant products\\Perennials\\Acai berry",
                "The yield when productive is 9750 kg/ha-y .",
                "The final yield corresponds to the average yield over the entire lifetime of the tree.",
                "Formula=[9750*10/13]"
            ), pi.comments
        )
        assertEquals("7500", pi.qty)
        assertEquals("kg", pi.unit)
        assertEquals("acai_berry_at_farm_wfldb_3_7_sl_br_u", pi.uid)

        assertEquals("Avoided Products", actual.productBlocks[1].comment)
        assertEquals(0, actual.productBlocks[1].exchanges.count())

    }

    @Test
    fun map_ShouldMapInputs_ForClassicalProcess() {
        // Given

        // When
        val actual = sut.map(process)

        // Then
        assertEquals(3, actual.inputBlocks.count())
        assertEquals("inputsMatAndFuel", actual.inputBlocks[0].comment)
        assertEquals(0, actual.inputBlocks[0].exchanges.count())

        assertEquals("inputsElectricity", actual.inputBlocks[1].comment)
        assertEquals(0, actual.inputBlocks[1].exchanges.count())

        assertEquals("wasteToTreatment", actual.inputBlocks[2].comment)
        assertEquals(0, actual.inputBlocks[2].exchanges.count())
    }

    @Test
    fun map_ShouldMapEmissions_ForClassicalProcess() {
        // Given

        // When
        val actual = sut.map(process)

        // Then
        assertEquals(7, actual.emissionBlocks.count())
        fun assertEmptyWithComment(pos: Int, comment: String) {
            assertEquals(comment, actual.emissionBlocks[pos].comment)
            assertEquals(0, actual.emissionBlocks[pos].exchanges.count())
        }
        assertEmptyWithComment(0, "Emission to air")
        assertEmptyWithComment(1, "Emission to water")
        assertEmptyWithComment(2, "Emission to soil")
        assertEmptyWithComment(3, "Emission to economic")
        assertEmptyWithComment(4, "Emission to non_mat")
        assertEmptyWithComment(5, "Emission to social")
        assertEmptyWithComment(6, "Emission to Final Waste Flows")
    }

    @Test
    fun map_ShouldMapResources_ForClassicalProcess() {
        // Given

        // When
        val actual = sut.map(process)

        // Then
        assertEquals(1, actual.resourceBlocks.count())
        assertEquals("", actual.resourceBlocks[0].comment)
        assertEquals(3, actual.resourceBlocks[0].exchanges.count())
        fun assertResEquals(
            pos: Int,
            comments: List<String>,
            qty: String,
            unit: String,
            uid: String,
            comp: String,
            sub: String?
        ) {
            val resultExchange = actual.resourceBlocks[0].exchanges.elementAt(pos)
            assertEquals(comments, resultExchange.comments)
            assertEquals(qty, resultExchange.qty)
            assertEquals(unit, resultExchange.unit)
            assertEquals(uid, resultExchange.uid)
            assertEquals(comp, resultExchange.compartment)
            assertEquals(sub, resultExchange.subCompartment)
        }
        assertResEquals(
            0, listOf("(2,2,1,1,1,na)", "Formula=[145.56 * 67E6 / (1 -4E-6)]"),
            "9752559010.236041", "kg", "carbon_dioxide_in_air", "raw", "in air"
        )
        assertResEquals(
            1, listOf("(2,2,1,1,1,na)"),
            "20295.524449877732", "MJ", "energy_gross_calorific_value_in_biomass", "raw", null
        )
        assertResEquals(
            2, listOf("(2,1,1,1,1,na)"),
            "501.95555914578216", "m3", "water_well_br", "raw", "in water"
        )
    }

    @Test
    fun map_ShouldMapLanduse_ForClassicalProcess() {
        // Given

        // When
        val actual = sut.map(process)

        // Then
        assertEquals(1, actual.landUseBlocks.count())
        assertEquals("", actual.landUseBlocks[0].comment)
        assertEquals(1, actual.landUseBlocks[0].exchanges.count())
        val lu = actual.landUseBlocks[0].exchanges.first()
        assertEquals(listOf("(2,1,1,1,1,na)"), lu.comments)
        assertEquals("10000.0", lu.qty)
        assertEquals("m2a", lu.unit)
        assertEquals("occupation_permanent_crop_irrigated", lu.uid)
        assertEquals("raw", lu.compartment)
        assertEquals("land", lu.subCompartment)
    }


    private fun initProcess(): ProcessBlock {
        val sample = ProcessBlock().name("Acai berry, at farm/BR")
            .category(ProcessCategory.MATERIAL) //Category type
            .identifier("LAUSMILA000241671000001")
            .processType(ProcessType.UNIT_PROCESS)
            .status(Status.TO_BE_REVIEWED)
            .infrastructure(false)
            .date(Date.from(LocalDate.of(2018, 9, 20).atStartOfDay(ZoneId.of("UTC")).toInstant()))
            .record("data entry by: Kerny@kleis.ch\u007F")
            .generator(
                """Kleis,
Telephone: 0041 21 211 21 21; E-mail: lca@kleis.ch;
"""
            )
            .collectionMethod(
                """Sampling procedure: Principles
"""
            )
            .dataTreatment("Data traitement")
            .verification(
                """Proof reading validation: Passed internally.
Validator: KleisAgro
E-mail: lca@agroscope.admin.ch; Company: Agroscope; Country: CH"""
            )
            .comment(
                """"Reference flow: The functional

Allocation: No allocation

System boundaries: Cradle-to-gate

"""
            )
            .systemDescription(SystemDescriptionRow().name("name").comment("Desc"))
            .allocationRules("allocationRules")
        sample.literatures().add(
            LiteratureRow().name("Methodological Guidelines for the Life Cycle Inventory of Agricultural Products.")
                .comment("comment")
        )
        sample.literatures().add(
            LiteratureRow().name("Another Methodological.")
                .comment("comment 2")
        )
        sample.resources().add(
            ElementaryExchangeRow()
                .name("Carbon dioxide, in air")
                .subCompartment("in air")
                .unit("kg")
                .amount(Numeric.of("145.56 * 67E6 / (1 -4E-6)"))
                .uncertainty(UncertaintyRecord.logNormal(1.0744))
                .comment("(2,2,1,1,1,na)\n")
        )
        sample.resources().add(
            ElementaryExchangeRow()
                .name("Energy, gross calorific value, in biomass")
                .subCompartment("")
                .unit("MJ")
                .amount(Numeric.of(20295.524449877732))
                .uncertainty(UncertaintyRecord.logNormal(1.0744))
                .comment("(2,2,1,1,1,na)\n")
        )
        sample.resources().add(
            ElementaryExchangeRow()
                .name("Water, well, BR;")
                .subCompartment("in water")
                .unit("m3")
                .amount(Numeric.of(501.95555914578216))
                .uncertainty(UncertaintyRecord.logNormal(1.0744))
                .comment("(2,1,1,1,1,na)\n")
        )
        sample.resources().add(
            ElementaryExchangeRow()
                .name("Occupation, permanent crop, irrigated")
                .subCompartment("land")
                .unit("m2a")
                .amount(Numeric.of(10000.0))
                .uncertainty(UncertaintyRecord.logNormal(1.1130))
                .comment("(2,1,1,1,1,na)\u007F")
        )

        sample.products().add(
            ProductOutputRow().name("Acai berry, at farm (WFLDB 3.7)/BR U")
                .unit("kg")
                .amount(Numeric.of("9750*10/13"))
                .allocation(Numeric.of("100"))
                .wasteType("not defined")
                .category("_WFLDB 3.7 (Phase 2)\\Plant products\\Perennials\\Acai berry")
                .comment(
                    """The yield when productive is 9750 kg/ha-y .
The final yield corresponds to the average yield over the entire lifetime of the tree."""
                )
        )
        sample.platformId("platformId")
        return sample
    }

}