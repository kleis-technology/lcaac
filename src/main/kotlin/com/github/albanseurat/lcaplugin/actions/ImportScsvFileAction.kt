package com.github.albanseurat.lcaplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserDialog
import com.intellij.openapi.fileChooser.ex.FileChooserDialogImpl
import com.intellij.openapi.progress.ProgressIndicatorProvider

class ImportScsvFileAction(): AnAction() {


    override fun actionPerformed(e: AnActionEvent) {
        val dataContext = e.dataContext
        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("gz")
        val fileChooserDialog: FileChooserDialog = FileChooserDialogImpl(descriptor, project)
        val file = fileChooserDialog.choose(project)
        val task = ImportScsvFileBackgroundTask(project, file[0])
        val progressIndicator = ProgressIndicatorProvider.getGlobalProgressIndicator()
        task.run(progressIndicator)
    }
}
