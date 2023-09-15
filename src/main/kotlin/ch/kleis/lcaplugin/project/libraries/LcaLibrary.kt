package ch.kleis.lcaplugin.project.libraries

import ch.kleis.lcaplugin.LcaIcons
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

class LcaLibrary(
    val libFile: VirtualFile,
    private val name: String
) : SyntheticLibrary(), ItemPresentation {

    override fun equals(other: Any?): Boolean =
        other is LcaLibrary && other.libFile == libFile

    override fun hashCode(): Int = libFile.hashCode()

    override fun getSourceRoots(): Collection<VirtualFile> = libFile.children.toList()

    override fun getBinaryRoots(): Collection<VirtualFile> = emptyList()

    override fun getLocationString(): String = libFile.presentableName

    override fun getIcon(unused: Boolean): Icon = LcaIcons.FILE

    override fun getPresentableText(): String = name

}
