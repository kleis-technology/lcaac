package com.github.albanseurat.lcaplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserDialog
import com.intellij.openapi.fileChooser.ex.FileChooserDialogImpl

class ImportScsvFileAction(): AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val dataContext = e.dataContext
        val view = LangDataKeys.IDE_VIEW.getData(dataContext) ?: return

        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return
        val projectBasePath = project.basePath

        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(".scsv")
        val fileChooserDialog: FileChooserDialog = FileChooserDialogImpl(descriptor, project)

        val file = fileChooserDialog.choose(project)

        TODO("parse scsv file into lca files")
    }
}
