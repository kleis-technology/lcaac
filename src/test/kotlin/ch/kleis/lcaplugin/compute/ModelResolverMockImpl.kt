package ch.kleis.lcaplugin.compute

import ch.kleis.lcaplugin.language.psi.type.PsiBioExchange
import ch.kleis.lcaplugin.language.psi.type.PsiInputExchange
import ch.kleis.lcaplugin.language.psi.type.PsiTechnoExchange
import com.intellij.psi.PsiElement

class ModelResolverMockImpl : ModelResolver {
    override fun resolveInputExchange(el: PsiInputExchange): PsiTechnoExchange {
        return el
    }

    override fun resolveBioExchange(el: PsiBioExchange): PsiElement {
        return el
    }
}
