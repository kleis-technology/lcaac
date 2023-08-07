package ch.kleis.lcaplugin.ide.imports.ecospold.lcia

import ch.kleis.lcaplugin.MyBundle
import ch.kleis.lcaplugin.ide.imports.LcaImportDialog
import ch.kleis.lcaplugin.ide.imports.ecospold.EcospoldImportSettingsPanel
import ch.kleis.lcaplugin.ide.imports.ecospold.settings.LCIASettings
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class EcospoldLCIAImportMenuAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val panel = EcospoldImportSettingsPanel(LCIASettings.instance)
        val title = MyBundle.message("lca.dialog.import.ecospold.lcia.title")
        val dlg = LcaImportDialog(panel, title, e.project)
        dlg.show()
    }
}