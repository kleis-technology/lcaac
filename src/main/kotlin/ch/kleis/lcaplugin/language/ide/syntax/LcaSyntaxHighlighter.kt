package ch.kleis.lcaplugin.language.ide.syntax

import ch.kleis.lcaplugin.language.parser.LcaLexerAdapter
import ch.kleis.lcaplugin.psi.LcaTypes.*
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors.BAD_CHARACTER
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType


class LcaSyntaxHighlighter : SyntaxHighlighterBase() {

    companion object {
        val KEYWORD_KEYS = arrayOf(createTextAttributesKey("LCA_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD))
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
        val FIELD_KEYS = arrayOf(createTextAttributesKey("FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD))
        val NUMBER_KEYS = arrayOf(createTextAttributesKey("UNIT", DefaultLanguageHighlighterColors.NUMBER))
        val BLOCK_COMMENT_KEYS =
            arrayOf(createTextAttributesKey("BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT))
    }

    override fun getHighlightingLexer(): Lexer {
        return LcaLexerAdapter()
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            PACKAGE_KEYWORD, PROCESS_KEYWORD, PARAMETERS_KEYWORD,
            PARAMETER_KEYWORD, SYSTEM_KEYWORD,
            UNIT_KEYWORD, PRODUCT_KEYWORD,
            PRODUCTS_KEYWORD, COPRODUCTS_KEYWORD, INPUTS_KEYWORD,
            EMISSIONS_KEYWORD, RESOURCES_KEYWORD -> KEYWORD_KEYS

            IDENTIFIER -> IDENTIFIER_KEYS

            STRING_LITERAL -> STRING_LITERAL_KEYS

            SCALE_KEYWORD, DIMENSION_KEYWORD,
            REFERENCE_UNIT_KEYWORD,
            SYMBOL_KEYWORD, NAME_KEYWORD -> FIELD_KEYS

            TokenType.BAD_CHARACTER -> BAD_CHARACTER_KEYS

            NUMBER -> NUMBER_KEYS

            COMMENT_BLOCK_START, COMMENT_BLOCK_END, COMMENT_LINE_START, COMMENT_CONTENT -> BLOCK_COMMENT_KEYS

            else -> EMPTY_KEYS
        }
    }
}
