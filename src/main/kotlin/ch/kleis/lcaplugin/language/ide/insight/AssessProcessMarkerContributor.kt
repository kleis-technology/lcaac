package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.actions.AssessProcessAction
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

/*
    https://github.com/JetBrains/intellij-plugins/blob/master/makefile/resources/META-INF/plugin.xml
 */

class AssessProcessMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (isTarget(element)) {
            val process = element.parent as PsiProcess
            val target = process.getProcessTemplateRef().getUID().name
            val action = AssessProcessAction(target)
            return Info(AllIcons.Actions.Execute, {
                "Assess $target"
            }, action)
        }
        return null
    }

    private fun isTarget(element: PsiElement): Boolean =
            element.elementType == LcaTypes.PROCESS_KEYWORD && element.parent is PsiProcess
}
