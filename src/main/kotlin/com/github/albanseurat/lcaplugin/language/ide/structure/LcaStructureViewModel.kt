package com.github.albanseurat.lcaplugin.language.ide.structure

import com.github.albanseurat.lcaplugin.psi.LcaDatasetDefinition
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile


class LcaStructureViewModel(editor: Editor?, psiFile: PsiFile)
    : StructureViewModelBase(psiFile, editor, LcaStructureViewElement(psiFile)), StructureViewModel.ElementInfoProvider {


    override fun getSorters(): Array<Sorter> {
        return arrayOf(Sorter.ALPHA_SORTER)
    }

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement?): Boolean {
        return false
    }

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
        return element.value is LcaDatasetDefinition
    }

    override fun getSuitableClasses(): Array<Class<*>> {
        return arrayOf(LcaDatasetDefinition::class.java)
    }
}
