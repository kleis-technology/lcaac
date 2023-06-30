package ch.kleis.lcaplugin.project

import ch.kleis.lcaplugin.project.libraries.EmissionFactorLibrary
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.util.io.StreamUtil
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.createDirectories
import com.intellij.util.io.isDirectory
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths.get
import kotlin.io.path.notExists

data class AdditionalLib(val alias: String, val jarName: String)

class LcaRootLibraryProvider : AdditionalLibraryRootsProvider() {
    private companion object {
        private val LOG = Logger.getInstance(LcaRootLibraryProvider::class.java)
    }

    private val additionalJars: Collection<EmissionFactorLibrary>

    init {
        val pluginId = PluginId.getId("ch.kleis.lcaplugin.main")
        val plugin = PluginManagerCore.getPlugins().firstOrNull { it.pluginId == pluginId }
        additionalJars =
            listOf(
                AdditionalLib("ef31", "emissions_factors3.1.jar"),
                AdditionalLib("ef30", "emissions_factors3.0.jar")
            ).mapNotNull { getEmissionFactorLib(it, plugin) }

    }

    private fun getEmissionFactorLib(lib: AdditionalLib, plugin: IdeaPluginDescriptor?): EmissionFactorLibrary? {
        val jarVirtualFile = plugin?.pluginPath?.let {
            val jarFile = if (it.isDirectory()) {
                // Case of the LCA As Code run as a plugin from Intellij
                it.resolve(get("lib", lib.jarName))
            } else {
                // Case of the LCA As Code run as an IDE from Intellij as a Gradle Project
                it.parent.resolve(lib.jarName)
            }

            val virtualFile =
                VfsUtil.findFile(jarFile, false) ?: extractLibToFolder(lib)

            if (virtualFile == null) {
                LOG.error("Unable to locate LCAProvider jar files, jar File was $jarFile")
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

    // Case of the LCA As Code run as an installed IDE
    private fun extractLibToFolder(lib: AdditionalLib): VirtualFile? {
        val targetFolder =
            Path.of(PathManager.getDefaultPluginPathFor("LcaAsCode2023.1") + File.separatorChar + "lca-as-code")
        if (targetFolder.notExists()) targetFolder.createDirectories()
        val targetFile = Path.of(targetFolder.toString() + File.separatorChar + lib.jarName)
        if (targetFile.notExists()) {
            FileOutputStream(targetFile.toFile()).use { target ->
                this.javaClass.getResourceAsStream("/${lib.jarName}").use { src ->
                    StreamUtil.copy(src!!, target)
                }
            }
        }
        return VfsUtil.findFile(targetFile, false)
    }

    override fun getAdditionalProjectLibraries(project: Project): Collection<EmissionFactorLibrary> {
        return additionalJars
    }

    override fun getRootsToWatch(project: Project) = emptyList<VirtualFile>()

}
