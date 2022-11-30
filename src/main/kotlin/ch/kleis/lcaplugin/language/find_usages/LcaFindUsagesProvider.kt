package ch.kleis.lcaplugin.language.find_usages

import ch.kleis.lcaplugin.language.parser.LcaLexerAdapter
import ch.kleis.lcaplugin.language.psi.type.PsiProductExchange
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
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
            TokenSet.create(IDENTIFIER, STRING_LITERAL),
            TokenSet.create(COMMENT_LINE_START, COMMENT_CONTENT, COMMENT_BLOCK_START, COMMENT_BLOCK_END),
            TokenSet.EMPTY,
        )

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean =
        psiElement is PsiNamedElement

    override fun getHelpId(psiElement: PsiElement): String? = null

    override fun getType(element: PsiElement): String =
        if (element is PsiProductExchange) "Product" else if (element is PsiSubstance) "Substance" else ""

    override fun getDescriptiveName(element: PsiElement): String =
        (element as? PsiNamedElement)?.name.orEmpty()

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String =
        (element as? PsiNamedElement)?.name.orEmpty()
}
