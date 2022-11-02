package ch.kleis.lcaplugin.language.ide.syntax

import ch.kleis.lcaplugin.language.parser.LcaLexerAdapter
import ch.kleis.lcaplugin.psi.LcaTypes.*
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_FIELD
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.KEYWORD
import com.intellij.openapi.editor.HighlighterColors.BAD_CHARACTER
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
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
        val BAD_CHARACTER_KEYS = arrayOf(createTextAttributesKey("SIMPLE_BAD_CHARACTER", BAD_CHARACTER))
        val STRING_LITERAL_KEYS =
            arrayOf(createTextAttributesKey("STRING_LITERAL", DefaultLanguageHighlighterColors.STRING))
        val FIELD_KEYS = arrayOf(createTextAttributesKey("FIELD", INSTANCE_FIELD))
        val NUMBER_KEYS = arrayOf(createTextAttributesKey("UNIT", DefaultLanguageHighlighterColors.NUMBER))
    }

    override fun getHighlightingLexer(): Lexer {
        return LcaLexerAdapter()
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            PROCESS_KEYWORD, SUBSTANCE_KEYWORD, PRODUCTS_KEYWORD, META_KEYWORD, LAND_USE_KEYWORD,
            EMISSIONS_KEYWORD, RESOURCES_KEYWORD, INPUTS_KEYWORD -> KEYWORD_KEYS
            IDENTIFIER -> IDENTIFIER_KEYS
            STRING -> STRING_LITERAL_KEYS
            TYPE_KEYWORD, UNIT_KEYWORD -> FIELD_KEYS
            TokenType.BAD_CHARACTER -> BAD_CHARACTER_KEYS
            NUMBER -> NUMBER_KEYS
            else -> EMPTY_KEYS
        }
    }
}
