package ch.kleis.lcaplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 *  An action to view the current processes as a graph.
 *
 *  This class represents an IntelliJ plugin action to open a tool window displaying the currently viewed LCA process
 *  list as a graph.
 */
class GraphChildProcessesAction(private val processName: String): AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        print("The action GraphChildProcessesAction was performed")
    }
}