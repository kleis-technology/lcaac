package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.core.matrix.InventoryError
import ch.kleis.lcaplugin.core.matrix.InventoryMatrix
import ch.kleis.lcaplugin.core.matrix.InventoryResult
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBEmptyBorder
import org.jdesktop.swingx.plaf.basic.core.BasicTransferable
import java.awt.BorderLayout
import java.awt.datatransfer.Transferable
import javax.swing.*
import javax.swing.plaf.UIResource

/*
    https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/tool_window
 */

class LcaProcessAssessResult(result: InventoryResult): LcaToolWindowContent {
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
                table.transferHandler = WithHeaderTransferableHandler()
                val defaultScrollPane = JBScrollPane(table)
                defaultScrollPane.border = JBEmptyBorder(0)
                content = JPanel(BorderLayout())
                content.add(defaultScrollPane, BorderLayout.CENTER)
                content.updateUI()
            }
        }
    }

    override fun getContent(): JPanel {
        return content
    }

    class WithHeaderTransferableHandler : TransferHandler(), UIResource {

        /**
         * Create a Transferable to use as the source for a data transfer.
         *
         * @param c  The component holding the data to be transfered.  This
         * argument is provided to enable sharing of TransferHandlers by
         * multiple components.
         * @return  The representation of the data to be transfered.
         */
        public override  fun createTransferable(c: JComponent?): Transferable? {
            if (c is JTable) {
                val table = c
                val rows: IntArray?
                val cols: IntArray?
                if (!table.rowSelectionAllowed && !table.columnSelectionAllowed) {
                    return null
                }
                if (!table.rowSelectionAllowed) {
                    val rowCount = table.rowCount
                    rows = IntArray(rowCount)
                    for (counter in 0 until rowCount) {
                        rows[counter] = counter
                    }
                } else {
                    rows = table.selectedRows
                }
                if (!table.columnSelectionAllowed) {
                    val colCount = table.columnCount
                    cols = IntArray(colCount)
                    for (counter in 0 until colCount) {
                        cols[counter] = counter
                    }
                } else {
                    cols = table.selectedColumns
                }
                if (rows == null || cols == null || rows.size == 0 || cols.size == 0) {
                    return null
                }
                val plainStr = StringBuilder()
                val htmlStr = StringBuilder()
                htmlStr.append("<html>\n<body>\n<table>\n")
                htmlStr.append("<tr>\n")
                for (col in cols.indices) {
                    val obj = table.getColumnName(cols[col])
                    val `val` = obj?.toString() ?: ""
                    plainStr.append(`val`).append('\t')
                    htmlStr.append("  <th>").append(`val`).append("</th>\n")
                }
                plainStr.append("\n")
                htmlStr.append("</tr>\n")
                for (row in rows.indices) {
                    htmlStr.append("<tr>\n")
                    for (col in cols.indices) {
                        val obj = table.getValueAt(rows[row], cols[col])
                        val `val` = obj?.toString() ?: ""
                        plainStr.append(`val`).append('\t')
                        htmlStr.append("  <td>").append(`val`).append("</td>\n")
                    }
                    // we want a newline at the end of each line and not a tab
                    plainStr.deleteCharAt(plainStr.length - 1).append('\n')
                    htmlStr.append("</tr>\n")
                }

                // remove the last newline
                plainStr.deleteCharAt(plainStr.length - 1)
                htmlStr.append("</table>\n</body>\n</html>")
                return BasicTransferable(plainStr.toString(), htmlStr.toString())
            }
            return null
        }

        override fun getSourceActions(c: JComponent?): Int {
            return TransferHandler.COPY
        }
    }
}
