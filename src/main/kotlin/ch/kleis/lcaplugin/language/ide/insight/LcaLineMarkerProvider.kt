package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.LcaIcons
import ch.kleis.lcaplugin.psi.LcaProcessDefinition
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType


class LcaLineMarkerProvider : RelatedItemLineMarkerProvider() {


    override fun collectNavigationMarkers(
        element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if(element.elementType == ch.kleis.lcaplugin.psi.LcaTypes.IDENTIFIER && element.parent is ch.kleis.lcaplugin.psi.LcaProcessDefinition) {
            val builder: NavigationGutterIconBuilder<PsiElement> = NavigationGutterIconBuilder.create(AllIcons.Actions.Execute)
                .setTargets(element)
                .setTooltipText("Calculate process impact");
            result.add(builder.createLineMarkerInfo(element))
        }
    }
}
