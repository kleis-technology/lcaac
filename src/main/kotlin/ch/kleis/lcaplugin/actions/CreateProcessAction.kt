package ch.kleis.lcaplugin.actions

import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.util.ThrowableRunnable


class CreateProcessAction(private val process: String, private val unitText: String?) : BaseIntentionAction() {

    override fun getText(): String {
        return "Create process '$process'"
    }

    override fun getFamilyName(): String {
        return "Create property"
    }

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        return true
    }


    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        WriteCommandAction.writeCommandAction(project).run(ThrowableRunnable<RuntimeException> {
            val newProcess = PsiFileFactory.getInstance(project)
                .createFileFromText(
                    "_Dummy_.${ch.kleis.lcaplugin.LcaFileType.INSTANCE.defaultExtension}",
                    ch.kleis.lcaplugin.LcaFileType.INSTANCE,
                    "\n\nprocess \"$process\" {\n products {\n - \"$process\" 1 $unitText\n}\n}\n"
                )
            file.node.addChildren(newProcess.firstChild.node, newProcess.lastChild.node, null);
            ReformatCodeProcessor(file, true).run()
            (newProcess.lastChild.navigationElement as Navigatable).navigate(true)
        })

    }
}
