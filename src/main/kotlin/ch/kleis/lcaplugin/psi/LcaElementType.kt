package ch.kleis.lcaplugin.psi

import ch.kleis.lcaplugin.LcaLanguage
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class LcaElementType(debugName: @NonNls String) : IElementType(debugName, LcaLanguage.INSTANCE)
