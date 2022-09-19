package com.github.albanseurat.lcaplugin.language.ide.style

import com.github.albanseurat.lcaplugin.LcaLanguage
import com.github.albanseurat.lcaplugin.LcaLanguage.Companion.INSTANCE
import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.github.albanseurat.lcaplugin.psi.LcaTypes.*
import com.intellij.formatting.*
import com.intellij.psi.codeStyle.CodeStyleSettings


class LcaFormattingModelBuilder : FormattingModelBuilder {

    companion object {

        private fun createSpaceBuilder(settings: CodeStyleSettings): SpacingBuilder {
            return SpacingBuilder(settings, INSTANCE)
                .before(DATASET_DEFINITION)
                .spacing(0, 0, 0, false, 1)
                .betweenInside(IDENTIFIER, LBRACE, DATASET_DEFINITION)
                .spacing(1, 1, 0, false, 0)
                .betweenInside(LBRACE, RBRACE, DATASET_DEFINITION)
                .spacing(0 , 0, 2, false, 0);
        }
    }

    override fun createModel(formattingContext: FormattingContext): FormattingModel {

        return FormattingModelProvider
            .createFormattingModelForPsiFile(
                formattingContext.containingFile,
                LcaFileBlock(
                    formattingContext.node,
                    Wrap.createWrap(WrapType.NONE, false),
                    Alignment.createAlignment(),
                    createSpaceBuilder(formattingContext.codeStyleSettings)
                ),
                formattingContext.codeStyleSettings
            )
    }
}