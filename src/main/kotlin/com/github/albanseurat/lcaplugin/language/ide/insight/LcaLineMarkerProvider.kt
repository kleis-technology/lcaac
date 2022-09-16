package com.github.albanseurat.lcaplugin.language.ide.insight

import com.github.albanseurat.lcaplugin.LcaIcons
import com.github.albanseurat.lcaplugin.psi.LcaDatasetDefinition
import com.github.albanseurat.lcaplugin.psi.LcaTypes
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
        if(element.elementType == LcaTypes.IDENTIFIER && element.parent is LcaDatasetDefinition) {
            val builder: NavigationGutterIconBuilder<PsiElement> = NavigationGutterIconBuilder.create(AllIcons.Actions.Execute)
                .setTargets(element)
                .setTooltipText("Calculate dataset impact");
            result.add(builder.createLineMarkerInfo(element))
        }
    }
}