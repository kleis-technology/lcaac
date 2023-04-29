package ch.kleis.lcaplugin.ide.imports.ecospold

import ch.kleis.lcaplugin.ide.imports.LcaImportDialog
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class EcospoldImportMenuAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val panel = EcospoldImportSettingsPanel(EcospoldImportSettings.instance)
        val dlg = LcaImportDialog(panel)
        dlg.show()
    }

}