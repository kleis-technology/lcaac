package com.github.albanseurat.lcaplugin.services

import com.intellij.openapi.project.Project
import com.github.albanseurat.lcaplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
