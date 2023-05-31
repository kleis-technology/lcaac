package ch.kleis.lcaplugin.language.psi.type.spec

import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import ch.kleis.lcaplugin.psi.LcaProcess
import ch.kleis.lcaplugin.psi.LcaTechnoProductExchange
import com.intellij.psi.util.PsiTreeUtil

interface PsiOutputProductSpec : PsiUIDOwner {
    fun getContainingProcess(): LcaProcess {
        return PsiTreeUtil.getParentOfType(this, LcaProcess::class.java) as LcaProcess
    }

    fun getContainingTechnoExchange(): LcaTechnoProductExchange {
        return PsiTreeUtil.getParentOfType(this, LcaTechnoProductExchange::class.java) as LcaTechnoProductExchange
    }
}
