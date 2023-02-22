package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.core.assessment.Assessment
import ch.kleis.lcaplugin.core.lang.Compiler
import ch.kleis.lcaplugin.core.lang.EntryPoint
import ch.kleis.lcaplugin.core.lang.LinkerException
import ch.kleis.lcaplugin.core.lang.VSystem
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
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
import com.intellij.ui.content.ContentFactory

class AssessSystemAction(private val systemName: String) : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(LangDataKeys.PSI_FILE) as LcaFile? ?: return
        val parser = LcaLangAbstractParser(project)

        try {
            val (pkg, dependencies) = parser.collect(file.getPackage().name!!)
            val entryPoint = EntryPoint(pkg, systemName)
            val program = Compiler(entryPoint, dependencies).compile()
            val value = program.run() as VSystem
            val assessment = Assessment(value)
            val result = assessment.inventory()
            displayToolWindow(project, result)
        } catch (e: EvaluatorException) {
            val result = InventoryError(e.message ?: "evaluator: unknown error")
            displayToolWindow(project, result)
        } catch (e: LinkerException) {
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


