package ch.kleis.lcaplugin.ui.toolwindow.sensitivity_analysis

import ch.kleis.lcaplugin.core.assessment.SensitivityAnalysis
import ch.kleis.lcaplugin.core.lang.value.ProductValue
import ch.kleis.lcaplugin.core.math.dual.DualNumber
import ch.kleis.lcaplugin.ui.toolwindow.FloatingPointRepresentation
import ch.kleis.lcaplugin.ui.toolwindow.LcaToolWindowContent
import ch.kleis.lcaplugin.ui.toolwindow.WithHeaderTransferableHandler
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer

class SensitivityAnalysisWindow(
    analysis: SensitivityAnalysis,
    val project: Project,
    val name: String,
) : LcaToolWindowContent {
    private val content: JPanel
    private val table = JBTable()

    init {
        /*
            Table pane
         */

        val firstProduct = analysis.getEntryPointProducts().first()
        val sensitivityTableModel = SensitivityTableModel(analysis, firstProduct)
        val transposedSensitivityTableModel = TransposedSensitivityTableModel(analysis, firstProduct)

        table.model = sensitivityTableModel
        table.autoCreateRowSorter = true
        table.transferHandler = WithHeaderTransferableHandler()

        val cellRenderer = DefaultTableCellRenderer()
        cellRenderer.horizontalAlignment = JLabel.RIGHT
        table.setDefaultRenderer(FloatingPointRepresentation::class.java, cellRenderer)

        val tablePane = JBScrollPane(table)
        tablePane.border = JBEmptyBorder(0)

        /*
            Menu bar
         */
        val comboBox = ComboBox<ProductValue<DualNumber>>()
        analysis.getEntryPointProducts().forEach { comboBox.addItem(it) }
        comboBox.addActionListener {
            if (it.actionCommand == "comboBoxChanged") {
                @Suppress("UNCHECKED_CAST")
                val target = comboBox.selectedItem as ProductValue<DualNumber>
                sensitivityTableModel.target = target
                transposedSensitivityTableModel.target = target
            }
            table.updateUI()
        }
        comboBox.renderer = ListCellRenderer { _, value, _, _, _ ->
            JBLabel(value.getShortName())
        }

        val button = JButton(AllIcons.Actions.BuildLoadChanges)
        button.addActionListener {
            table.model = when (table.model) {
                sensitivityTableModel -> transposedSensitivityTableModel
                transposedSensitivityTableModel -> sensitivityTableModel
                else -> sensitivityTableModel
            }
            table.updateUI()
        }
        button.margin = JBUI.emptyInsets()

        val menuBar = JMenuBar()
        menuBar.add(JBLabel("Sensitivity analysis"))
        menuBar.add(JBBox.createHorizontalGlue())
        menuBar.add(comboBox, BorderLayout.LINE_END)
        menuBar.add(button, BorderLayout.LINE_END)

        /*
            Content
         */
        content = JPanel(BorderLayout())
        content.add(menuBar, BorderLayout.PAGE_START)
        content.add(tablePane, BorderLayout.CENTER)
        content.updateUI()
    }

    override fun getContent(): JPanel {
        return content
    }
}
