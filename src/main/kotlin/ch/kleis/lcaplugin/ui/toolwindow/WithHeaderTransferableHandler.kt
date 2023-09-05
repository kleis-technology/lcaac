package ch.kleis.lcaplugin.ui.toolwindow

import org.jdesktop.swingx.plaf.basic.core.BasicTransferable
import java.awt.datatransfer.Transferable
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.TransferHandler
import javax.swing.plaf.UIResource

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
