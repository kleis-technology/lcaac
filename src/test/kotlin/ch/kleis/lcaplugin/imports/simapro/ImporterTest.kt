package ch.kleis.lcaplugin.imports.simapro

import org.junit.Test

class ImporterTest {

    @Test
    fun test_main() {
        Importer().importFile("ecoinvent", "/Users/pke/Downloads/export", "/Users/pke/Downloads/sample_wfldb_370.csv")
    }


}