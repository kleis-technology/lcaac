package com.github.albanseurat.lcaplugin.language.parser

import com.github.albanseurat.lcaplugin.language.LcaLexer
import com.intellij.lexer.FlexAdapter

class LcaLexerAdapter : FlexAdapter(LcaLexer(null)) {
}
