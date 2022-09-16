package com.github.albanseurat.lcaplugin.language.ide.structure

import com.github.albanseurat.lcaplugin.language.psi.LcaFile
import com.github.albanseurat.lcaplugin.psi.LcaDatasetDefinition
import com.github.albanseurat.lcaplugin.psi.impl.LcaDatasetDefinitionImpl
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.util.PsiTreeUtil
import kotlin.collections.ArrayList


class LcaStructureViewElement(private val element: NavigatablePsiElement)
    : StructureViewTreeElement, SortableTreeElement {


    override fun getValue(): Any {
        return element
    }

    override fun navigate(requestFocus: Boolean) {
        element.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean {
        return element.canNavigate()
    }

    override fun canNavigateToSource(): Boolean {
        return element.canNavigateToSource()
    }

    override fun getAlphaSortKey(): String {
        return element.name ?: ""
    }

    override fun getPresentation(): ItemPresentation {
        return element.presentation ?: PresentationData(element.name,
            null, AllIcons.Nodes.Class, null);
    }

    override fun getChildren(): Array<TreeElement> {
        if (element is LcaFile) {
            val definitions: List<LcaDatasetDefinition> = PsiTreeUtil.getChildrenOfTypeAsList(
                element,
                LcaDatasetDefinition::class.java
            )
            val treeElements: MutableList<TreeElement> = ArrayList(definitions.size)
            for (def in definitions) {
                treeElements.add(LcaStructureViewElement(def as LcaDatasetDefinitionImpl))
            }
            return treeElements.toTypedArray()
        }
        return emptyArray()
    }

}
