package ch.kleis.lcaplugin.project

import ch.kleis.lcaplugin.project.libraries.EmissionFactorLibrary
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.isDirectory
import java.nio.file.Paths.get

data class AdditionalLib(val alias: String, val jarName: String)

class LcaRootLibraryProvider : AdditionalLibraryRootsProvider() {
    private companion object {
        private val LOG = Logger.getInstance(LcaRootLibraryProvider::class.java)
    }

    private val additionalJars: Collection<EmissionFactorLibrary>

    init {
        val pluginId = PluginId.getId("ch.kleis.lcaplugin")
        val plugin = PluginManagerCore.getPlugins().firstOrNull { it.pluginId == pluginId }
        additionalJars =
            listOf(
                AdditionalLib("ef31", "emissions_factors3.1.jar"),
                AdditionalLib("ef30", "emissions_factors3.0.jar")
            ).mapNotNull { getEmissionFactorLib(it, plugin) }

    }

    fun getEmissionFactorLib(lib: AdditionalLib, plugin: IdeaPluginDescriptor?): EmissionFactorLibrary? {
        val jarVirtualFile = plugin?.pluginPath?.let {
            val jarFile = if (it.isDirectory()) {
                // Case of the LCA As Code run as a plugin from Intellij
                it.resolve(get("lib", lib.jarName))
            } else {
                // Case of the LCA As Code run as an IDE from Intellij
                it.parent.resolve(lib.jarName)
            }
            val virtualFile = VfsUtil.findFile(jarFile, false)
            if (virtualFile == null) {
                LOG.error("Unable to locate LCAProvider jar files, jar File was if $jarFile")
            }
            virtualFile
        }
        val jarRoot = jarVirtualFile?.let {
            JarFileSystem.getInstance().getJarRootForLocalFile(it)
        }
        return jarRoot?.let {
            EmissionFactorLibrary(it, lib.alias)
        }
    }

    override fun getAdditionalProjectLibraries(project: Project): Collection<EmissionFactorLibrary> {
        return additionalJars
    }

    override fun getRootsToWatch(project: Project) = emptyList<VirtualFile>()

}
