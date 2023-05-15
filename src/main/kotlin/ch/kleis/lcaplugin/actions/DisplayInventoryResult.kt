package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.core.matrix.InventoryResult
import ch.kleis.lcaplugin.ui.toolwindow.LcaProcessAssessResult
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory

class DisplayInventoryResult(
    private val project: Project,
    private  val result: InventoryResult,
) {
    fun show() {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return
        val lcaProcessAssessResult = LcaProcessAssessResult(result)
        val content =
            ContentFactory.getInstance().createContent(lcaProcessAssessResult.getContent(), project.name, false)
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)
        toolWindow.show()
    }
}
