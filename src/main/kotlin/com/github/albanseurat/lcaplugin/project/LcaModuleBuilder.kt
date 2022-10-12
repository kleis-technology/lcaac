package com.github.albanseurat.lcaplugin.project

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleBuilderListener
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ModifiableRootModel

class LcaModuleBuilder : ModuleBuilder(), ModuleBuilderListener {

    init {
        addListener(this)
    }

    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
        doAddContentEntry(modifiableRootModel)
    }


    override fun getModuleType(): ModuleType<*> {
        return LcaModuleType.getInstance()
    }

    override fun moduleCreated(module: Module) {

    }
}
