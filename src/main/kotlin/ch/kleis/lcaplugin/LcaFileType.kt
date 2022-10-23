package ch.kleis.lcaplugin


import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class LcaFileType : LanguageFileType(ch.kleis.lcaplugin.LcaLanguage.Companion.INSTANCE) {

    companion object {
        val INSTANCE = ch.kleis.lcaplugin.LcaFileType()
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
        return ch.kleis.lcaplugin.LcaIcons.FILE;
    }
}
