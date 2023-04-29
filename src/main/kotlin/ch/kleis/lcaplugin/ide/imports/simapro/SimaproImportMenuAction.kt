package ch.kleis.lcaplugin.ide.imports.simapro

import ch.kleis.lcaplugin.ide.imports.LcaImportDialog
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class SimaproImportMenuAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val panel = SimaproImportSettingsPanel(SimaproImportSettings.instance)
        val dlg = LcaImportDialog(panel)
        dlg.show()
    }

}