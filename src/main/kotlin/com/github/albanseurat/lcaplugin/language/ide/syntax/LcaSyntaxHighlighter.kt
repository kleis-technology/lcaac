package com.github.albanseurat.lcaplugin.language.ide.syntax

import com.github.albanseurat.lcaplugin.language.parser.LcaLexerAdapter
import com.github.albanseurat.lcaplugin.psi.LcaTypes.*
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.KEYWORD
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

class LcaSyntaxHighlighter : SyntaxHighlighterBase() {

    companion object {
        val KEYWORD_KEYS = arrayOf(createTextAttributesKey("LCA_KEYWORD", KEYWORD))
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
        return when (tokenType) {
            DATASET_KEYWORD, PRODUCTS_KEYWORD, EMISSIONS_KEYWORD, RESOURCES_KEYWORD, INPUTS_KEYWORD -> KEYWORD_KEYS
            IDENTIFIER -> IDENTIFIER_KEYS
            else -> EMPTY_KEYS
        }
    }
}