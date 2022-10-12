package com.github.albanseurat.lcaplugin.project

import com.github.albanseurat.lcaplugin.project.libraries.EmissionFactorLibrary
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.nio.charset.Charset
import java.util.Collections.singletonList

class LcaRootLibraryProvider : AdditionalLibraryRootsProvider() {

    override fun getAdditionalProjectLibraries(project: Project): Collection<EmissionFactorLibrary> {
        val emissionsFactors = this.javaClass.classLoader.getResource("META-INF/EF-LCIAMethod_CF(EF-v3.1).csv");
        val parser = CSVParser.parse(emissionsFactors.openStream(), Charset.defaultCharset(), CSVFormat.DEFAULT)
        val substances = parser.map { it[1] }.distinct().map { LightVirtualFile(it) }.toSet()
        return singletonList(EmissionFactorLibrary(substances, "EF 3.1"))   
    }

    override fun getRootsToWatch(project: Project) = emptyList<VirtualFile>()

}