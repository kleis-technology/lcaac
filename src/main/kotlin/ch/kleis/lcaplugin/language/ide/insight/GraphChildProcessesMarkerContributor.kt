package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.isProcess
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement

class GraphChildProcessesMarkerProvider: LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (isProcess(element)) {
//            val process = element.parent as PsiProcess
            // val target = process.getProcessTemplateRef().getUID().name
            // val action = GraphChildProcessesAction(target)
            return LineMarkerInfo<PsiElement>(
                    element,
                    element.textRange,
                    AllIcons.Graph.Layout,
                    null,
                    null,
                    GutterIconRenderer.Alignment.LEFT
            ) { "Show process child graph" }
        }
        return null
    }
}