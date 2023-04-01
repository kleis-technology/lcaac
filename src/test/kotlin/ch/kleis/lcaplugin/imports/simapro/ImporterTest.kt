package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.ide.imports.LcaImportSettings
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.RefreshQueue
import com.intellij.util.io.exists
import io.mockk.*
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class ImporterTest {
    private val file = this.javaClass.classLoader.getResource("sample_wfldb_370.csv")!!.file

    //    private val file = "/Users/pke/Downloads/ecoinvent_38.csv"
    private val rootFolder = Files.createTempDirectory("lca_test_").toString()
    private var settings = mockk<LcaImportSettings>()

    private val output_unit_file = "$rootFolder${File.separatorChar}unit.lca"

    @Suppress("RemoveExplicitTypeArguments")
    @Before
    fun init() {
        settings = mockk<LcaImportSettings>()
        every { settings.libraryFile } returns file
        every { settings.rootPackage } returns "ecoinvent"
        every { settings.rootFolder } returns rootFolder

        mockkStatic(LocalFileSystem::class)
        val fileSys = mockk<LocalFileSystem>()
        every { LocalFileSystem.getInstance() } returns fileSys
        val vfile = mockk<VirtualFile>()
        every { fileSys.findFileByPath(output_unit_file) } returns vfile
        mockkStatic(ModalityState::class)
        every { ModalityState.current() } returns mockk()
        mockkStatic(RefreshQueue::class)
        val refresh = mockk<RefreshQueue>()
        every { RefreshQueue.getInstance() } returns refresh
        justRun { refresh.refresh(false, false, null, ModalityState.current(), vfile) }
    }

    @After
    fun after() {
        unmockkAll()
    }

    @Test
    fun test_e2e_import_all_when_switch_on() {
        // Given
        every { settings.importUnits } returns true

        // When
        Importer(settings).importFile()

        // Then
        assertTrue("Unit file should exists", Path.of(output_unit_file).exists())
    }

    @Test
    fun test_e2e_import_nothing_when_switch_off() {
        // Given
        every { settings.importUnits } returns false

        // When
        Importer(settings).importFile()

        // Then
        assertFalse("Unit file should exists", Path.of(output_unit_file).exists())
    }

}