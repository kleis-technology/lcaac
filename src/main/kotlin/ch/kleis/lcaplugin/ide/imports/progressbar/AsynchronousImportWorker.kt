package ch.kleis.lcaplugin.ide.imports.progressbar

import ch.kleis.lcaplugin.MyBundle
import ch.kleis.lcaplugin.ide.component.ProgressBar
import ch.kleis.lcaplugin.imports.*
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.concurrency.SwingWorker


class AsynchronousImportWorker(
    private val importer: Importer,
    private val onSuccess: Runnable,
    private val onFailure: Runnable,
    private val progressBar: ProgressBar
) : SwingWorker<Summary>(), AsyncTaskController {
    @Volatile
    var active = true

    init {
        progressBar.cancelAction = Runnable { this.active = false }
    }

    /* Called in worker Thread */
    override fun construct(): Summary {
        return importer.import(this, progressBar)
    }

    /* Called in GUI Thread after end of the worker task */
    override fun finished() {
        when (val result = get()) {
            is SummaryInSuccess -> {
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("LcaAsCode")
                    .createNotification(
                        MyBundle.message(
                            "lca.dialog.import.finished.success",
                            result.durationInSec,
                            result.getResourcesAsString()
                        ), NotificationType.INFORMATION
                    )
                    .notify(ProjectManager.getInstance().openProjects.firstOrNull())
                VirtualFileManager.getInstance().refreshAndFindFileByNioPath(importer.getImportRoot())
                onSuccess.run()
            }

            is SummaryInterrupted -> {
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("LcaAsCode")
                    .createNotification(
                        MyBundle.message(
                            "lca.dialog.import.finished.interrupted",
                            result.durationInSec,
                            result.getResourcesAsString()
                        ), NotificationType.INFORMATION
                    )
                    .notify(ProjectManager.getInstance().openProjects.firstOrNull())
                VirtualFileManager.getInstance().refreshAndFindFileByNioPath(importer.getImportRoot())
                onSuccess.run()
            }

            is SummaryInError -> {
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("LcaAsCode")
                    .createNotification(
                        MyBundle.message(
                            "lca.dialog.import.finished.error",
                            result.errorMessage,
                            result.getResourcesAsString()
                        ), NotificationType.ERROR
                    )
                    .notify(ProjectManager.getInstance().openProjects.firstOrNull())
                VirtualFileManager.getInstance().refreshAndFindFileByNioPath(importer.getImportRoot())
                onFailure.run()
            }
        }
    }

    override fun isActive(): Boolean {
        return active
    }


}