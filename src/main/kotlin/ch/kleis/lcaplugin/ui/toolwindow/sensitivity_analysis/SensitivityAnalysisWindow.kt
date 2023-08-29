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
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.table.TableModel

class SensitivityAnalysisWindow(
    analysis: SensitivityAnalysis,
    observablePortComparator: Comparator<MatrixColumnIndex<DualNumber>>,
    val project: Project,
    val name: String,
) : LcaToolWindowContent {
    private val content: JPanel
    private val table = JBTable()

    init {
        val sensitivityTableModel = SensitivityTableModel(analysis, observablePortComparator)
        val transposedSensitivityTableModel = TransposedSensitivityTableModel(analysis, observablePortComparator)

        table.model = sensitivityTableModel
        table.autoCreateRowSorter = true

        table.addKeyListener(TransposeListener(sensitivityTableModel, transposedSensitivityTableModel, table))
        table.toolTipText = "Press 't' to transpose data."

        val defaultScrollPane = JBScrollPane(table)
        defaultScrollPane.border = JBEmptyBorder(0)
        content = JPanel(BorderLayout())
        content.add(defaultScrollPane, BorderLayout.CENTER)
        content.updateUI()
    }

    override fun getContent(): JPanel {
        return content
    }

    private class TransposeListener(
        private val model1: TableModel,
        private val model2: TableModel,
        val table: JBTable,
    ) : KeyAdapter() {
        override fun keyReleased(e: KeyEvent?) {
            if (e?.keyChar == 't') {
                if (table.model == model1) {
                    table.model = model2
                } else if (table.model == model2) {
                    table.model = model1
                }
                table.updateUI()
            }
        }
    }
}
