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

/*
 if (mySdk != null && mySdk.getSdkType() instanceof RsSdkType) {
      setupFacet(module, mySdk);
      VirtualFile[] roots = ModuleRootManager.getInstance(module).getSourceRoots();
      if (roots.length == 1) {
        VirtualFile srcRoot = roots[0];
        if (srcRoot.getName().equals("smalltalk")) {
          VirtualFile main = srcRoot.getParent();
          if (main != null && "main".equals(main.getName())) {
            final VirtualFile src = main.getParent();
            if (src != null) {
              ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                  try {
                    VirtualFile test = src.createChildDirectory(this, "test");
                    if (test != null) {
                      VirtualFile testSrc = test.createChildDirectory(this, "smalltalk");
                      ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
                      ContentEntry entry = findContentEntry(model, testSrc);
                      if (entry != null) {
                        entry.addSourceFolder(testSrc, true);
                        model.commit();
                      }
                    }
                  } catch (IOException e) {//
                  }
                }
              });
            }
          }
        }
      }
 */