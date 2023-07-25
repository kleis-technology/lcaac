package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.core.assessment.Inventory
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class InventoryTableModel(
    private val inventory: Inventory,
    observablePortComparator: Comparator<MatrixColumnIndex>,
) : TableModel {
    private val sortedObservablePorts = inventory.getObservablePorts().getElements().sortedWith(observablePortComparator)
    private val sortedControllablePorts = inventory.getControllablePorts().getElements().sortedBy { it.getUID() }

    override fun getRowCount(): Int {
        return sortedObservablePorts.size
    }

    override fun getColumnCount(): Int {
        return 3 + sortedControllablePorts.size
    }

    override fun getColumnName(columnIndex: Int): String {
        if (columnIndex == 0) {
            return "item"
        }

        if (columnIndex == 1) {
            return "quantity"
        }

        if (columnIndex == 2) {
            return "unit"
        }

        val product = sortedControllablePorts[columnIndex - 3]
        return "${product.getDisplayName()} [${product.referenceUnit().symbol}]"
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        if (columnIndex < 3) {
            return String::class.java
        }

        return sortedControllablePorts[columnIndex - 3]::class.java
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val outputProduct = sortedObservablePorts[rowIndex]

        if (columnIndex == 0) {
            return outputProduct.getDisplayName()
        }

        val quantity = inventory.supply.quantityOf(outputProduct)
        if (columnIndex == 1) {
            return FloatingPointRepresentation.of(quantity.amount).toString()
        }
        if (columnIndex == 2) {
            return "${quantity.unit.symbol}"
        }

        val inputProduct = sortedControllablePorts[columnIndex - 3]
        val ratio = inventory.impactFactors.valueRatio(outputProduct, inputProduct).amount
        return FloatingPointRepresentation.of(quantity.amount * ratio).toString()
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
