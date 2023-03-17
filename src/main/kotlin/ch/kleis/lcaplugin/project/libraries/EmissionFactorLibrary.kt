package ch.kleis.lcaplugin.project.libraries

import ch.kleis.lcaplugin.LcaIcons
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

class EmissionFactorLibrary(
    val substances: VirtualFile,
    private val name: String
) : SyntheticLibrary(), ItemPresentation {

    override fun equals(other: Any?): Boolean =
        other is EmissionFactorLibrary && other.substances == substances

    override fun hashCode(): Int = substances.hashCode()

    override fun getSourceRoots(): Collection<VirtualFile> = substances.children.toList()

    override fun getBinaryRoots(): Collection<VirtualFile> = emptyList()

    override fun getLocationString(): String = substances.presentableName

    override fun getIcon(unused: Boolean): Icon = LcaIcons.FILE

    override fun getPresentableText(): String = name

}
