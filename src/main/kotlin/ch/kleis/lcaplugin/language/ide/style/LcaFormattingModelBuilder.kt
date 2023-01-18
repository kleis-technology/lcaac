package ch.kleis.lcaplugin.language.ide.style

import ch.kleis.lcaplugin.LcaLanguage.Companion.INSTANCE
import ch.kleis.lcaplugin.psi.LcaTypes.*
import com.intellij.formatting.*
import com.intellij.psi.codeStyle.CodeStyleSettings


class LcaFormattingModelBuilder : FormattingModelBuilder {

    companion object {

        private fun createSpaceBuilder(settings: CodeStyleSettings): SpacingBuilder {
            return SpacingBuilder(settings, INSTANCE)
                .before(PROCESS)
                .spacing(0, 0, 0, false, 1)
                .betweenInside(IDENTIFIER, LBRACE, PROCESS)
                .spacing(1, 1, 0, false, 0)
                .betweenInside(LBRACE, RBRACE, PROCESS)
                .spacing(0 , 0, 2, false, 0)
        }
    }

    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        return FormattingModelProvider
            .createFormattingModelForPsiFile(
                formattingContext.containingFile,
                LcaAstBlock(
                    formattingContext.node,
                    createSpaceBuilder(formattingContext.codeStyleSettings)
                ),
                formattingContext.codeStyleSettings
            )
    }
}
