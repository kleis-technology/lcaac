package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.core.lang.value.CharacterizationFactorValue
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.matrix.InventoryMatrix
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class InventoryTableModel(private val matrix: InventoryMatrix) : TableModel {
    override fun getRowCount(): Int {
        return matrix.observablePorts.size()
    }

    override fun getColumnCount(): Int {
        return 2 + matrix.controllablePorts.size()
    }

    override fun getColumnName(columnIndex: Int): String {
        if (columnIndex == 0) {
            return "item"
        }

        if (columnIndex == 1) {
            return "quantity"
        }

        val product = matrix.controllablePorts[columnIndex - 2]
        return "${product.getDisplayName()} [${product.referenceUnit().symbol}]"
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        if (columnIndex < 2) {
            return String::class.java
        }

        return matrix.controllablePorts[columnIndex - 2]::class.java
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val outputProduct = matrix.observablePorts[rowIndex]

        if (columnIndex == 0) {
            return outputProduct.getDisplayName()
        }

        if (columnIndex == 1) {
            return "1 ${outputProduct.referenceUnit().symbol}"
        }

        val inputProduct = matrix.controllablePorts[columnIndex - 2]
        val cf = matrix.value(outputProduct, inputProduct)
        return render(cf, inputProduct, outputProduct)
    }

    private fun render(
        cf: CharacterizationFactorValue,
        inputPort: MatrixColumnIndex,
        outputPort: MatrixColumnIndex,
    ): Double {
        val input = cf.input
        val output = cf.output
        val numerator = input.quantity().referenceValue() / inputPort.referenceUnit().scale
        val denominator = output.quantity().referenceValue() / outputPort.referenceUnit().scale
        return numerator / denominator
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {

    }

    override fun addTableModelListener(l: TableModelListener?) {

    }

    override fun removeTableModelListener(l: TableModelListener?) {

    }
}
