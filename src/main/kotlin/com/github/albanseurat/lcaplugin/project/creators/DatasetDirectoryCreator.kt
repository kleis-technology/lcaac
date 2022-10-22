package com.github.albanseurat.lcaplugin.project.creators

import com.intellij.ide.IdeBundle
import com.intellij.ide.actions.ElementCreator
import com.intellij.internal.statistic.collectors.fus.fileTypes.FileTypeUsageCounterCollector
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import java.io.File

class DatasetDirectoryCreator(
    val project: Project,
    private val directory: PsiDirectory,
    errorTitle: String
) : ElementCreator(project, errorTitle) {
    override fun create(newName: String): Array<PsiElement> {
        val subDirectory = directory.createSubdirectory(newName)
        FileTypeUsageCounterCollector.triggerCreate(project, subDirectory.virtualFile)
        return arrayOf(subDirectory)
    }

    override fun getActionName(newName: String): String {
        return IdeBundle.message(
            "progress.creating.directory",
            directory.virtualFile.presentableUrl,
            File.separator,
            newName
        )
    }

}
