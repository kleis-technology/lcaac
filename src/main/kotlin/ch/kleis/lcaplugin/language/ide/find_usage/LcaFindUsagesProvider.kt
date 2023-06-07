package ch.kleis.lcaplugin.language.ide.find_usage

import ch.kleis.lcaplugin.language.parser.LcaLexerAdapter
import ch.kleis.lcaplugin.psi.*
import ch.kleis.lcaplugin.psi.LcaTypes.*
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.TokenSet

/*
    https://plugins.jetbrains.com/docs/intellij/find-usages.html
    https://plugins.jetbrains.com/docs/intellij/find-usages-provider.html#define-a-find-usages-provider
 */

class LcaFindUsagesProvider : FindUsagesProvider {

    override fun getWordsScanner(): WordsScanner =
        DefaultWordsScanner(
            LcaLexerAdapter(),
            TokenSet.create(IDENTIFIER),
            TokenSet.create(COMMENT_LINE, COMMENT_CONTENT, COMMENT_BLOCK_START, COMMENT_BLOCK_END),
            TokenSet.EMPTY,
        )

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean =
        when (psiElement) {
            is LcaProcess -> true
            is LcaOutputProductSpec -> true
            is LcaSubstance -> true
            is LcaUnitDefinition -> true
            is LcaGlobalAssignment -> true
            is LcaAssignment -> true
            is LcaLabelAssignment -> true
            else -> false
        }

    override fun getHelpId(psiElement: PsiElement): String? = null

    override fun getType(element: PsiElement): String =
        when (element) {
            is LcaProcess -> "Process"
            is LcaOutputProductSpec -> "Product"
            is LcaSubstance -> "Substance"
            is LcaUnitDefinition -> "Unit"
            is LcaGlobalAssignment -> "Data"
            is LcaAssignment -> "Data"
            is LcaLabelAssignment -> "Label"
            else -> ""
        }

    override fun getDescriptiveName(element: PsiElement): String =
        (element as? PsiNamedElement)?.name.orEmpty()

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String =
        (element as? PsiNamedElement)?.name.orEmpty()
}
