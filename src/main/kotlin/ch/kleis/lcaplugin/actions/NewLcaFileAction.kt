package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.LcaFileType
import ch.kleis.lcaplugin.LcaIcons
import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

class NewLcaFileAction : CreateFileFromTemplateAction("Processs File", null, LcaIcons.FILE) {

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle("Processs File")
            .addKind("Processs", ch.kleis.lcaplugin.LcaFileType.INSTANCE.icon, "processs.lca")
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
       return "LCA Processs";
    }

}
