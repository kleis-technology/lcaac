package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.core.matrix.InventoryMatrix
import ch.kleis.lcaplugin.ui.toolwindow.LcaProcessAssessResult
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory

class DisplayInventoryMatrix(
    private val project: Project,
    private val result: InventoryMatrix,
) {
}
