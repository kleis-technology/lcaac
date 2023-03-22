package ch.kleis.lcaplugin.project

import ch.kleis.lcaplugin.project.libraries.EmissionFactorLibrary
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.isDirectory
import java.nio.file.Path
import java.nio.file.Paths.get
import java.util.Collections.singletonList
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

class LcaRootLibraryProvider : AdditionalLibraryRootsProvider() {
    private companion object {
        private val LOG = Logger.getInstance(LcaRootLibraryProvider::class.java)
    }
    
    private val jarRoot: VirtualFile?

    init {
        val pluginId = PluginId.getId("ch.kleis.lcaplugin")
        val plugin = PluginManagerCore.getPlugins().firstOrNull { it.pluginId == pluginId }
        val jarVirtualFile = plugin?.pluginPath?.let {
            val jarFile = if (it.isDirectory()) {
                // Case of the LCA As Code run as a plugin from Intellij
                it.resolve(get("lib", "emissions_factors.jar"))
            } else {
                // Case of the LCA As Code run as an IDE from Intellij
                it.parent.resolve("emissions_factors.jar")
            }
            val virtualFile = VfsUtil.findFile(jarFile, false)
            if (virtualFile == null) {
                LOG.error("Unable to locate LCAProvider jar files, jar File was if $jarFile")
            }
            virtualFile
        }
        jarRoot = jarVirtualFile?.let {
            JarFileSystem.getInstance().getJarRootForLocalFile(it)
        }
    }

    override fun getAdditionalProjectLibraries(project: Project): Collection<EmissionFactorLibrary> {
        return if (jarRoot != null) {
            singletonList(EmissionFactorLibrary(jarRoot, "ef31"))
        } else {
            emptyList()
        }
    }

    override fun getRootsToWatch(project: Project) = emptyList<VirtualFile>()

}
