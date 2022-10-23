package ch.kleis.lcaplugin

import com.intellij.lang.Language
import org.jetbrains.annotations.NonNls


class LcaLanguage : Language("LCA") {
    companion object {
        val INSTANCE: ch.kleis.lcaplugin.LcaLanguage = ch.kleis.lcaplugin.LcaLanguage()
    }
}
