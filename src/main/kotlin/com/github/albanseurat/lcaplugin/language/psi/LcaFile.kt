package com.github.albanseurat.lcaplugin.language.psi

import com.github.albanseurat.lcaplugin.LcaFileType
import com.github.albanseurat.lcaplugin.LcaLanguage
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class LcaFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LcaLanguage.INSTANCE) {
    override fun getFileType(): FileType {
        return LcaFileType.INSTANCE
    }

    override fun toString(): String {
        return "Lca File"
    }
}
