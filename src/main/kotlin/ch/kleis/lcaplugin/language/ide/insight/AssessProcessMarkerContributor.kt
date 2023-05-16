package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.actions.AssessProcessAction
import ch.kleis.lcaplugin.actions.AssessProcessWithDataAction
import ch.kleis.lcaplugin.actions.GraphChildProcessesAction
import ch.kleis.lcaplugin.language.psi.isProcess
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement

/*
    https://github.com/JetBrains/intellij-plugins/blob/master/makefile/resources/META-INF/plugin.xml
 */

class AssessProcessMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (isProcess(element)) {
            val process = element.parent as PsiProcess
            val target = process.getProcessTemplateRef().getUID().name
            val assessProcessAction = AssessProcessAction(target)
            val assessProcessWithExternalDataAction = AssessProcessWithDataAction(target)
            val graphChildProcessesAction = GraphChildProcessesAction(target)
            return Info(AllIcons.Actions.Execute, {
                "Run $target"
            }, assessProcessAction, assessProcessWithExternalDataAction, graphChildProcessesAction)
        }
        return null
    }
}
