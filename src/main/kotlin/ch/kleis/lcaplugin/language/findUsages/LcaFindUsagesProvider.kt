package ch.kleis.lcaplugin.language.findUsages

import ch.kleis.lcaplugin.language.parser.LcaLexerAdapter
import ch.kleis.lcaplugin.language.psi.type.PsiProductExchange
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.psi.LcaTypes.IDENTIFIER
import ch.kleis.lcaplugin.psi.LcaTypes.STRING_LITERAL
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.TokenSet


class LcaFindUsagesProvider : FindUsagesProvider {

    override fun getWordsScanner(): WordsScanner =
        DefaultWordsScanner(
            LcaLexerAdapter(),
            TokenSet.create(IDENTIFIER),
            TokenSet.EMPTY,
            TokenSet.create(STRING_LITERAL),
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
