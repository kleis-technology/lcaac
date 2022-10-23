package ch.kleis.lcaplugin.language.ide.syntax

import ch.kleis.lcaplugin.psi.LcaTypes.STRING
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset

class LcaStringFoldingBuilder : FoldingBuilderEx(), DumbAware {

    companion object {
        private const val MAX_DISPLAYED_LENGTH = 60
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> =
        PsiTreeUtil
            .collectElements(root) { element -> element.elementType == STRING }
            .map { FoldingDescriptor(it, TextRange(it.startOffset, it.endOffset)) }
            .toTypedArray()

    override fun getPlaceholderText(node: ASTNode): String =
        when (node.elementType) {
            STRING -> getPlaceholderText(node.text)
            else -> "..."
        }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = node.textContains('\n')

    private fun getPlaceholderText(text: String): String =
        if (text.length > MAX_DISPLAYED_LENGTH || text.contains('\n')) {
            text.take(MAX_DISPLAYED_LENGTH).replace("\\n.*".toRegex(), "").plus("...")
        } else text
}
