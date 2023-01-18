package ch.kleis.lcaplugin.language.ide.structure

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.LcaFile
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
        return if (type is ch.kleis.lcaplugin.psi.LcaProcess) {
            return type.name
        } else null

    }

    override fun getIcon(type: Any?): Icon? {
        return when (type) {
            is LcaFile -> {
                AllIcons.Nodes.Module
            }

            is ch.kleis.lcaplugin.psi.LcaProcess -> {
                AllIcons.Nodes.Class
            }

            else -> null
        }
    }
}
