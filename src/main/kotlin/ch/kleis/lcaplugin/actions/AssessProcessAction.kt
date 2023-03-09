package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.LcaFileType
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.TemplateExpression
import ch.kleis.lcaplugin.core.lang.preprocessor.PreProcessor
import ch.kleis.lcaplugin.core.matrix.InventoryError
import ch.kleis.lcaplugin.core.matrix.InventoryResult
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.ui.toolwindow.LcaResult
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.content.ContentFactory

class AssessProcessAction(private val processName: String) : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(LangDataKeys.PSI_FILE) as LcaFile? ?: return
        val psiManager = PsiManager.getInstance(project)
        val parser = LcaLangAbstractParser { pkgName ->
            val files = FileTypeIndex
                .getFiles(LcaFileType.INSTANCE, GlobalSearchScope.projectScope(project))
                .mapNotNull { psiManager.findFile(it) }
                .map { it as LcaFile }
                .filter { it.getPackage().name!! == pkgName }
            if (files.isEmpty()) throw NoSuchElementException("cannot find any LCA file for package $pkgName")
            files
        }

        try {
            val (pkg, deps) = parser.collect(file.getPackage().name!!)
            val fqn = "${pkg.name}.$processName"
            val program = PreProcessor(
                { it.processTemplates[fqn]!! },
                pkg,
                deps
            ).prepare()
            val value = Evaluator(program.environment).eval(program.entryPoint as TemplateExpression)
            TODO()
        } catch (e: EvaluatorException) {
            val result = InventoryError(e.message ?: "evaluator: unknown error")
            displayToolWindow(project, result)
        } catch (e: NoSuchElementException) {
            val result = InventoryError(e.message ?: "evaluator: unknown error")
            displayToolWindow(project, result)
        }
    }

    private fun displayToolWindow(project: Project, result: InventoryResult) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return
        val lcaResult = LcaResult(result)
        val content = ContentFactory.getInstance().createContent(lcaResult.getContent(), project.name, false)
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)
        toolWindow.show()
    }
}


