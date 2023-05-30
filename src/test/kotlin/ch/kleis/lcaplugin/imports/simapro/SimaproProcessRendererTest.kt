package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.ide.imports.simapro.SubstanceImportMode
import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.model.ProcessImported
import ch.kleis.lcaplugin.imports.shared.ProcessSerializer
import io.mockk.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.openlca.simapro.csv.enums.ProcessCategory
import org.openlca.simapro.csv.process.ProcessBlock


class SimaproProcessRendererTest {
    private val writer = mockk<ModelWriter>()

    private val pathSlot = slot<String>()
    private val bodySlot = slot<String>()

    @Before
    fun before() {
    }

    @After
    fun after() {
        unmockkAll()
    }

    @Test
    fun render_shouldRender() {
        // Given
        every { writer.write(capture(pathSlot), capture(bodySlot), index = true, closeAfterWrite = true) } returns Unit
        val processBlock = mockk<ProcessBlock>()
        every { processBlock.category() } returns ProcessCategory.ENERGY
        mockkObject(SimaproProcessMapper)
        val mapper = mockk<SimaproProcessMapper>()
        every { SimaproProcessMapper.of(SubstanceImportMode.SIMAPRO) } returns mapper
        val processImported = mockk<ProcessImported>()
        every { processImported.uid } returns "uid"
        every { mapper.map(processBlock) } returns processImported
        mockkObject(ProcessSerializer)
        every { ProcessSerializer.serialize(processImported) } returns "serialized process"
        val sut = ProcessRenderer(SubstanceImportMode.SIMAPRO)

        // When
        sut.render(processBlock, writer)

        // Then, Better way to view large diff than using mockk.verify
        Assert.assertEquals("processes/energy/uid.lca", pathSlot.captured)
        Assert.assertEquals("serialized process", bodySlot.captured)

    }

}
