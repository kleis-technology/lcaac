package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.imports.ModelWriter
import io.mockk.*
import org.junit.After
import org.junit.Assert.assertEquals
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


class ProcessRendererTest {
    private val defaultZoneId = ZoneId.systemDefault()
    private val writer = mockk<ModelWriter>()

    private val pathSlot = slot<String>()
    private val bodySlot = slot<String>()
    private val indexSlot = slot<Boolean>()
    private val sut = ProcessRenderer()

    @Before
    fun before() {
        every { writer.write(capture(pathSlot), capture(bodySlot), capture(indexSlot)) } returns Unit
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
    fun test_render_shouldRender_forClassicalProcess() {
        // Given
        val sample = initProcess()

        // When
        sut.render(sample, writer)

        // Then
        val expected = """


process acai_berry_at_farm_br8553_1886 {

    meta {
        description = "'Reference flow: The functional unit is the production of 1 kg of Acai berry at the farm, (total  amount of water 1862 m3/ha).
            Allocation: No allocation
            System boundaries: Cradle-to-gate. The inventory includes the processes of tree seedling production and planting, fertilisation (mineral and manure).
            Geography: Brazil
            Technology: Conventional production.
            Time: 2000-2018
            Data quality rating (DQR) = 1.8, Very good quality"
        category = "material"
        identifier = "LAUSMILA000241671000001"
        comment = "'Reference flow: The functional unit is the production of 1 kg of Acai berry at the farm, (total  amount of water 1862 m3/ha).
            Allocation: No allocation
            System boundaries: Cradle-to-gate. The inventory includes the processes of tree seedling production and planting, fertilisation (mineral and manure).
            Geography: Brazil
            Technology: Conventional production.
            Time: 2000-2018
            Data quality rating (DQR) = 1.8, Very good quality"
        date = "2018-09-20"
        generator = "Kleis,
            Telephone: 0041 21 211 21 21; E-mail: lca@kleis.ch;"
        collectionMethod = "Sampling procedure: Principles"
        dataTreatment = "Data traitement"
        verification = "Proof reading validation: Passed internally.
            Validator: KleisAgro
            E-mail: lca@agroscope.admin.ch; Company: Agroscope; Country: CH"
        systemDescription = "name: Desc"
        allocationRules = "allocationRules"
        processType = "Unit process"
        status = "To be reviewed"
        infrastructure = "false"
        record = "data entry by: Kerny@kleis.ch"
        platformId = "platformId"
        literatures = "
            * Methodological Guidelines for the Life Cycle Inventory of Agricultural Products.
            * Another Methodological."
    }



    products { // Product
        // name: Acai berry, at farm (WFLDB 3.7)/BR U
        // category: _WFLDB 3.7 (Phase 2)\Plant products\Perennials\Acai berry
        // The yield when productive is 9750 kg/ha-y .
        // The final yield corresponds to the average yield over the entire lifetime of the tree.
        7500 kg acai_berry_at_farm_wfldb_3_7_br_u allocate 100 percent // 9750*10/13
    }
    
    products { // Avoid Products
    }
    
    inputs { // materialsAndFuels
    }

    inputs { // electricityAndHeat
    }

    emissions { // To Air
    }

    emissions { // To Water
    }

    emissions { // To Soil
    }

    emissions { // Economics
    }

    emissions { // Non Material
    }

    emissions { // Social
    }

    emissions { // Final Waste Flows
    }

    inputs { // Waste To Treatment
    }

    emissions { // Remaining Waste
    }

    emissions { // Separated Waste
    }

    resources {
        // (2,2,1,1,1,na)
        9752559010.236041 kg carbon_dioxide_in_air_raw // 145.56 * 67E6 / (1 -4E-6)
        // (2,2,1,1,1,na)
        20295.524449877732 MJ energy_gross_calorific_value_in_biomass_raw // 20295.524449877732
        // (2,1,1,1,1,na)
        501.95555914578216 m3 water_well_br_raw // 501.95555914578216
        // (2,1,1,1,1,na)
        2170.792762893368 m3 water_river_br_raw // 2170.792762893368
        // (2,1,1,1,1,na)
        10000.0 m2a occupation_permanent_crop_irrigated_raw // 10000.0
        // (2,1,1,1,1,na)
        2170.792762893368 m3 water_river_br_raw // 2170.792762893368
        // (2,1,1,1,1,na)
        10000.0 m2a occupation_permanent_crop_irrigated_raw // 10000.0
        // (2,1,1,1,1,na)
        2170.792762893368 m3 transformation_from_permanent_crop_irrigated_raw // 2170.792762893368
        // (2,1,1,1,1,na)
        2170.792762893368 m3 water_river_br_raw // 2170.792762893368
        // (2,1,1,1,1,na)
        500.0 m2 transformation_to_permanent_crop_irrigated_raw // 500.0
    }

}

""".trimIndent()
        // Better way to view large diff than using mockk.verify
        assertEquals("processes/material", pathSlot.captured)
        assertEquals(expected, bodySlot.captured)
        assertEquals(true, indexSlot.captured)
    }


    @Test
    fun test_render_shouldRender_forWasteTreatment() {
        // Given
        val waste = initWasteTreatment()


        // When
        sut.render(waste, writer)

        // Then
        val expected = """


process waste6422_6422 {

    meta {
        category = "processing"
        identifier = "XYXYX"
        processType = "System"
    }



    products { // Product
        // name: Acai berry, at farm (WFLDB 3.7)/BR U
        // category: _WFLDB 3.7 (Phase 2)\Plant products\Perennials\Acai berry
        // wasteType: not defined
        // The yield when productive is 9750 kg/ha-y .
        // The final yield corresponds to the average yield over the entire lifetime of the tree.
        7500 kg acai_berry_at_farm_wfldb_3_7_br_u // 9750*10/13
    }
    
    products { // Avoid Products
    }
    
    inputs { // materialsAndFuels
        // (2,2)
        6 g carbon_dioxide21_in_final_waste_flow // 3 * 2
    }

    inputs { // electricityAndHeat
        // (2,2)
        6 g carbon_dioxide22_in_final_waste_flow // 3 * 2
    }

    emissions { // To Air
        // (2,2,1,1,1,na)
        9752559010.236041 kg carbon_dioxide2_in_air_air // 145.56 * 67E6 / (1 -4E-6)
    }

    emissions { // To Water
        // (2,2)
        6 g carbon_dioxide3_in_water_water // 3 * 2
    }

    emissions { // To Soil
        // (2,2)
        6 g carbon_dioxide4_in_soil_soil // 3 * 2
    }

    emissions { // Economics
        // (2,2)
        6 g carbon_dioxide5_in_economics_economic // 3 * 2
    }

    emissions { // Non Material
        // (2,2)
        6 g carbon_dioxide7_in_non_mat_non_mat // 3 * 2
    }

    emissions { // Social
        // (2,2)
        6 g carbon_dioxide6_in_social_social // 3 * 2
    }

    emissions { // Final Waste Flows
        // (2,2)
        6 g carbon_dioxide8_in_final_waste_flow // 3 * 2
    }

    inputs { // Waste To Treatment
        // (2,2)
        6 g carbon_dioxide20_in_final_waste_flow // 3 * 2
    }

    emissions { // Remaining Waste
        // QQQ No managed
    }

    emissions { // Separated Waste
        // QQQ No managed
    }

    resources {
        // (2,2,1,1,1,na)
        9752559010.236041 kg carbon_dioxide_in_air_raw // 145.56 * 67E6 / (1 -4E-6)
    }

}

""".trimIndent()
        // Better way to view large diff than using mockk.verify
        assertEquals("processes/processing", pathSlot.captured)
        assertEquals(expected, bodySlot.captured)
        assertEquals(true, indexSlot.captured)
    }

    @Test
    fun test_render_shouldRender_forWasteScenario() {
        // Given
        val waste = initWasteScenario()

        // When
        sut.render(waste, writer)

        // Then
        val expected = """


process wastescen6422_6422 {

    meta {
        category = "processing"
        identifier = "XYXYX"
        processType = "System"
    }



    products { // Product
        // name: Municipal solid waste (waste scenario) {CY}| Treatment of waste | Cut-off, U
        // category: Municipal
        // wasteType: All waste types
        // Cyprus
        1.0 kg municipal_solid_waste_waste_scenario_cy_treatment_of_waste_cut_off_u // 1.0
    }
    
    products { // Avoid Products
    }
    
    inputs { // materialsAndFuels
    }

    inputs { // electricityAndHeat
    }

    emissions { // To Air
    }

    emissions { // To Water
    }

    emissions { // To Soil
    }

    emissions { // Economics
    }

    emissions { // Non Material
    }

    emissions { // Social
    }

    emissions { // Final Waste Flows
    }

    inputs { // Waste To Treatment
    }

    emissions { // Remaining Waste
    }

    emissions { // Separated Waste
    }

    resources {
    }

}

""".trimIndent()
        // Better way to view large diff than using mockk.verify
        assertEquals("processes/processing", pathSlot.captured)
        assertEquals(expected, bodySlot.captured)
        assertEquals(true, indexSlot.captured)
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
E-mail: lca@agroscope.admin.ch; Company: Agroscope; Country: CH
"""
            )
            .comment(
                """"Reference flow: The functional unit is the production of 1 kg of Acai berry at the farm, (total  amount of water 1862 m3/ha).

Allocation: No allocation

System boundaries: Cradle-to-gate. The inventory includes the processes of tree seedling production and planting, fertilisation (mineral and manure). 

Geography: Brazil

Technology: Conventional production.

Time: 2000-2018

Data quality rating (DQR) = 1.8, Very good quality
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
                .name("Water, river, BR")
                .subCompartment("in water")
                .unit("m3")
                .amount(Numeric.of(2170.792762893368))
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
                .comment("(2,1,1,1,1,na)\n")
        )
        sample.resources().add(
            ElementaryExchangeRow()
                .name("Water, river, BR")
                .subCompartment("in water")
                .unit("m3")
                .amount(Numeric.of(2170.792762893368))
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

        sample.resources().add(
            ElementaryExchangeRow()
                .name("Transformation, from permanent crop, irrigated")
                .subCompartment("in water")
                .unit("m3")
                .amount(Numeric.of(2170.792762893368))
                .uncertainty(UncertaintyRecord.undefined())
                .comment("(2,1,1,1,1,na)\n")
        )
        sample.resources().add(
            ElementaryExchangeRow()
                .name("Water, river, BR")
                .subCompartment("in water")
                .unit("m3")
                .amount(Numeric.of(2170.792762893368))
                .uncertainty(UncertaintyRecord.logNormal(1.0744))
                .comment("(2,1,1,1,1,na)\n")
        )
        sample.resources().add(
            ElementaryExchangeRow()
                .name("Transformation, to permanent crop, irrigated")
                .subCompartment("land")
                .unit("m2")
                .amount(Numeric.of(500.0))
                .uncertainty(UncertaintyRecord.logNormal(1.2077))
                .comment("(2,1,1,1,1,na)\n")
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

    private fun initWasteTreatment(): ProcessBlock {
        val waste = ProcessBlock().name("Waste")
            .category(ProcessCategory.PROCESSING) //Category type
            .identifier("XYXYX")
            .processType(ProcessType.SYSTEM)
        waste.resources().add(
            ElementaryExchangeRow()
                .name("Carbon dioxide, in air")
                .subCompartment("in air")
                .unit("kg")
                .amount(Numeric.of("145.56 * 67E6 / (1 -4E-6)"))
                .uncertainty(UncertaintyRecord.logNormal(1.0744))
                .comment("(2,2,1,1,1,na)\n")
        )
        waste.emissionsToAir().add(
            ElementaryExchangeRow()
                .name("Carbon dioxide2, in air")
                .subCompartment("in air")
                .unit("kg")
                .amount(Numeric.of("145.56 * 67E6 / (1 -4E-6)"))
                .uncertainty(UncertaintyRecord.logNormal(1.0744))
                .comment("(2,2,1,1,1,na)\n")
        )
        waste.emissionsToWater().add(
            ElementaryExchangeRow()
                .name("Carbon dioxide3, in water")
                .subCompartment("in water")
                .unit("g")
                .amount(Numeric.of("3 * 2"))
                .uncertainty(UncertaintyRecord.logNormal(1.01))
                .comment("(2,2)\n")
        )
        waste.emissionsToSoil().add(
            ElementaryExchangeRow()
                .name("Carbon dioxide4, in soil")
                .subCompartment("in soil")
                .unit("g")
                .amount(Numeric.of("3 * 2"))
                .uncertainty(UncertaintyRecord.logNormal(1.01))
                .comment("(2,2)\n")
        )
        waste.economicIssues().add(
            ElementaryExchangeRow()
                .name("Carbon dioxide5, in economics")
                .subCompartment("in economics")
                .unit("g")
                .amount(Numeric.of("3 * 2"))
                .uncertainty(UncertaintyRecord.logNormal(1.01))
                .comment("(2,2)\n")
        )
        waste.socialIssues().add(
            ElementaryExchangeRow()
                .name("Carbon dioxide6, in social")
                .subCompartment("in social")
                .unit("g")
                .amount(Numeric.of("3 * 2"))
                .uncertainty(UncertaintyRecord.logNormal(1.01))
                .comment("(2,2)\n")
        )
        waste.nonMaterialEmissions().add(
            ElementaryExchangeRow()
                .name("Carbon dioxide7, in non mat")
                .subCompartment("in non mat")
                .unit("g")
                .amount(Numeric.of("3 * 2"))
                .uncertainty(UncertaintyRecord.logNormal(1.01))
                .comment("(2,2)\n")
        )
        waste.finalWasteFlows().add(
            ElementaryExchangeRow()
                .name("Carbon dioxide8, in final waste flow")
                .subCompartment("in final waste flow")
                .unit("g")
                .amount(Numeric.of("3 * 2"))
                .uncertainty(UncertaintyRecord.logNormal(1.01))
                .comment("(2,2)\n")
        )
        waste.remainingWaste().add(
            WasteFractionRow()
                .fraction(0.5)
                .wasteTreatment("No managed")
                .comment("(2,2)\n")
        )
        waste.separatedWaste().add(
            WasteFractionRow()
                .fraction(0.5)
                .wasteTreatment("No managed")
                .comment("(2,3)\n")
        )
        waste.wasteTreatment(
            WasteTreatmentRow().name("Acai berry, at farm (WFLDB 3.7)/BR U")
                .unit("kg")
                .amount(Numeric.of("9750*10/13"))
                .wasteType("not defined")
                .category("_WFLDB 3.7 (Phase 2)\\Plant products\\Perennials\\Acai berry")
                .comment(
                    """The yield when productive is 9750 kg/ha-y .
The final yield corresponds to the average yield over the entire lifetime of the tree."""
                )
        )
        waste.wasteToTreatment().add(
            TechExchangeRow()
                .name("Carbon dioxide20, in final waste flow")
                .unit("g")
                .amount(Numeric.of("3 * 2"))
                .uncertainty(UncertaintyRecord.logNormal(1.01))
                .comment("(2,2)\n")
                .platformId("platformId")
        )
        waste.materialsAndFuels().add(
            TechExchangeRow()
                .name("Carbon dioxide21, in final waste flow")
                .unit("g")
                .amount(Numeric.of("3 * 2"))
                .uncertainty(UncertaintyRecord.logNormal(1.01))
                .comment("(2,2)\n")
                .platformId("platformId")
        )
        waste.electricityAndHeat().add(
            TechExchangeRow()
                .name("Carbon dioxide22, in final waste flow")
                .unit("g")
                .amount(Numeric.of("3 * 2"))
                .uncertainty(UncertaintyRecord.logNormal(1.01))
                .comment("(2,2)\n")
                .platformId("platformId")
        )
        return waste
    }

    private fun initWasteScenario(): ProcessBlock {
        val waste = ProcessBlock().name("WasteScen")
            .category(ProcessCategory.PROCESSING) //Category type
            .identifier("XYXYX")
            .processType(ProcessType.SYSTEM)
        waste.wasteScenario(
            WasteTreatmentRow()
                .name("Municipal solid waste (waste scenario) {CY}| Treatment of waste | Cut-off, U")
                .unit("kg")
                .amount(Numeric.of("1.0"))
                .wasteType("All waste types")
                .category("Municipal")
                .comment("Cyprus")
                .platformId("plateformId")

        )
        return waste
    }


}