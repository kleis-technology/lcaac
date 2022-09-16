package com.github.albanseurat.lcaplugin


import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class LcaFileType : LanguageFileType(LcaLanguage.INSTANCE) {

    companion object {
        val INSTANCE = LcaFileType()
    };

    override fun getName(): String {
        return "LCA Definition File"
    }

    override fun getDescription(): String {
        return "Life Cycle Analysis - activity definition"
    }

    override fun getDefaultExtension(): String {
        return "lca"
    }

    override fun getIcon(): Icon {
        return LcaIcons.FILE;
    }
}