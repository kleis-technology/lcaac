package com.github.albanseurat.lcaplugin.project.libraries

import com.github.albanseurat.lcaplugin.LcaIcons
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

class EmissionFactorLibrary(
    val root: VirtualFile,
    private val name: String
) : SyntheticLibrary(), ItemPresentation {

    override fun equals(other: Any?): Boolean =
        other is EmissionFactorLibrary && other.root == root

    override fun hashCode(): Int = root.hashCode()

    override fun getSourceRoots(): Collection<VirtualFile> = emptyList()

    override fun getBinaryRoots(): Collection<VirtualFile> = listOf(root)

    override fun getLocationString(): String? =
        null

    override fun getIcon(unused: Boolean): Icon =
        LcaIcons.FILE

    override fun getPresentableText(): String = name

}