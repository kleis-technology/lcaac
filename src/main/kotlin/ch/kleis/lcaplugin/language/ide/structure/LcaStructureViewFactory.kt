package ch.kleis.lcaplugin.language.ide.structure

import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile


class LcaStructureViewFactory : PsiStructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder? {
        return object : TreeBasedStructureViewBuilder() {

            override fun createStructureViewModel(editor: Editor?): StructureViewModel {
                return LcaStructureViewModel(editor, psiFile)
            }
        }
    }
}
