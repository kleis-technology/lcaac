package ch.kleis.lcaplugin.project

import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.project.libraries.LcaLibrary
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.roots.SyntheticLibrary
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
import kotlin.io.path.exists
import kotlin.io.path.notExists

data class AdditionalLib(val alias: String, val jarName: String)

class LcaRootLibraryProvider : AdditionalLibraryRootsProvider() {
    private companion object {
        private val LOG = Logger.getInstance(LcaRootLibraryProvider::class.java)
    }

    private val additionalJars: Collection<LcaLibrary>

    init {
        val pluginId = PluginId.getId("ch.kleis.lcaplugin.main")
        val plugin = PluginManagerCore.getPlugins().firstOrNull { it.pluginId == pluginId }
        additionalJars =
            listOfNotNull(
                getEmissionFactorLib(AdditionalLib("ef31", "emissions_factors3.1.jar"), plugin),
                getEmissionFactorLib(AdditionalLib("ef30", "emissions_factors3.0.jar"), plugin),
                getUnitLibrary(plugin)
            )
    }

    private fun getUnitLibrary(plugin: IdeaPluginDescriptor?): LcaLibrary {
        val version: String = plugin?.version ?: "unknown"
        val jarName = "built_in_units-$version.jar"
        val folder = cacheFolder()
        val fullPath = Path.of(folder.toString(), jarName)
        val generator = UnitLcaFileFromPreludeGenerator<BasicNumber>()
        generator.recreate(fullPath, version)
        val virtualFile = VfsUtil.findFile(fullPath, false)
        return LcaLibrary(virtualFile!!, "built_in_units.jar")
    }


    private fun getEmissionFactorLib(lib: AdditionalLib, plugin: IdeaPluginDescriptor?): LcaLibrary? {
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
            LcaLibrary(it, lib.alias)
        }
    }

    // Case of the LCA As Code run as an installed IDE
    private fun extractLibToFolder(lib: AdditionalLib): VirtualFile? {
        // TODO Remove cleaning after next deployment ie after august 2023
        val oldPath = Path.of(PathManager.getDefaultPluginPathFor("LcaAsCode1.x")).parent
        if (oldPath.exists()) oldPath.toFile().deleteRecursively()
        val targetFolder = cacheFolder()
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

    private fun cacheFolder(): Path {
        val targetFolder =
            Path.of(PathManager.getDefaultPluginPathFor("CacheLcaAsCode1.x") + File.separatorChar + "lca-as-code")
        if (targetFolder.notExists()) targetFolder.createDirectories()
        return targetFolder
    }

    override fun getAdditionalProjectLibraries(project: Project): Collection<SyntheticLibrary> {
        return additionalJars
    }

    override fun getRootsToWatch(project: Project) = emptyList<VirtualFile>()

}
