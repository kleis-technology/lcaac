package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.MyBundle
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.matrix.InventoryMatrix
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBEmptyBorder
import org.jdesktop.swingx.plaf.basic.core.BasicTransferable
import java.awt.BorderLayout
import java.awt.datatransfer.Transferable
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.plaf.UIResource

/*
    https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/tool_window
 */

class LcaProcessAssessResult(
    inventory: InventoryMatrix,
    observablePortComparator: Comparator<MatrixColumnIndex>,
    val project: Project,
    val name: String,
) :
    LcaToolWindowContent {
    private val content: JPanel

    init {
        val tableModel = InventoryTableModel(inventory, observablePortComparator)
        val table = JBTable(tableModel)
        table.transferHandler = WithHeaderTransferableHandler()
        table.addMouseListener(SaveListener(project, inventory, name))
        table.toolTipText = MyBundle.message("lca.dialog.export.tooltip")
        val defaultScrollPane = JBScrollPane(table)
        defaultScrollPane.border = JBEmptyBorder(0)
        content = JPanel(BorderLayout())
        content.add(defaultScrollPane, BorderLayout.CENTER)
        content.updateUI()
    }

    private class SaveListener(val project: Project, val inventory: InventoryMatrix, val name: String) :
        MouseAdapter() {
        override fun mouseReleased(e: MouseEvent?) {
            if (SwingUtilities.isRightMouseButton(e)) {
                val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return

                val content = ContentFactory.getInstance()
                    .createContent(
                        LcaProcessAssessHugeResult(inventory, "lca.dialog.export.info", project).getContent(),
                        name,
                        false
                    )
                toolWindow.contentManager.addContent(content)
                toolWindow.contentManager.setSelectedContent(content)
                toolWindow.show()

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
         * @param c  The component holding the data to be transferred.  This
         * argument is provided to enable sharing of TransferHandlers by
         * multiple components.
         * @return  The representation of the data to be transferred.
         */
        public override fun createTransferable(c: JComponent?): Transferable? {
            if (c is JTable) {
                val rows: IntArray?
                val cols: IntArray?
                if (!c.rowSelectionAllowed && !c.columnSelectionAllowed) {
                    return null
                }
                if (!c.rowSelectionAllowed) {
                    val rowCount = c.rowCount
                    rows = IntArray(rowCount)
                    for (counter in 0 until rowCount) {
                        rows[counter] = counter
                    }
                } else {
                    rows = c.selectedRows
                }
                if (!c.columnSelectionAllowed) {
                    val colCount = c.columnCount
                    cols = IntArray(colCount)
                    for (counter in 0 until colCount) {
                        cols[counter] = counter
                    }
                } else {
                    cols = c.selectedColumns
                }
                if (rows == null || cols == null || rows.isEmpty() || cols.isEmpty()) {
                    return null
                }
                val plainStr = StringBuilder()
                val htmlStr = StringBuilder()
                htmlStr.append("<html>\n<body>\n<table>\n")
                htmlStr.append("<tr>\n")
                for (col in cols.indices) {
                    val obj = c.getColumnName(cols[col])
                    val `val` = obj?.toString() ?: ""
                    plainStr.append(`val`).append('\t')
                    htmlStr.append("  <th>").append(`val`).append("</th>\n")
                }
                plainStr.append("\n")
                htmlStr.append("</tr>\n")
                for (row in rows.indices) {
                    htmlStr.append("<tr>\n")
                    for (col in cols.indices) {
                        val obj = c.getValueAt(rows[row], cols[col])
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
            return COPY
        }
    }
}
