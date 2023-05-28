package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.actions.csv.CsvProcessor
import ch.kleis.lcaplugin.actions.csv.CsvRequestReader
import ch.kleis.lcaplugin.actions.csv.CsvResultWriter
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.language.parser.LcaFileCollector
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import java.io.FileNotFoundException
import kotlin.io.path.Path

class AssessProcessWithDataAction(
    private val processName: String,
    private val matchLabels: Map<String, String>,
) : AnAction(
    "Run with ${processName}.csv",
    "Run with ${processName}.csv",
    AllIcons.Actions.Execute,
) {
    companion object {
        private val LOG = Logger.getInstance(AssessProcessWithDataAction::class.java)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(LangDataKeys.PSI_FILE) as LcaFile? ?: return
        val containingDirectory = file.containingDirectory ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Run with ${processName}.csv") {
            override fun run(indicator: ProgressIndicator) {
                try {
                    // read
                    indicator.fraction = 0.0
                    indicator.text = "Reading $processName.csv"
                    val csvFile = Path(containingDirectory.virtualFile.path, "$processName.csv").toFile()
                    val requests = csvFile.inputStream().use {
                        val requestReader = CsvRequestReader(processName, matchLabels, it)
                        requestReader.read()
                    }

                    // process
                    val symbolTable = runReadAction {
                        val collector = LcaFileCollector()
                        val parser = LcaLangAbstractParser(collector.collect(file))
                        parser.load()
                    }
                    val csvProcessor = CsvProcessor(symbolTable)
                    val results = requests.map { request ->
                        ProgressManager.checkCanceled()
                        indicator.text = "Processing using ${request.arguments()}"
                        indicator.fraction = indicator.fraction + 1.0 / requests.size
                        csvProcessor.process(request)
                    }

                    // write
                    indicator.text = "Writing to $processName.results.csv"
                    indicator.fraction = 1.0
                    val csvResultFile = Path(containingDirectory.virtualFile.path, "$processName.results.csv").toFile()
                    CsvResultWriter(csvResultFile.outputStream()).use { writer ->
                        writer.write(results)
                    }

                    // done
                    indicator.text = "Written to $processName.results.csv"
                    indicator.fraction = 1.0
                    val title = "${requests.size} successful assessments of process $processName"
                    val message = "Results stored in ${processName}.results.csv"
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("LcaAsCode")
                        .createNotification(title, message, NotificationType.INFORMATION)
                        .notify(project)
                } catch (e: EvaluatorException) {
                    val title = "Error while assessing $processName"
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("LcaAsCode")
                        .createNotification(title, e.message ?: "unknown error", NotificationType.ERROR)
                        .notify(project)
                    LOG.warn("Unable to process computation", e)
                } catch (e: NoSuchElementException) {
                    val title = "Error while assessing $processName"
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("LcaAsCode")
                        .createNotification(title, e.message ?: "unknown error", NotificationType.ERROR)
                        .notify(project)
                    LOG.warn("Unable to process computation", e)
                } catch (e: FileNotFoundException) {
                    val title = "Error while assessing $processName"
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("LcaAsCode")
                        .createNotification(title, e.message ?: "unknown error", NotificationType.ERROR)
                        .notify(project)
                }
            }
        })
    }
}
