package ch.kleis.lcaplugin.imports.simapro.substance

import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.model.ImportedSubstance
import ch.kleis.lcaplugin.imports.shared.serializer.SubstanceSerializer
import io.mockk.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.openlca.simapro.csv.enums.ElementaryFlowType
import org.openlca.simapro.csv.refdata.ElementaryFlowBlock
import org.openlca.simapro.csv.refdata.ElementaryFlowRow

class SimaproSubstanceRendererTest {
    private val writer = mockk<ModelWriter>()
    private val pathSlot = slot<String>()
    private val bodySlot = slot<CharSequence>()

    private val sut = SimaproSubstanceRenderer()


    @Before
    fun before() {
        every { writer.write(capture(pathSlot), capture(bodySlot)) } returns Unit
    }

    @After
    fun after() {
        unmockkAll()
    }

    @Test
    fun render() {
        // Given
        val block = ElementaryFlowBlock.of(ElementaryFlowType.RESOURCES)
        val row = ElementaryFlowRow()
            .name("Aluminium")
            .unit("kg")
            .cas("007429-90-5")
            .comment("Formula: Al\nAl\n")
            .platformId("platformId")
        block.flows().add(row)
        val substance = mockk<ImportedSubstance>()
        mockkObject(SimaproSubstanceMapper)
        every { SimaproSubstanceMapper.map(row, ElementaryFlowType.RESOURCES, "raw") } returns substance
        mockkObject(SubstanceSerializer)
        every { SubstanceSerializer.serialize(substance) } returns "serialized substance"


        // When
        sut.render(block, writer)

        // Then, Better way to view large diff than using mockk.verify
        Assert.assertEquals("substances/raw/aluminium.lca", pathSlot.captured)
        Assert.assertEquals("serialized substance", bodySlot.captured.toString())
    }
}

