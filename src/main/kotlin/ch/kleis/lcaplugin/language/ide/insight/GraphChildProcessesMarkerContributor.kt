package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.actions.GraphChildProcessesAction
import ch.kleis.lcaplugin.language.psi.isProcess
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.ui.jcef.JBCefApp

/*
Note: Only one Run Line Action marker is taken into account in the plugin.xml, so we have to rely on the Line Marker
interface, which makes some namings a bit weird, as it is expected to jump around in code rather than run stuff.
 */

class GraphChildProcessesMarkerProvider: LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        // Don't even print something if we cannot render the graph later on.
        if (!JBCefApp.isSupported()) return null

        return if (isProcess(element)) {
            LineMarkerInfo(
                    element,
                    element.textRange,
                    AllIcons.Graph.Layout,
                    null,
                    GraphChildProcessesAction(),
                    GutterIconRenderer.Alignment.LEFT
            ) { "Show process child graph" }
        } else {
            null
        }
    }
}