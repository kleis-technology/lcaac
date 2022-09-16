package com.github.albanseurat.lcaplugin.language

import com.intellij.lexer.FlexAdapter

class LcaLexerAdapter : FlexAdapter(LcaLexer(null)) {
}
