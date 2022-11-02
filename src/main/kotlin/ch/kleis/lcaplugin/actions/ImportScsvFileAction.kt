package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.services.ScsvProcessBlockFormatter
import ch.kleis.lcaplugin.services.ScsvProcessBlockStream
import com.intellij.internal.statistic.collectors.fus.fileTypes.FileTypeUsageCounterCollector
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserDialog
import com.intellij.openapi.fileChooser.ex.FileChooserDialogImpl
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import java.util.zip.GZIPInputStream

class ImportScsvFileAction : AnAction() {
    val formatter = ScsvProcessBlockFormatter()

    override fun actionPerformed(e: AnActionEvent) {
        val view = e.getData(LangDataKeys.IDE_VIEW) ?: return
        val project = e.project ?: return
        val dir = view.orChooseDirectory ?: return

        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("gz")
        val fileChooserDialog: FileChooserDialog = FileChooserDialogImpl(descriptor, project)
        val scsvFile = fileChooserDialog.choose(project)[0]

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Importing SCSV file...") {
            override fun run(indicator: ProgressIndicator) {
                indicator.fraction = 0.0
                indicator.text = "Processing ..."

                val subdirectories: HashMap<String, PsiDirectory> = HashMap()

                var processBlockCount = 0
                ScsvProcessBlockStream {
                    ProgressManager.checkCanceled()
                    val content = formatter.format(it)
                    val ds = runReadAction {
                        PsiFileFactory.getInstance(project).createFileFromText(LcaLanguage.INSTANCE, content)
                    }

                    val key = it.category().name.lowercase()
                    val subdir: PsiDirectory = subdirectories[key] ?: run {
                        val createdSubdir = WriteCommandAction.writeCommandAction(project)
                            .compute<PsiDirectory, Throwable> {
                                val subdir = dir.createSubdirectory(key)
                                FileTypeUsageCounterCollector.triggerCreate(project, subdir.virtualFile)
                                subdir
                            }
                        subdirectories[key] = createdSubdir
                        createdSubdir
                    }

                    val containingFile = WriteCommandAction.writeCommandAction(project)
                        .compute<PsiFile, Throwable> {
                            val file = subdir.createFile("${it.identifier()}.lca")
                            FileTypeUsageCounterCollector.triggerCreate(project, file.virtualFile)
                            file
                        }

                    WriteCommandAction.writeCommandAction(containingFile, ds).run<Throwable> {
                        containingFile.node.addChild(ds.node.firstChildNode)
                    }

                    processBlockCount += 1
                    indicator.text = "Processing ... $processBlockCount processs"
                }.read(GZIPInputStream(scsvFile.inputStream))

                indicator.fraction = 1.0
                indicator.text = "Done"
            }
        })
    }
}
