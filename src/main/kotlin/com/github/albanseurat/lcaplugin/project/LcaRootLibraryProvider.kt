package com.github.albanseurat.lcaplugin.project

import com.github.albanseurat.lcaplugin.project.libraries.EmissionFactorLibrary
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.nio.charset.Charset

class LcaRootLibraryProvider : AdditionalLibraryRootsProvider() {

    override fun getAdditionalProjectLibraries(project: Project): Collection<EmissionFactorLibrary> {

        //TODO : have a look how they handle file bundled in a plugins here :
        // https://github.com/bedalton/Caos-Plugin-IntelliJ/blob/570d2326f86fa5e84deec09e900db5f201dae779/src/main/java/com/badahori/creatures/plugins/intellij/agenteering/utils/CaosFileUtil.kt
        // https://github.com/bedalton/Caos-Plugin-IntelliJ/blob/master/src/main/java/com/badahori/creatures/plugins/intellij/agenteering/caos/project/library/CaosScriptSyntheticLibrary.kt
        //TODO : simple way to have files is to zip lca files and load them with JarFileSystem.getInstance().getJarRootForLocalFile
        val emissionsFactors = this.javaClass.classLoader.getResource("META-INF/EF-LCIAMethod_CF(EF-v3.1).csv");
        val parser = CSVParser.parse(emissionsFactors.openStream(), Charset.defaultCharset(), CSVFormat.DEFAULT)
        val substances = parser.map { it[1] }.distinct().map { LightVirtualFile(it) }.toSet()
        //return singletonList(EmissionFactorLibrary(substances, "EF 3.1"))
        return emptyList()
    }

    override fun getRootsToWatch(project: Project) = emptyList<VirtualFile>()

}