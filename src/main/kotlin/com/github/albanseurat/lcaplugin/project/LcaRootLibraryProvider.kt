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
import java.util.Collections.singletonList

class LcaRootLibraryProvider : AdditionalLibraryRootsProvider() {

    val PLUGIN_ID = "com.github.albanseurat.lcaplugin"

    private val RESOURCES_FOLDER = "classes"

    val PLUGIN: IdeaPluginDescriptor?
        get() {
            val pluginId = PluginId.getId(PLUGIN_ID)
            return PluginManagerCore.getPlugins().firstOrNull { it.pluginId == pluginId }
        }

    private val PLUGIN_HOME_FILE: Path?
        get() = PLUGIN?.pluginPath


    val PLUGIN_HOME_DIRECTORY: VirtualFile?
        get() {
            val path = PLUGIN_HOME_FILE ?: return null
            val libFolder = VfsUtil.findFile(path, false)?.findChild("lib")
                    ?: return DEBUG_PLUGIN_HOME_DIRECTORY
            val jar = libFolder.children.firstOrNull {
                it.name.startsWith("CaosPlugin") && it.extension == "jar"
            } ?: return DEBUG_PLUGIN_HOME_DIRECTORY
            return JarFileSystem.getInstance().getJarRootForLocalFile(jar)
                    ?: DEBUG_PLUGIN_HOME_DIRECTORY
        }

    private val DEBUG_PLUGIN_HOME_DIRECTORY: VirtualFile?
        get() {
            val path = PLUGIN_HOME_FILE ?: return null
            return VfsUtil.findFile(path, true)?.findChild(RESOURCES_FOLDER)
        }

    private val PLUGIN_RESOURCES_DIRECTORY: VirtualFile?
        get() = PLUGIN_HOME_DIRECTORY

    fun getPluginResourceFile(relativePath: String): VirtualFile? {
        return PLUGIN_RESOURCES_DIRECTORY?.findFileByRelativePath(relativePath)
    }


    override fun getAdditionalProjectLibraries(project: Project): Collection<EmissionFactorLibrary> {

        val jarFile = PLUGIN?.pluginPath?.resolve(Paths.get("lib", "substances.jar"))
        val jarVirtualFile = jarFile?.let { VfsUtil.findFile(it, false) }
        val jarRoot = jarVirtualFile?.let {
            JarFileSystem.getInstance().getJarRootForLocalFile(it)
        }

        return if(jarRoot != null) {
            singletonList(EmissionFactorLibrary(jarRoot, "EF 3.1"))
        } else {
            emptyList()
        }
    }

    override fun getRootsToWatch(project: Project) = emptyList<VirtualFile>()

}