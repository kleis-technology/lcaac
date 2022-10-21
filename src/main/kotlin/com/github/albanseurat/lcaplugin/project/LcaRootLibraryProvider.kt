package com.github.albanseurat.lcaplugin.project

import com.github.albanseurat.lcaplugin.project.libraries.EmissionFactorLibrary
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Paths.get
import java.util.Collections.singletonList

class LcaRootLibraryProvider() : AdditionalLibraryRootsProvider() {

    private val jarRoot: VirtualFile?
    init {
        val pluginId = PluginId.getId("com.github.albanseurat.lcaplugin")
        val plugin = PluginManagerCore.getPlugins().firstOrNull { it.pluginId == pluginId }
        val jarFile = plugin?.pluginPath?.resolve(get("lib", "substances.jar"))
        val jarVirtualFile = jarFile?.let {
            VfsUtil.findFile(it, false)
        }
        jarRoot = jarVirtualFile?.let {
            JarFileSystem.getInstance().getJarRootForLocalFile(it)
        }
    }

    override fun getAdditionalProjectLibraries(project: Project): Collection<EmissionFactorLibrary> {
        return if (jarRoot != null) {
            singletonList(EmissionFactorLibrary(jarRoot, "EF 3.1"))
        } else {
            emptyList()
        }
    }

    override fun getRootsToWatch(project: Project) = emptyList<VirtualFile>()

}