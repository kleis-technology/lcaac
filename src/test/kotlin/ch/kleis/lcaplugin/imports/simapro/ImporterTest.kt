package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.ide.imports.LcaImportSettings
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class ImporterTest {

    @Test
    fun test_main() {
        val settings = mockk<LcaImportSettings>()
        every { settings.libraryFile } returns "/Users/pke/Downloads/sample_wfldb_370.csv"
        every { settings.rootPackage } returns "ecoinvent"
        every { settings.rootFolder } returns "/Users/pke/Downloads/export"
        Importer(settings).importFile()
    }


}