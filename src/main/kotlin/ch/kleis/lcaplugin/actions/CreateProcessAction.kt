package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.LcaFileType
import ch.kleis.lcaplugin.LcaLanguage
import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.application.runReadAction
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
        val content = """
            
            process "$process" {
                products {
                    - "$process" 1 $unitText
                }
            }
            
            
        """.trimIndent()
        val newProcess = runReadAction {
            PsiFileFactory.getInstance(project)
                .createFileFromText(
                    "_dummy_.${LcaFileType.INSTANCE.defaultExtension}",
                    LcaFileType.INSTANCE, content)
        }

        WriteCommandAction.writeCommandAction(project).run(ThrowableRunnable<RuntimeException> {
            file.node.addChildren(newProcess.node.firstChildNode, newProcess.node.lastChildNode, null)
            ReformatCodeProcessor(file, true).run()
        })

        (newProcess.lastChild.navigationElement as Navigatable).navigate(true)
    }
}
