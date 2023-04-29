package ch.kleis.lcaplugin.imports

import java.nio.file.Path
import java.time.Instant
import java.time.temporal.ChronoUnit

abstract class Importer {
    private val begin = Instant.now()

    fun import(controller: AsyncTaskController, watcher: AsynchronousWatcher): Summary {
        return try {
            importAll(controller, watcher)
            val duration = begin.until(Instant.now(), ChronoUnit.SECONDS)
            SummaryInSuccess(duration, collectProgress())
        } catch (e: ImportInterruptedException) {
            val duration = begin.until(Instant.now(), ChronoUnit.SECONDS)
            SummaryInterrupted(duration, collectProgress())
        } catch (e: Exception) {
            val duration = begin.until(Instant.now(), ChronoUnit.SECONDS)
            SummaryInError(duration, collectProgress(), e.message ?: "")
        }
    }

    protected abstract fun importAll(controller: AsyncTaskController, watcher: AsynchronousWatcher)

    protected abstract fun collectProgress(): List<Imported>
    abstract fun getImportRoot(): Path

}