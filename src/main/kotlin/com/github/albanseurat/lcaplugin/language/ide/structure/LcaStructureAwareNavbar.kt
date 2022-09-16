package com.github.albanseurat.lcaplugin.language.ide.structure

import com.github.albanseurat.lcaplugin.LcaLanguage
import com.github.albanseurat.lcaplugin.language.psi.LcaFile
import com.github.albanseurat.lcaplugin.psi.LcaDatasetDefinition
import com.intellij.icons.AllIcons
import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension
import com.intellij.lang.Language
import javax.swing.Icon

class LcaStructureAwareNavbar : StructureAwareNavBarModelExtension() {

    override val language: Language
        get() = LcaLanguage.INSTANCE

    override fun getPresentableText(type: Any?): String? {
        if (type is LcaFile) {
            return type.name
        }
        return if (type is LcaDatasetDefinition) {
            return type.name
        } else null

    }

    override fun getIcon(type: Any?): Icon? {
        return if (type is LcaFile) {
            AllIcons.Nodes.Module
        } else if (type is LcaDatasetDefinition ) {
            AllIcons.Nodes.Class
        } else null
    }
}