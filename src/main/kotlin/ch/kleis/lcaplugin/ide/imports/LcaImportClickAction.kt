package ch.kleis.lcaplugin.ide.imports

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class LcaImportClickAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val dlg = LcaImportDialog()
        dlg.show()
    }

}