package ch.kleis.lcaplugin

import com.intellij.lang.Language


class LcaLanguage : Language("LCA") {
    companion object {
        val INSTANCE: LcaLanguage = LcaLanguage()
    }
}
