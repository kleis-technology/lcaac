package com.github.albanseurat.lcaplugin.language.ide.syntax

import com.github.albanseurat.lcaplugin.language.LcaLexerAdapter
import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.KEYWORD
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

class LcaSyntaxHighlighter : SyntaxHighlighterBase() {

    companion object {
        val DATASET_KEYS = arrayOf(createTextAttributesKey("LCA_DATASET", KEYWORD))
        val IDENTIFIER_KEYS = arrayOf(
            createTextAttributesKey(
                "IDENTIFIER",
                DefaultLanguageHighlighterColors.IDENTIFIER
            )
        )
        val EMPTY_KEYS = emptyArray<TextAttributesKey>()
    }

    override fun getHighlightingLexer(): Lexer {
        return LcaLexerAdapter()
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        if (tokenType == LcaTypes.DATASET_KEYWORD) {
            return DATASET_KEYS
        } else if (tokenType == LcaTypes.IDENTIFIER) {
            return IDENTIFIER_KEYS
        }
        return EMPTY_KEYS
    }
}