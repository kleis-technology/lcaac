package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.core.matrix.InventoryError
import ch.kleis.lcaplugin.core.matrix.InventoryMatrix
import ch.kleis.lcaplugin.core.matrix.InventoryResult
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBEmptyBorder
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.SwingConstants

/*
    https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/tool_window
 */

class LcaResult(result: InventoryResult) {
    private val content: JPanel

    init {
        when (result) {
            is InventoryError -> {
                val label = JBLabel(result.message, SwingConstants.CENTER)
                content = JPanel(BorderLayout())
                content.add(label, BorderLayout.CENTER)
                content.updateUI()
            }

            is InventoryMatrix -> {
                val tableModel = InventoryTableModel(result)
                val table = JBTable(tableModel)
                val defaultScrollPane = JBScrollPane(table)
                defaultScrollPane.border = JBEmptyBorder(0)
                content = JPanel(BorderLayout())
                content.add(defaultScrollPane, BorderLayout.CENTER)
                content.updateUI()
            }
        }
    }

    fun getContent(): JPanel {
        return content
    }
}
