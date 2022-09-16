package com.github.albanseurat.lcaplugin

import com.intellij.lang.Language
import org.jetbrains.annotations.NonNls


class LcaLanguage : Language("LCA") {
    companion object {
        val INSTANCE: LcaLanguage = LcaLanguage()
    }
}
