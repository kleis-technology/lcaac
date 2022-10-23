package ch.kleis.lcaplugin.language.psi

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.tree.TokenSet

object LcaTokenSets {
    val IDENTIFIERS = TokenSet.create(ch.kleis.lcaplugin.psi.LcaTypes.IDENTIFIER)
}
