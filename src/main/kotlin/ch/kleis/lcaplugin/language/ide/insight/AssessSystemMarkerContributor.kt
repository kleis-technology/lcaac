package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.actions.AssessSystemAction
import ch.kleis.lcaplugin.language.psi.type.PsiSystem
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

/*
    https://github.com/JetBrains/intellij-plugins/blob/master/makefile/resources/META-INF/plugin.xml
 */

class AssessSystemMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (isTarget(element)) {
            val system = element.parent as PsiSystem
            val target = system.getUid()?.name!!
            val action = AssessSystemAction(target)
            return Info(AllIcons.Actions.Execute, {
                "Assess $target"
            }, action)
        }
        return null
    }

    private fun isTarget(element: PsiElement): Boolean {
        if (element.elementType != LcaTypes.SYSTEM_KEYWORD || element.parent !is PsiSystem) {
            return false
        }
        val parent = element.parent as PsiSystem
        return parent.getUid() != null
    }
}
