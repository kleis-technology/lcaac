package com.github.albanseurat.lcaplugin.language

import com.github.albanseurat.lcaplugin.LcaFileType.Companion.INSTANCE
import com.github.albanseurat.lcaplugin.language.psi.LcaFile
import com.github.albanseurat.lcaplugin.language.psi.Product
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager.getInstance
import com.intellij.psi.search.FileTypeIndex.getFiles
import com.intellij.psi.search.GlobalSearchScope.allScope
import com.intellij.psi.util.PsiTreeUtil.findChildrenOfType


interface SearchTrait {

    fun findProducts(project: Project, name: String): List<Product> {
        val psiManager = getInstance(project)
        return getFiles(INSTANCE, allScope(project)).map { psiManager.findFile(it) as LcaFile }
            .flatMap { findChildrenOfType(it, Product::class.java) }
            .filter { it.name == name }
    }

}