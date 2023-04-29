package ch.kleis.lcaplugin.imports.ecospold

import ch.kleis.lcaplugin.ide.imports.ecospold.EcospoldImportSettings
import ch.kleis.lcaplugin.imports.AsyncTaskController
import ch.kleis.lcaplugin.imports.AsynchronousWatcher
import ch.kleis.lcaplugin.imports.Imported
import ch.kleis.lcaplugin.imports.Importer
import java.nio.file.Path

class EcospoldImporter(private val settings: EcospoldImportSettings) : Importer() {

    override fun importAll(controller: AsyncTaskController, watcher: AsynchronousWatcher) {
        TODO("Not yet implemented")
    }

    override fun getImportRoot(): Path {
        return Path.of(settings.rootFolder)
    }

    override fun collectProgress(): List<Imported> {
        return listOf(
//            Imported(unitRenderer.nbUnit, "units"),
//            Imported(processRenderer.nbProcesses, "processes"),
        )


    }

}