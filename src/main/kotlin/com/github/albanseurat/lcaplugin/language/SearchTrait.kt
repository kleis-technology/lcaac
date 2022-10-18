package com.github.albanseurat.lcaplugin.language

import com.github.albanseurat.lcaplugin.LcaFileType.Companion.INSTANCE
import com.github.albanseurat.lcaplugin.language.psi.LcaFile
import com.github.albanseurat.lcaplugin.language.psi.Product
import com.github.albanseurat.lcaplugin.language.psi.Substance
import com.github.albanseurat.lcaplugin.language.psi.stub.ProductKeyIndex
import com.github.albanseurat.lcaplugin.language.psi.stub.SubstanceKeyIndex
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager.getInstance
import com.intellij.psi.search.FileTypeIndex.getFiles
import com.intellij.psi.search.GlobalSearchScope.allScope
import com.intellij.psi.util.PsiTreeUtil.findChildrenOfType


interface SearchTrait {

    fun findProducts(project: Project, name: String): Collection<Product> =
        ProductKeyIndex.findProducts(project, name)

    // TODO : cache results for performance (maybe)
    fun findSubstances(project: Project, name: String): Collection<Substance> =
        SubstanceKeyIndex.findSubstances(project, name)

}