package com.github.albanseurat.lcaplugin.project

import com.github.albanseurat.lcaplugin.project.libraries.EmissionFactorLibrary
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.testFramework.LightVirtualFile
import java.util.Collections.singletonList

class LcaRootLibraryProvider : AdditionalLibraryRootsProvider() {

    override fun getAdditionalProjectLibraries(project: Project): Collection<EmissionFactorLibrary> {




        return singletonList(EmissionFactorLibrary(LightVirtualFile("test"), "EF 3.1"))
    }

    override fun getRootsToWatch(project: Project) =
        getAdditionalProjectLibraries(project).map { it.root }
}