package ch.kleis.lcaplugin.psi

import ch.kleis.lcaplugin.LcaLanguage
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class LcaTokenType(debugName: @NonNls String) : IElementType(debugName, LcaLanguage.INSTANCE) {
    override fun toString(): String {
        return "LcaTokenType." + super.toString()
    }
}
