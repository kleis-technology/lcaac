package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.core.lang_obsolete.VCharacterizationFactor
import ch.kleis.lcaplugin.core.lang_obsolete.VProduct
import ch.kleis.lcaplugin.core.matrix.InventoryMatrix
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class InventoryTableModel(private val matrix: InventoryMatrix) : TableModel {
    override fun getRowCount(): Int {
        return matrix.observableProducts.size()
    }

    override fun getColumnCount(): Int {
        return 2 + matrix.controllableProducts.size()
    }

    override fun getColumnName(columnIndex: Int): String {
        if (columnIndex == 0) {
            return "product"
        }

        if (columnIndex == 1) {
            return "unit"
        }

        val product = matrix.controllableProducts[columnIndex - 2]
        return "${product.name} [${product.referenceUnit.symbol}]"
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        if (columnIndex < 2) {
            return String::class.java
        }

        return matrix.controllableProducts[columnIndex - 2]::class.java
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val outputProduct = matrix.observableProducts[rowIndex]

        if (columnIndex == 0) {
            return outputProduct.name
        }

        if (columnIndex == 1) {
            return outputProduct.referenceUnit.symbol
        }

        val inputProduct = matrix.controllableProducts[columnIndex - 2]
        val cf = matrix.value(outputProduct, inputProduct)
        return render(cf, inputProduct, outputProduct)
    }

    private fun render(
        cf: VCharacterizationFactor,
        inputProduct: VProduct,
        outputProduct: VProduct,
    ): Double {
        val input = cf.input
        val output = cf.output
        val numerator = input.quantity.referenceValue() / inputProduct.referenceUnit.scale
        val denominator = output.quantity.referenceValue() / outputProduct.referenceUnit.scale
        return numerator / denominator
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {

    }

    override fun addTableModelListener(l: TableModelListener?) {

    }

    override fun removeTableModelListener(l: TableModelListener?) {

    }
}
