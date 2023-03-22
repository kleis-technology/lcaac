package ch.kleis.lcaplugin.language.psi.type.trait

import ch.kleis.lcaplugin.psi.LcaBlockMeta
import org.jetbrains.annotations.NotNull

interface BlockMetaOwner {
    @NotNull
    fun getBlockMetaList(): List<LcaBlockMeta>
}