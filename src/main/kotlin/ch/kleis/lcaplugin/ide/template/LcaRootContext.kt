package ch.kleis.lcaplugin.ide.template

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiFile


@Suppress("DialogTitleCapitalization")
class LcaRootContext : TemplateContextType("LCARoot", "LCA File Root") {


    override fun isInContext(ctx: TemplateActionContext): Boolean {
        return ctx.file.name.endsWith(".lca") && isInRootBlock(ctx.file, ctx.startOffset)
    }

    private fun isInRootBlock(file: PsiFile, startOffset: Int): Boolean {
        val elt = file.findElementAt(startOffset)
        return ErrorHelper.isInErrorInRootBlock(elt)
    }

}