package ch.kleis.lcaplugin.ui.toolwindow.sensitivity_analysis

import ch.kleis.lcaplugin.core.assessment.SensitivityAnalysis
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.math.dual.DualNumber
import ch.kleis.lcaplugin.ui.toolwindow.LcaToolWindowContent
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBEmptyBorder
import java.awt.BorderLayout
import javax.swing.JPanel

class SensitivityAnalysisWindow(
    analysis: SensitivityAnalysis,
    observablePortComparator: Comparator<MatrixColumnIndex<DualNumber>>,
    val project: Project,
    val name: String,
) : LcaToolWindowContent {
    private val content: JPanel

    init {
        val tableModel = SensitivityTableModel(analysis, observablePortComparator)
        val table = JBTable(tableModel)
        table.autoCreateRowSorter = true
        val defaultScrollPane = JBScrollPane(table)
        defaultScrollPane.border = JBEmptyBorder(0)
        content = JPanel(BorderLayout())
        content.add(defaultScrollPane, BorderLayout.CENTER)
        content.updateUI()
    }

    override fun getContent(): JPanel {
        return content
    }
}
