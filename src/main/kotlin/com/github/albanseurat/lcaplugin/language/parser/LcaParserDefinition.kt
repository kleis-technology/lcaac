package com.github.albanseurat.lcaplugin.language.parser

import com.github.albanseurat.lcaplugin.LcaLanguage
import com.github.albanseurat.lcaplugin.language.psi.LcaFile
import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.github.albanseurat.lcaplugin.psi.LcaTypes.STRING
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class LcaParserDefinition : ParserDefinition {
    companion object {
        val FILE = IFileElementType(LcaLanguage.INSTANCE)
    }

    override fun createLexer(project: Project?): Lexer {
        return LcaLexerAdapter()
    }

    override fun createParser(project: Project?): PsiParser {
        return LcaParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    override fun getCommentTokens(): TokenSet {
        return TokenSet.EMPTY
    }

    override fun getStringLiteralElements(): TokenSet {
        return TokenSet.create(STRING)
    }

    override fun createElement(node: ASTNode?): PsiElement {
        return LcaTypes.Factory.createElement(node)
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return LcaFile(viewProvider)
    }
}
