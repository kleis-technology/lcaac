package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.imports.ModelWriter
import io.mockk.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.openlca.simapro.csv.UncertaintyRecord
import org.openlca.simapro.csv.refdata.InputParameterBlock
import org.openlca.simapro.csv.refdata.InputParameterRow

class InputParameterRendererTest {
    private val writer = mockk<ModelWriter>()

    private val pathSlot = slot<String>()
    private val bodySlot = slot<String>()
    private val indexSlot = slot<Boolean>()
    private val sut = InputParameterRenderer()


    @Before
    fun before() {
        every { writer.write(capture(pathSlot), capture(bodySlot), capture(indexSlot)) } returns Unit
        mockkObject(ModelWriter)
        every { ModelWriter.sanitizeAndCompact("kg") } returns "kg"
    }

    @After
    fun after() {
        unmockkAll()
    }

    @Test
    fun render() {
        // Given
        val block = InputParameterBlock.forProject()
        block.parameters().add(
            InputParameterRow()
                .name("LUC_crop_specific")
                .value(1.0)
                .uncertainty(UncertaintyRecord.undefined())
                .isHidden(false)
                .comment("Approach for LUC: If the approach is specific, put the value to \"1\" else \"0\"\nfj sjfhsdk \n")
        )
        block.parameters().add(
            InputParameterRow()
                .name("Heavy_metal_uptake")
                .value(0.0)
                .uncertainty(UncertaintyRecord.logNormal(1.23))
                .isHidden(true)
                .comment("0 to be used else 1")
        )

        // When
        sut.render(block, writer)

        // Then
        val expected = """

variables {
    // Approach for LUC: If the approach is specific, put the value to "1" else "0"
    // fj sjfhsdk 
    LUC_crop_specific = 1.0 u
    // 0 to be used else 1
    Heavy_metal_uptake = 0.0 u
}

""".trimIndent()
        // Better way to view large diff than using mockk.verify
        Assert.assertEquals("main", pathSlot.captured)
        Assert.assertEquals(expected, bodySlot.captured)
        Assert.assertEquals(false, indexSlot.captured)
    }
}