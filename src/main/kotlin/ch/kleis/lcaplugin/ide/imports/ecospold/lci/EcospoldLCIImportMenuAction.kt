package ch.kleis.lcaplugin.ide.imports.ecospold.lci

import ch.kleis.lcaplugin.MyBundle
import ch.kleis.lcaplugin.ide.imports.LcaImportDialog
import ch.kleis.lcaplugin.ide.imports.ecospold.EcospoldImportSettingsPanel
import ch.kleis.lcaplugin.ide.imports.ecospold.settings.UPRAndLCISettings
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class EcospoldLCIImportMenuAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val panel = EcospoldImportSettingsPanel(UPRAndLCISettings.instance)
        val title = MyBundle.message("lca.dialog.import.ecospold.lci.title")
        val dlg = LcaImportDialog(panel, title, e.project)
        dlg.show()
    }
}