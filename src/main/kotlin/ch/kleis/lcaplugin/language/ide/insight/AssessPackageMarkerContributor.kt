package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.actions.AssessPackageAction
import ch.kleis.lcaplugin.language.psi.type.PsiPackage
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

/*
    https://github.com/JetBrains/intellij-plugins/blob/master/makefile/resources/META-INF/plugin.xml
 */

class AssessPackageMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (isPackage(element)) {
            val pkg = element.parent as PsiPackage
            val target = pkg.name!!
            val action = AssessPackageAction(target)
            return Info(AllIcons.Actions.Execute, {
                "Assess $target"
            }, action)
        }
        return null
    }

    private fun isPackage(element: PsiElement) =
        element.elementType == LcaTypes.PACKAGE_KEYWORD && element.parent is PsiPackage


}
