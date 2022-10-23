package ch.kleis.lcaplugin.listeners

import ch.kleis.lcaplugin.services.LcaProjectService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

internal class MyProjectManagerListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        project.service<LcaProjectService>()
    }
}
