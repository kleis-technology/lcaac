package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.compute.matrix.InventoryMatrix
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBEmptyBorder
import java.awt.BorderLayout
import javax.swing.JPanel

/*
    https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/tool_window
 */

class LcaResult(result: InventoryMatrix) {
    private val content: JPanel

    init {
        val tableModel = InventoryTableModel(result)
        val table = JBTable(tableModel)
        val defaultScrollPane = JBScrollPane(table)
        defaultScrollPane.border = JBEmptyBorder(0)
        content = JPanel(BorderLayout())
        content.add(defaultScrollPane, BorderLayout.CENTER)
        content.updateUI()
    }

    fun getContent(): JPanel {
        return content
    }
}
