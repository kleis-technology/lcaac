package com.github.albanseurat.lcaplugin.services

import com.intellij.openapi.project.Project
import tech.units.indriya.function.Calculus
import tech.units.indriya.function.DefaultNumberSystem

class LcaProjectService(project: Project) {

    init {
        Calculus.setCurrentNumberSystem(DefaultNumberSystem())
    }
}
