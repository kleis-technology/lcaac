package ch.kleis.lcaplugin.compute

import ch.kleis.lcaplugin.language.psi.type.PsiBioExchange
import ch.kleis.lcaplugin.language.psi.type.PsiInputExchange
import ch.kleis.lcaplugin.language.psi.type.PsiTechnoExchange
import com.intellij.psi.PsiElement

interface ModelResolver {
    fun resolveInputExchange(el: PsiInputExchange): PsiTechnoExchange
    fun resolveBioExchange(el: PsiBioExchange): PsiElement
}

class ModelResolverImpl : ModelResolver {
    override fun resolveInputExchange(el: PsiInputExchange): PsiTechnoExchange {
        return (el.reference?.resolve() ?: el) as PsiTechnoExchange
    }

    override fun resolveBioExchange(el: PsiBioExchange): PsiElement {
        return el.reference?.resolve() ?: el
    }
}
