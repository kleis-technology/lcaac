package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.matrix.InventoryMatrix
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class InventoryTableModel(
    private val matrix: InventoryMatrix,
    observablePortComparator: Comparator<MatrixColumnIndex>,
) : TableModel {
    private val sortedObservablePorts = matrix.observablePorts.getElements().sortedWith(observablePortComparator)
    private val sortedControllablePorts = matrix.controllablePorts.getElements().sortedBy { it.getUID() }

    override fun getRowCount(): Int {
        return sortedObservablePorts.size
    }

    override fun getColumnCount(): Int {
        return 2 + sortedControllablePorts.size
    }

    override fun getColumnName(columnIndex: Int): String {
        if (columnIndex == 0) {
            return "item"
        }

        if (columnIndex == 1) {
            return "quantity"
        }

        val product = sortedControllablePorts[columnIndex - 2]
        return "${product.getDisplayName()} [${product.referenceUnit().symbol}]"
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        if (columnIndex < 2) {
            return String::class.java
        }

        return sortedControllablePorts[columnIndex - 2]::class.java
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val outputProduct = sortedObservablePorts[rowIndex]

        if (columnIndex == 0) {
            return outputProduct.getDisplayName()
        }

        if (columnIndex == 1) {
            return "1 ${outputProduct.referenceUnit().symbol}"
        }

        val inputProduct = sortedControllablePorts[columnIndex - 2]
        return matrix.valueRatio(outputProduct, inputProduct).amount
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        // Read Only
    }

    override fun addTableModelListener(l: TableModelListener?) {
        // Read Only
    }

    override fun removeTableModelListener(l: TableModelListener?) {
        // Read Only
    }
}
