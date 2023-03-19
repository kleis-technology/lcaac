package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.type.trait.BlockMetaOwner
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import ch.kleis.lcaplugin.psi.LcaSubstance
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.ui.JBColor
import java.awt.Font

class LcaBioExchangeDocumentationProvider : AbstractDocumentationProvider() {
    var SECTION_ROW_START = "<tr>\n<td valign='top' class='section'>"
    var SECTION_NEXT_COL = "</td>\n<td valign='top'>"
    var SECTION_ROW_END = "</td>\n</tr>\n"
//    var SECTION_END = "</td>"

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        return super.getQuickNavigateInfo(element, originalElement)
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        return if (element is LcaSubstance) {
            val sb = StringBuilder()
            documentTitle(sb, element)
            documentBlockMetaOwner(sb, element)
            documentSubstanceData(sb, element)
            sb.toString()

        } else {
            super.generateDoc(element, originalElement)
        }
    }

    private fun documentTitle(sb: StringBuilder, psiUIDOwner: PsiUIDOwner) {
        sb.append(DocumentationMarkup.DEFINITION_START).append("\n")
        val att = TextAttributes()
        att.foregroundColor = JBColor.ORANGE
        att.fontType = Font.ITALIC
        HtmlSyntaxInfoUtil.appendStyledSpan(sb, att, "Substance ", 1f)
        documentUid(sb, psiUIDOwner)
        sb.append(DocumentationMarkup.DEFINITION_END).append("\n")
    }

    private fun documentSubstanceData(sb: StringBuilder, element: LcaSubstance) {
        sb.append(DocumentationMarkup.CONTENT_START).append("\n")
        sb.append(DocumentationMarkup.SECTIONS_START).append("\n")
        addKeyValueSection("Compartment", element.getCompartmentField().getValue(), sb)
        addKeyValueSection("Sub-Compartment", element.getSubcompartmentField()?.getValue(), sb)
        addKeyValueSection("Reference Unit", element.getReferenceUnitField().getValue().text, sb)
        sb.append(DocumentationMarkup.SECTIONS_END).append("\n")
        sb.append(DocumentationMarkup.CONTENT_END).append("\n")
    }

    private fun documentUid(sb: StringBuilder, elt: PsiUIDOwner) {
        val att = TextAttributes()
        att.foregroundColor = JBColor.BLUE
        att.fontType = Font.BOLD
        HtmlSyntaxInfoUtil.appendStyledSpan(sb, att, elt.name, 0.5f)
        sb.append("\n")
    }

    private fun documentBlockMetaOwner(sb: StringBuilder, blockOwner: BlockMetaOwner) {
        val meta = blockOwner.getBlockMetaList()
            .flatMap { it.metaAssignmentList }
            .associate { Pair(it.name, it.getValue()) }
        val desc = meta["description"]
        if (desc != null) {
            sb.append(DocumentationMarkup.CONTENT_START).append("\n")
            HtmlSyntaxInfoUtil.appendStyledSpan(sb, TextAttributes(), desc, 0.5f)
            sb.append(DocumentationMarkup.CONTENT_END).append("\n")
        }
        val author = meta["author"]
        if (author != null) {
            sb.append(DocumentationMarkup.CONTENT_START).append("\n")
            sb.append(DocumentationMarkup.SECTIONS_START).append("\n")
            addKeyValueSection("Author", author, sb)
            sb.append(DocumentationMarkup.SECTIONS_END).append("\n")
            sb.append(DocumentationMarkup.CONTENT_END).append("\n")
        }
    }

    private fun addKeyValueSection(key: String, value: String?, sb: StringBuilder) {
        if (value != null) {
            sb.append(SECTION_ROW_START)
            sb.append(key)
            sb.append(SECTION_NEXT_COL)
            sb.append(value)
            sb.append(SECTION_ROW_END)
        }
    }
}