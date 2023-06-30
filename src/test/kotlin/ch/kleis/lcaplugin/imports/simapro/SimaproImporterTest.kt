package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.ide.imports.simapro.SimaproImportSettings
import ch.kleis.lcaplugin.ide.imports.simapro.SubstanceImportMode
import ch.kleis.lcaplugin.imports.*
import ch.kleis.lcaplugin.imports.simapro.substance.Ef3xDictionary
import ch.kleis.lcaplugin.imports.simapro.substance.SimaproDictionary
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.RefreshQueue
import io.mockk.*
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.test.assertEquals

class SimaproImporterTest {
    private val file = this.javaClass.classLoader.getResource("sample_wfldb_370.csv")!!.file

    private val rootFolder = Files.createTempDirectory("lca_test_").toString()
    private var settings = mockk<SimaproImportSettings>()
    private var watcher = mockk<AsynchronousWatcher>()
    private var controller = mockk<AsyncTaskController>()

    private val outputUnitFile = "$rootFolder${File.separatorChar}unit.lca"
    private val outputProcessFile = "$rootFolder${File.separatorChar}process.lca"
    private val outputSubstanceFile = "$rootFolder${File.separatorChar}substance.lca"

    @Before
    fun init() {
        settings = mockk<SimaproImportSettings>()
        every { settings.libraryFile } returns file
        every { settings.rootPackage } returns "ecoinvent"
        every { settings.rootFolder } returns rootFolder

        mockkStatic(LocalFileSystem::class)
        val fileSys = mockk<LocalFileSystem>()
        every { LocalFileSystem.getInstance() } returns fileSys
        val vFile = mockk<VirtualFile>()
        every { fileSys.findFileByPath(any()) } returns vFile
        mockkStatic(ModalityState::class)
        every { ModalityState.current() } returns mockk()
        mockkStatic(RefreshQueue::class)
        val refresh = mockk<RefreshQueue>()
        every { RefreshQueue.getInstance() } returns refresh
        justRun { refresh.refresh(false, false, null, ModalityState.current(), vFile) }
    }

    @After
    fun after() {
        unmockkAll()
    }

    @Test
    fun import_ShouldImportAll_WhenAllSwitchesAreOn() {
        // Given
        every { settings.importUnits } returns true
        every { settings.importProcesses } returns true
        every { settings.importSubstancesMode } returns SubstanceImportMode.SIMAPRO
        justRun { watcher.notifyProgress(any()) }
        justRun { watcher.notifyCurrentWork(any()) }
        every { controller.isActive() } returns true

        val sut = SimaproImporter(settings)

        // When
        val result = sut.import(controller, watcher)

        // Then
        assertTrue("Unit file should exists", Path.of(outputUnitFile).exists())
        assertTrue(result is SummaryInSuccess)
        assertEquals("88 units, 3 parameters, 10 processes, 1353 substances", result.getResourcesAsString())
        assertTrue(result.durationInSec >= 0)
    }

    @Test
    fun import_ShouldImportNothing_WhenAllSwitchesAreOff() {
        // Given
        every { settings.importUnits } returns false
        every { settings.importProcesses } returns false
        every { settings.importSubstancesMode } returns SubstanceImportMode.EF30
        mockkObject(Ef3xDictionary.Companion)
        every { Ef3xDictionary.fromClassPath(any()) } returns SimaproDictionary()
        val sut = SimaproImporter(settings)

        // When
        sut.import(controller, watcher)

        // Then
        assertFalse("Unit file should not exists", Path.of(outputUnitFile).exists())
        assertFalse("Process file should not exists", Path.of(outputProcessFile).exists())
        assertFalse("Substance file should not exists", Path.of(outputSubstanceFile).exists())
    }

    @Test
    fun import_ShouldReturnAnErrorSummary_WhenAnErrorHappen() {
        // Given
        every { settings.importUnits } returns true
        every { settings.importProcesses } returns true
        every { settings.importSubstancesMode } returns SubstanceImportMode.SIMAPRO
        every { watcher.notifyProgress(any()) } throws Exception("Unexpected")

        val sut = SimaproImporter(settings)

        // When
        val result = sut.import(controller, watcher)

        // Then
        assertTrue(result is SummaryInError)
        assertEquals("", result.getResourcesAsString())
        assertThat((result as SummaryInError).errorMessage, CoreMatchers.containsString("Unexpected"))
        assertTrue(result.durationInSec >= 0)
    }

    @Test
    fun import_ShouldReturnAnInterruptedSummary_WhenStoppedByUser() {
        // Given
        every { settings.importUnits } returns true
        every { settings.importProcesses } returns true
        every { settings.importSubstancesMode } returns SubstanceImportMode.SIMAPRO
        justRun { watcher.notifyProgress(any()) }
        every { controller.isActive() } returns false

        val sut = SimaproImporter(settings)

        // When
        val result = sut.import(controller, watcher)

        // Then
        assertTrue(result is SummaryInterrupted)
        assertEquals("", result.getResourcesAsString())
        assertTrue(result.durationInSec >= 0)
    }


}