package ch.kleis.lcaplugin.imports

import ch.kleis.lcaplugin.TestUtils
import ch.kleis.lcaplugin.imports.simapro.AsynchronousWatcher
import com.intellij.openapi.vfs.LocalFileSystem
import io.mockk.*
import junit.framework.TestCase
import org.junit.After
import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

class ModelWriterTest {

    @After
    fun after() {
        unmockkAll()
    }

    @Test
    fun write() {
        // Given
        val watcher = mockk<AsynchronousWatcher>()
        justRun { watcher.notifyCurrentWork("relative_file") }
        val sut = ModelWriter("test", System.getProperty("java.io.tmpdir"), listOf("custom.import"), watcher)


        // When
        sut.write("relative_file", "Content", false)

        // Then
        verify { watcher.notifyCurrentWork("relative_file") }
        assertTrue(File(System.getProperty("java.io.tmpdir") + File.separator + "relative_file.lca").exists())
    }

    @Test
    fun close_ShouldCloseSubResources() {
        // Given
        val watcher = mockk<AsynchronousWatcher>()
        val sut = ModelWriter("test", System.getProperty("java.io.tmpdir"), listOf("custom.import"), watcher)
        val existingWriter = mockk<FileWriterWithSize>()
        justRun { existingWriter.close() }
        val opened = mutableMapOf("relative_file.lca" to existingWriter)
        TestUtils.setField(sut, "openedFiles", opened)
        mockkStatic(LocalFileSystem::class)
        val fileSys = mockk<LocalFileSystem>()
        every { LocalFileSystem.getInstance() } returns fileSys
        every { fileSys.findFileByPath(any()) } returns null

        // When
        sut.close()

        // Then
        verify { existingWriter.close() }
    }

    private data class Input(val raw: String, val expected: String)

    private data class Result(val result: String, val expected: String)

    @Test
    fun test_sanitize() {
        // Given
        val data = arrayOf(
            Input("01", "_01"),
            Input("ab", "ab"),
            Input("a_+__b", "a__p___b"),
            Input("a_*__b", "a__m___b"),
            Input("m*2a", "m_m_2a"),
            Input("a&b", "a_a_b"),
            Input("  a&b++", "a_a_b_p__p")
        )

        // When
        val result = data.map { p -> Result(ModelWriter.sanitize(p.raw), p.expected) }

        // Then
        result.forEach { r -> TestCase.assertEquals(r.expected, r.result) }
    }

    @Test
    fun test_sanitizeAndCompact() {
        // Given
        val data = arrayOf(
            Input("01", "_01"),
            Input("ab", "ab"),
            Input("a_+__b", "a_p_b"),
            Input("a_*__b", "a_m_b"),
            Input("m*2a", "m_m_2a"),
            Input("a&b", "a_a_b"),
            Input("  a&b++", "a_a_b_p_p")
        )

        // When
        val result = data.map { p -> Result(ModelWriter.sanitizeAndCompact(p.raw), p.expected) }

        // Then
        result.forEach { r -> TestCase.assertEquals(r.expected, r.result) }
    }

}