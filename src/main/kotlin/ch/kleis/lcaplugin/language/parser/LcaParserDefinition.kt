package ch.kleis.lcaplugin.language.parser

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.psi.LcaTypes
import ch.kleis.lcaplugin.psi.LcaTypes.STRING_LITERAL
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.stubs.PsiFileStubImpl
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.psi.tree.TokenSet

class LcaParserDefinition : ParserDefinition {
    companion object {
        val FILE = IStubFileElementType<PsiFileStubImpl<LcaFile>>(LcaLanguage.INSTANCE.id, LcaLanguage.INSTANCE)
    }

    override fun createLexer(project: Project?): Lexer {
        return LcaLexerAdapter()
    }

    override fun createParser(project: Project?): PsiParser {
        return ch.kleis.lcaplugin.language.parser.LcaParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    override fun getCommentTokens(): TokenSet {
        return TokenSet.create(
            LcaTypes.COMMENT_BLOCK_START,
            LcaTypes.COMMENT_CONTENT,
            LcaTypes.COMMENT_BLOCK_END,
            LcaTypes.COMMENT_LINE
        )
    }

    override fun getStringLiteralElements(): TokenSet {
        return TokenSet.create(STRING_LITERAL)
    }

    override fun createElement(node: ASTNode?): PsiElement {
        return LcaTypes.Factory.createElement(node)
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return LcaFile(viewProvider)
    }
}
