package ch.kleis.lcaplugin.imports.ecospold

import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.ecospold.model.ActivityDataset
import ch.kleis.lcaplugin.imports.ecospold.model.Classification
import ch.kleis.lcaplugin.imports.model.ImportedProcess
import ch.kleis.lcaplugin.imports.model.ImportedSubstance
import ch.kleis.lcaplugin.imports.shared.serializer.ProcessSerializer
import ch.kleis.lcaplugin.imports.shared.serializer.SubstanceSerializer
import io.mockk.*
import org.junit.After
import org.junit.Before
import kotlin.test.assertEquals


class EcospoldProcessRendererTest {
    private val writer = mockk<ModelWriter>()

    @Before
    fun before() {
    }

    @After
    fun after() {
        unmockkAll()
    }

    // FIXME @Test
    fun render_shouldRender() {
        // Given
        every { writer.write(any(), any(), index = false, closeAfterWrite = any()) } returns Unit
        val activity = mockk<ActivityDataset>()
        every { activity.description.activity.name } returns "pName"
        every { activity.description.geography?.shortName } returns "ch"
        every { activity.description.classifications } returns listOf(Classification("EcoSpold01Categories", "cat"))
        mockkObject(EcoSpoldProcessMapper)
        val importedProcess = mockk<ImportedProcess>()
        every { EcoSpoldProcessMapper.map(activity) } returns importedProcess
        val comments = mutableListOf<String>()
        every { importedProcess.comments } returns comments
        every { importedProcess.uid } returns "uid"
        mockkObject(ProcessSerializer)
        every { ProcessSerializer.serialize(importedProcess) } returns "serialized process"

        mockkObject(EcoSpoldSubstanceMapper)
        val importedSubstance = mockk<ImportedSubstance>()
        every { EcoSpoldSubstanceMapper.map(activity, "EF v3.1") } returns importedSubstance
        mockkObject(SubstanceSerializer)
        every { SubstanceSerializer.serialize(importedSubstance) } returns "serialized substance"
        val sut = EcospoldProcessRenderer()

        // When
        sut.render(activity, writer, "a comment", "EF v3.1")

        // Then, Better way to view large diff than using mockk.verify
        verifyOrder {
            writer.write("processes/cat/uid.lca", "serialized process", index = false, closeAfterWrite = false)
            writer.write("processes/cat/uid.lca", "serialized substance", index = false, closeAfterWrite = true)
        }
        assertEquals(mutableListOf("a comment"), comments)
    }

}
