package ch.kleis.lcaplugin.language.ide.syntax

import ch.kleis.lcaplugin.psi.LcaEmissions
import ch.kleis.lcaplugin.psi.LcaInputs
import ch.kleis.lcaplugin.psi.LcaMetadata
import ch.kleis.lcaplugin.psi.LcaProducts
import ch.kleis.lcaplugin.psi.LcaResources
import ch.kleis.lcaplugin.psi.LcaTypes
import ch.kleis.lcaplugin.psi.LcaTypes.PROCESS
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType

class LcaFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean):
            Array<FoldingDescriptor> {

        val descriptors: MutableList<FoldingDescriptor> = ArrayList()

        val lcaProcessDefinitions: Collection<PsiElement> =
            PsiTreeUtil.findChildrenOfAnyType(
                root,
                ch.kleis.lcaplugin.psi.LcaProcess::class.java,
                ch.kleis.lcaplugin.psi.LcaProducts::class.java,
                ch.kleis.lcaplugin.psi.LcaInputs::class.java,
                ch.kleis.lcaplugin.psi.LcaResources::class.java,
                ch.kleis.lcaplugin.psi.LcaEmissions::class.java,
                ch.kleis.lcaplugin.psi.LcaMetadata::class.java,
                ch.kleis.lcaplugin.psi.LcaFactors::class.java
            )

        for (definition in lcaProcessDefinitions) {
            val braces = PsiTreeUtil.collectElements(definition)
            { e -> e.elementType == LcaTypes.LBRACE || e.elementType == LcaTypes.RBRACE }

            if (braces.size > 1) {
                descriptors.add(
                    FoldingDescriptor(
                        definition,
                        TextRange(braces[0].textOffset, braces[braces.size - 1].textOffset + 1)
                    )
                )
            }
        }
        return descriptors.toTypedArray();

    }

    override fun getPlaceholderText(node: ASTNode): String = "..."

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false;
    }
}
