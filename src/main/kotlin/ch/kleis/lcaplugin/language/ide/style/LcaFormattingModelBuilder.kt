package ch.kleis.lcaplugin.language.ide.style

import ch.kleis.lcaplugin.LcaLanguage.Companion.INSTANCE
import ch.kleis.lcaplugin.psi.LcaTypes.*
import com.intellij.formatting.*
import com.intellij.psi.codeStyle.CodeStyleSettings


class LcaFormattingModelBuilder : FormattingModelBuilder {

    companion object {

        private fun createSpaceBuilder(settings: CodeStyleSettings): SpacingBuilder {
            return SpacingBuilder(settings, INSTANCE)
                // Before Block
                .before(IMPORT)
                .spacing(0, 0, 0, true, 2)
                .before(UNIT_LITERAL)
                .spacing(0, 0, 0, true, 1)
                .before(PROCESS)
                .spacing(0, 0, 0, true, 1)
                .before(SUBSTANCE)
                .spacing(0, 0, 0, true, 1)
                // Braces
                .before(RBRACE)
                .spacing(0, 0, 0, true, 0)
                .before(LBRACE)
                .spacing(1, 1, 0, false, 0)
                .between(LBRACE, ASSIGNMENT )
                .spacing(0, 0, 1, true, 0)
                .between(LBRACE, TECHNO_INPUT_EXCHANGE )
                .spacing(0, 0, 1, true, 0)
                .between(LBRACE, TECHNO_PRODUCT_EXCHANGE )
                .spacing(0, 0, 1, true, 0)
                .between(LBRACE, BIO_EXCHANGE )
                .spacing(0, 0, 1, true, 0)
                // Unit_Literal
                .aroundInside(UNIT_REF, UNIT_LITERAL)
                .spaces(1)
                // PROCESS
                .aroundInside(PROCESS_TEMPLATE_REF, PROCESS)
                .spaces(1)
                .betweenInside(IDENTIFIER, LBRACE, PROCESS)
                .spacing(1, 1, 0, false, 0)
                .betweenInside(LBRACE, RBRACE, PROCESS)
                .spacing(0 , 0, 2, false, 0)
                .beforeInside(PARAMS, PROCESS)
                .spacing(0 , 0, 0, true, 1)
                .before(VARIABLES)
                .spacing(0 , 0, 0, true, 1)
                .beforeInside(BLOCK_PRODUCTS, PROCESS)
                .spacing(0 , 0, 0, true, 1)
                .beforeInside(BLOCK_INPUTS, PROCESS)
                .spacing(0 , 0, 0, true, 1)
                .beforeInside(BLOCK_EMISSIONS, PROCESS)
                .spacing(0 , 0, 0, true, 1)
                .beforeInside(BLOCK_RESOURCES, PROCESS)
                .spacing(0 , 0, 0, true, 1)
                .beforeInside(BLOCK_META, PROCESS)
                .spacing(0 , 0, 0, true, 1)
                // Comments
                .before(COMMENT_CONTENT)
                .spaces(0)
                .before(COMMENT_BLOCK_END)
                .spaces(0)
                // Formula
                .around(PLUS).spaces(1)
                .around(MINUS).spaces(1)
                .around(SLASH).spaces(1)
                .around(STAR).spaces(1)
                .around(EQUAL).spaces(1)
                .after(LPAREN).spaces(1)
                .before(RPAREN).spaces(1)
                .around(NUMBER).spaces(1)
                .before(PRODUCT_REF)
                .spaces(1)
                .before(SUBSTANCE_REF)
                .spaces(1)
                // Substances
                .before(INDICATOR_REF)
                .spaces(1)
        }
    }

    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        return FormattingModelProvider
            .createFormattingModelForPsiFile(
                formattingContext.containingFile,
                LcaIndentBlock(
                    formattingContext.node,
                    createSpaceBuilder(formattingContext.codeStyleSettings)
                ),
                formattingContext.codeStyleSettings
            )
    }
}
