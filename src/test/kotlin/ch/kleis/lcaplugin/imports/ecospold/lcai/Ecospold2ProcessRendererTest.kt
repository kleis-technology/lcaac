package ch.kleis.lcaplugin.imports.ecospold.lcai

import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.ecospold.lcai.model.ActivityDataset
import ch.kleis.lcaplugin.imports.ecospold.lcai.model.Classification
import ch.kleis.lcaplugin.imports.model.ProcessImported
import ch.kleis.lcaplugin.imports.model.SubstanceImported
import ch.kleis.lcaplugin.imports.shared.ProcessSerializer
import ch.kleis.lcaplugin.imports.shared.SubstanceSerializer
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals


class Ecospold2ProcessRendererTest {
    private val writer = mockk<ModelWriter>()

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
        every { writer.write(any(), any(), index = false, closeAfterWrite = any()) } returns Unit
        val activity = mockk<ActivityDataset>()
        every { activity.description.activity.name } returns "pName"
        every { activity.description.geography?.shortName } returns "ch"
        every { activity.description.classifications } returns listOf(Classification("EcoSpold01Categories", "cat"))
        mockkObject(EcoSpold2ProcessMapper)
        val processImported = mockk<ProcessImported>()
        every { EcoSpold2ProcessMapper.map(activity) } returns processImported
        val comments = mutableListOf<String>()
        every { processImported.comments } returns comments
        every { processImported.uid } returns "uid"
        mockkObject(ProcessSerializer)
        every { ProcessSerializer.serialize(processImported) } returns "serialized process"

        mockkObject(EcoSpold2SubstanceMapper)
        val substanceImported = mockk<SubstanceImported>()
        every { EcoSpold2SubstanceMapper.map(activity, "EF v3.1") } returns substanceImported
        mockkObject(SubstanceSerializer)
        every { SubstanceSerializer.serialize(substanceImported) } returns "serialized substance"
        val sut = Ecospold2ProcessRenderer()

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
