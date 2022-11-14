package ch.kleis.lcaplugin.ui.toolwindow

import com.intellij.ui.table.JBTable
import javax.swing.table.TableColumn
import kotlin.math.max

/*
    https://tips4java.wordpress.com/2008/11/10/table-column-adjuster/
 */

class TableColumnAdjuster(val table: JBTable) {
    fun adjust() {
        val columnModel = table.columnModel
        for (col: Int in 0 until columnModel.columnCount) {
            adjust(col)
        }
    }

    private fun adjust(col: Int) {
        val column = table.columnModel.getColumn(col)
        val columnHeaderWidth = getColumnHeaderWidth(col);
        val columnDataWidth = getColumnDataWidth(col);
        val preferredWidth = max(columnHeaderWidth, columnDataWidth);

        updateTableColumn(column, preferredWidth);

    }

    private fun updateTableColumn(column: TableColumn, preferredWidth: Int) {
        table.tableHeader.resizingColumn = column
        column.width = preferredWidth
    }

    private fun getColumnDataWidth(col: Int): Int {
        var preferredWidth = 0;
        val maxWith = table.columnModel.getColumn(col).maxWidth

        for (row: Int in 0 until table.rowCount) {
            val cellRenderer = table.getCellRenderer(row, col)
            val component = table.prepareRenderer(cellRenderer, row, col)
            val width = component.preferredSize.width + table.intercellSpacing.width
            preferredWidth = max(preferredWidth, width)
            if (preferredWidth >= maxWith) {
                break
            }
        }

        return preferredWidth
    }

    private fun getColumnHeaderWidth(col: Int): Int {
        val column = table.columnModel.getColumn(col)
        val value = column.headerValue
        val renderer = column.headerRenderer ?: table.tableHeader.defaultRenderer
        val component = renderer.getTableCellRendererComponent(table, value, false, false, -1, col)
        return component.preferredSize.width
    }
}
