package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.compute.matrix.ObservableFactorMatrix
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import javax.swing.JPanel

/*
    https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/tool_window
 */

class LcaResult(result: ObservableFactorMatrix) {
    private val content: JPanel = JPanel()

    init {
        val tableModel = ObservableFactorTableModel(result)
        val table = JBTable(tableModel)
        val adjuster = TableColumnAdjuster(table)
        adjuster.adjust()
        val scrollPane = JBScrollPane(table)
        content.add(scrollPane)
    }

    fun getContent(): JPanel {
        return content
    }
}
