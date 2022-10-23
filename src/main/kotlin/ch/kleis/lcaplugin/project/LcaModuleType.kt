package ch.kleis.lcaplugin.project

import ch.kleis.lcaplugin.LcaIcons
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import javax.swing.Icon


class LcaModuleType : ModuleType<LcaModuleBuilder>(ID) {

    companion object {
        val ID = "LCA_MODULE_TYPE";

        fun getInstance(): LcaModuleType {
            return ModuleTypeManager.getInstance().findByID(ID) as LcaModuleType
        }
    }

    override fun createModuleBuilder(): LcaModuleBuilder {
        return LcaModuleBuilder()
    }

    override fun getName(): String {
        return "LCA Module Type";
    }

    override fun getDescription(): String {
        return "This module allow to create dataset using LCA methodology"
    }

    override fun getNodeIcon(isOpened: Boolean): Icon {
        return LcaIcons.PROJECT;
    }

}
