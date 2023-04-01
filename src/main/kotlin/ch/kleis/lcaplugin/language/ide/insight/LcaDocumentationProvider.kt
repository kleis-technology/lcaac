package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.type.trait.BlockMetaOwner
import ch.kleis.lcaplugin.language.psi.type.unit.UnitDefinitionType
import ch.kleis.lcaplugin.psi.*
import ch.kleis.lcaplugin.psi.impl.LcaTechnoProductExchangeWithAllocateFieldImpl
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.psi.PsiElement
import com.intellij.ui.JBColor
import java.awt.Font

class LcaDocumentationProvider : AbstractDocumentationProvider() {
    private companion object {
        private var SECTION_ROW_START = "<tr>\n<td valign='top' class='section'>"
        private var SECTION_NEXT_COL = "</td>\n<td valign='top'>"
        private var SECTION_ROW_END = "</td>\n</tr>\n"
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        return super.getQuickNavigateInfo(element, originalElement)
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        return when (element) {
            is LcaSubstance -> {
                val sb = StringBuilder()
                documentTitle(sb, "Substance", element.getSubstanceRef().name)
                documentSubstanceData(sb, element)
                documentBlockMetaOwner(sb, element)
                sb.toString()
            }

            is LcaTechnoProductExchange -> {
                val sb = StringBuilder()
                val processProducer = getProcessProducer(element)
                documentProductTitle(sb, element.getProductRef(), processProducer)
                documentBlockMetaOwner(sb, processProducer)
                documentProcessParams(sb, processProducer)
                addSeparatorLine(sb)
                sb.toString()
            }

            is LcaProcess -> {
                val sb = StringBuilder()
                documentTitle(sb, "Process", element.getProcessTemplateRef().name)
                documentBlockMetaOwner(sb, element)
                documentProcessParams(sb, element)
                addSeparatorLine(sb)
                sb.toString()
            }

            is LcaQuantityRef -> {
                when (val target = element.reference.resolve()) {
                    is LcaUnitDefinition -> {
                        val sb = StringBuilder()
                        documentTitle(sb, "Unit", element.name)
                        documentUnitData(sb, target)
                        sb.toString()
                    }

                    is LcaGlobalAssignment -> {
                        val sb = StringBuilder()
                        documentTitle(sb, "Global quantity", element.name)
                        documentQuantityData(sb, target)
                        sb.toString()
                    }

                    is LcaAssignment -> {
                        val sb = StringBuilder()
                        documentTitle(sb, "Quantity", element.name)
                        documentQuantityData(sb, target)
                        sb.toString()
                    }

                    else -> super.generateDoc(element, originalElement)
                }
            }

            is LcaParameterRef -> {
                when (val target = element.reference.resolve()) {
                    is LcaAssignment -> {
                        val sb = StringBuilder()
                        documentTitle(sb, "Quantity", element.name)
                        documentQuantityData(sb, target)
                        sb.toString()
                    }
                    else -> super.generateDoc(element, originalElement)
                }
            }

            else -> super.generateDoc(element, originalElement)
        }
    }

    private fun documentName(sb: StringBuilder, name: String?) {
        if (name != null) {
            sb.append(DocumentationMarkup.CONTENT_START).append("\n")
            HtmlSyntaxInfoUtil.appendStyledSpan(sb, TextAttributes(), name, 0.5f)
            sb.append(DocumentationMarkup.CONTENT_END).append("\n")
        }
    }

    private fun documentProcessParams(sb: StringBuilder, lcaProcess: LcaProcess?) {
        sb.append(DocumentationMarkup.CONTENT_START).append("\n")
        val att = TextAttributes()
        att.foregroundColor = JBColor.GRAY
        att.fontType = Font.ITALIC
        HtmlSyntaxInfoUtil.appendStyledSpan(sb, att, "Process Parameters:", 1f)
        sb.append(DocumentationMarkup.SECTIONS_START).append("\n")
        lcaProcess?.paramsList?.flatMap { it.assignmentList }
            ?.forEach {
                addKeyValueSection("${it.name} = ", it.quantity.text, sb)
            }
        sb.append(DocumentationMarkup.SECTIONS_END).append("\n")
        sb.append(DocumentationMarkup.CONTENT_END).append("\n")
    }

    private fun documentProductTitle(sb: StringBuilder, product: LcaProductRef, process: LcaProcess?) {
        sb.append(DocumentationMarkup.DEFINITION_START).append("\n")
        val att = TextAttributes()
        att.foregroundColor = JBColor.ORANGE
        att.fontType = Font.ITALIC
        HtmlSyntaxInfoUtil.appendStyledSpan(sb, att, "Product", 1f)
        sb.append(" ")
        documentUid(sb, product.uid.name, false)
        HtmlSyntaxInfoUtil.appendStyledSpan(sb, att, " from ", 1f)
        if (process == null) {
            documentUid(sb, "unknown")
        } else {
            documentUid(sb, process.getProcessTemplateRef().name)
        }
        sb.append(DocumentationMarkup.DEFINITION_END).append("\n")
    }


    private fun documentTitle(sb: StringBuilder, type: String, name: String) {
        sb.append(DocumentationMarkup.DEFINITION_START).append("\n")
        val att = TextAttributes()
        att.foregroundColor = JBColor.ORANGE
        att.fontType = Font.ITALIC
        HtmlSyntaxInfoUtil.appendStyledSpan(sb, att, type, 1f)
        sb.append(" ")
        documentUid(sb, name)
        sb.append(DocumentationMarkup.DEFINITION_END).append("\n")
    }

    private fun documentUnitData(sb: StringBuilder, element: LcaUnitDefinition) {
        sb.append(DocumentationMarkup.CONTENT_START).append("\n")
        sb.append(DocumentationMarkup.SECTIONS_START).append("\n")
        addKeyValueSection("Symbol", element.getSymbolField().getValue(), sb)
        if (element.getType() == UnitDefinitionType.LITERAL) {
            addKeyValueSection("Dimension", element.getDimensionField().getValue(), sb)
        }
        if (element.getType() == UnitDefinitionType.ALIAS) {
            addKeyValueSection("Alias for", element.getAliasForField().getValue().text, sb)
        }
        sb.append(DocumentationMarkup.SECTIONS_END).append("\n")
        sb.append(DocumentationMarkup.CONTENT_END).append("\n")
    }

    private fun documentQuantityData(sb: StringBuilder, element: LcaGlobalAssignment) {
        sb.append(DocumentationMarkup.CONTENT_START).append("\n")
        sb.append(DocumentationMarkup.SECTIONS_START).append("\n")
        addKeyValueSection("Symbol", element.getQuantityRef().name, sb)
        addKeyValueSection("Value", element.getValue().text, sb)
        sb.append(DocumentationMarkup.SECTIONS_END).append("\n")
        sb.append(DocumentationMarkup.CONTENT_END).append("\n")
    }

    private fun documentQuantityData(sb: StringBuilder, element: LcaAssignment) {
        sb.append(DocumentationMarkup.CONTENT_START).append("\n")
        sb.append(DocumentationMarkup.SECTIONS_START).append("\n")
        addKeyValueSection("Symbol", element.getQuantityRef().name, sb)
        addKeyValueSection("Value", element.getValue().text, sb)
        sb.append(DocumentationMarkup.SECTIONS_END).append("\n")
        sb.append(DocumentationMarkup.CONTENT_END).append("\n")
    }


    private fun documentSubstanceData(sb: StringBuilder, element: LcaSubstance) {
        sb.append(DocumentationMarkup.CONTENT_START).append("\n")
        sb.append(DocumentationMarkup.SECTIONS_START).append("\n")
        addKeyValueSection("Name", element.getNameField().getValue(), sb)
        addKeyValueSection("Compartment", element.getCompartmentField().getValue(), sb)
        addKeyValueSection("Sub-Compartment", element.getSubcompartmentField()?.getValue(), sb)
        addKeyValueSection("Reference Unit", element.getReferenceUnitField().getValue().text, sb)
        sb.append(DocumentationMarkup.SECTIONS_END).append("\n")
        sb.append(DocumentationMarkup.CONTENT_END).append("\n")
    }

    private fun documentUid(sb: StringBuilder, name: String, withCR: Boolean = true) {
        val att = TextAttributes()
        att.foregroundColor = JBColor.BLUE
        att.fontType = Font.BOLD
        HtmlSyntaxInfoUtil.appendStyledSpan(sb, att, name, 0.5f)
        if (withCR) sb.append("\n")
    }

    private fun documentBlockMetaOwner(sb: StringBuilder, blockOwner: BlockMetaOwner?) {
        if (blockOwner != null) {
            val meta = blockOwner.getBlockMetaList()
                .flatMap { it.metaAssignmentList }
                .associate { Pair(it.name, it.getValue()) }
            val desc = meta["description"]
            if (desc != null) {
                sb.append(DocumentationMarkup.CONTENT_START).append("\n")
                HtmlSyntaxInfoUtil.appendStyledSpan(sb, TextAttributes(), desc.replace("\n", "<br>"), 0.5f)
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

    private fun addSeparatorLine(sb: StringBuilder) {
        sb.append(DocumentationMarkup.DEFINITION_START).append("\n")
        sb.append(DocumentationMarkup.DEFINITION_END).append("\n")
    }

    private fun getProcessProducer(element: PsiElement): LcaProcess?{
        if (element.parent is LcaTechnoProductExchangeWithAllocateFieldImpl){
            return element.parent.parent?.parent as LcaProcess?
        }
        return element.parent?.parent as LcaProcess?
    }
}
