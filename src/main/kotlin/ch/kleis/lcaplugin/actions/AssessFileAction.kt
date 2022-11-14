package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.compute.ModelVisitor
import ch.kleis.lcaplugin.ui.toolwindow.LcaResult
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory

class AssessFileAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(PlatformDataKeys.PSI_FILE) ?: return

        val modelVisitor = ModelVisitor()
        file.accept(modelVisitor)

        val system = modelVisitor.getSystem()
        val methodMap = modelVisitor.getMethodMap()
        val firstMethodName = methodMap.keys.first()
        val method = methodMap[firstMethodName] ?: return

        val result = system.observe(
            method.getIndicators(),
            emptyList(),
            method.getCharacterizationFactors()
        )
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return

        val lcaResult = LcaResult(result)
        val content = ContentFactory.getInstance().createContent(lcaResult.getContent(), file.name, false);
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)
        toolWindow.show()
    }
}


