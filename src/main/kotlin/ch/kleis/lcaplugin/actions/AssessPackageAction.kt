package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.LcaFileType
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.compute.ModelCoreSystemVisitor
import ch.kleis.lcaplugin.ui.toolwindow.LcaResult
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.content.ContentFactory

class AssessPackageAction(private val packageName: String) : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val psiManager = PsiManager.getInstance(project)
        val psiFiles = FileTypeIndex
            .getFiles(LcaFileType.INSTANCE, GlobalSearchScope.projectScope(project))
            .mapNotNull { psiManager.findFile(it) }
            .map { it as LcaFile }
            .filter { it.getPackage().name!! == packageName }
        if (psiFiles.isEmpty()) return // TODO: popup

        val visitor = ModelCoreSystemVisitor()
        psiFiles.forEach { it.accept(visitor) }

        val system = visitor.getSystem()
        val inventory = system.inventory()
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return

        val lcaResult = LcaResult(inventory)
        val content = ContentFactory.getInstance().createContent(lcaResult.getContent(), project.name, false)
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)
        toolWindow.show()
    }
}


