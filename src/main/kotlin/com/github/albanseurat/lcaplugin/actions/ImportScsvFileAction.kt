package com.github.albanseurat.lcaplugin.actions

import com.intellij.ide.IdeBundle
import com.intellij.ide.actions.ElementCreator
import com.intellij.internal.statistic.collectors.fus.fileTypes.FileTypeUsageCounterCollector
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserDialog
import com.intellij.openapi.fileChooser.ex.FileChooserDialogImpl
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import java.io.File

class ImportScsvFileAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val view = e.getData(LangDataKeys.IDE_VIEW) ?: return
        val project = e.project ?: return
        val dir = view.orChooseDirectory ?: return

        val elementCreator = MyElementCreator(project, dir, "error")
        val elements = elementCreator.tryCreate("scsv.lca")
        val containerFile = elements[0]

        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("gz")
        val fileChooserDialog: FileChooserDialog = FileChooserDialogImpl(descriptor, project)
        val scsvFile = fileChooserDialog.choose(project)[0]

        val task = ImportScsvFileBackgroundTask(project, scsvFile, containerFile)
        ReadAction.run<RuntimeException> {
            ProgressManager.getInstance().run(task)
        }
    }

    private class MyElementCreator(
        val project: Project,
        val directory: PsiDirectory,
        errorTitle: String
    ) : ElementCreator(project, errorTitle) {
        override fun create(newName: String): Array<PsiElement> {
            val file = WriteAction.compute<PsiFile, Exception> { directory.createFile(newName) }
            FileTypeUsageCounterCollector.triggerCreate(project, file.virtualFile)
            return arrayOf(file)
        }

        override fun getActionName(newName: String): String {
            return IdeBundle.message("progress.creating.file", directory.virtualFile.presentableUrl, File.separator, newName)
        }
    }
}
