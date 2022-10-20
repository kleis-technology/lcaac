package com.github.albanseurat.lcaplugin.language

import com.github.albanseurat.lcaplugin.language.psi.type.Product
import com.github.albanseurat.lcaplugin.language.psi.type.Substance
import com.github.albanseurat.lcaplugin.language.psi.stub.ProductKeyIndex
import com.github.albanseurat.lcaplugin.language.psi.stub.SubstanceKeyIndex
import com.intellij.openapi.project.Project


interface SearchTrait {

    fun findProducts(project: Project, name: String): Collection<Product> =
        ProductKeyIndex.findProducts(project, name)

    // TODO : cache results for performance (maybe)
    fun findSubstances(project: Project, name: String): Collection<Substance> =
        SubstanceKeyIndex.findSubstances(project, name)

}