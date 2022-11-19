package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.LcaFileType
import ch.kleis.lcaplugin.compute.ModelMethodVisitor
import ch.kleis.lcaplugin.compute.ModelSystemVisitor
import ch.kleis.lcaplugin.language.psi.stub.SubstanceKeyIndex
import ch.kleis.lcaplugin.ui.toolwindow.LcaResult
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.content.ContentFactory

class AssessProjectAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFiles = FileTypeIndex.getFiles(LcaFileType.INSTANCE, GlobalSearchScope.projectScope(project))
        val psiManager = PsiManager.getInstance(project)
        val psiFiles = virtualFiles.mapNotNull { psiManager.findFile(it) }
        if (psiFiles.isEmpty()) return

        val modelSystemVisitor = ModelSystemVisitor()
        psiFiles.forEach { it.accept(modelSystemVisitor) }
        val system = modelSystemVisitor.getSystem()


        val modelMethodVisitor = ModelMethodVisitor()
        system.getElementaryFlows().getElements()
            .forEach { flow ->
                SubstanceKeyIndex.findSubstances(project, flow.getUniqueId())
                    .forEach {
                        it.accept(modelMethodVisitor)
                    }
            }
        val methodMap = modelMethodVisitor.getMethodMap()
        val firstMethodName = methodMap.keys.firstOrNull() ?: return
        val method = methodMap[firstMethodName] ?: return

        val result = system.observe(
            method.getIndicators(),
            emptyList(),
            method.getCharacterizationFactors()
        )
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return

        val lcaResult = LcaResult(result)
        val content = ContentFactory.getInstance().createContent(lcaResult.getContent(), project.name, false)
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)
        toolWindow.show()
    }
}


