package com.github.albanseurat.lcaplugin.actions

import com.github.albanseurat.lcaplugin.LcaFileType
import com.github.albanseurat.lcaplugin.LcaIcons
import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

class NewLcaFileAction : CreateFileFromTemplateAction("Datasets File", null, LcaIcons.FILE) {

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle("Datasets File")
            .addKind("Datasets", LcaFileType.INSTANCE.icon, "datasets.lca")
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
       return "LCA Datasets";
    }

}