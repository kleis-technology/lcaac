package ch.kleis.lcaplugin.ui.toolwindow.contribution_analysis

import ch.kleis.lcaplugin.core.assessment.ContributionAnalysis
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.math.basic.BasicMatrix
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.ui.toolwindow.FloatingPointRepresentation
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class ContributionTableModel(
    private val analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    observablePortComparator: Comparator<MatrixColumnIndex<BasicNumber>>,
) : TableModel {
    private val sortedObservablePorts = analysis.getObservablePorts().getElements().sortedWith(observablePortComparator)
    private val sortedControllablePorts = analysis.getControllablePorts().getElements().sortedBy { it.getUID() }

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
        return when (columnIndex) {
            0 -> String::class.java
            2 -> String::class.java
            else -> FloatingPointRepresentation::class.java
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val outputProduct = sortedObservablePorts[rowIndex]
        if (columnIndex == 0) {
            return outputProduct.getDisplayName()
        }

        val quantity = analysis.supplyOf(outputProduct)
        if (columnIndex == 1) {
            return FloatingPointRepresentation.of(quantity.amount.value)
        }
        if (columnIndex == 2) {
            return "${quantity.unit.symbol}"
        }

        val inputProduct = sortedControllablePorts[columnIndex - 3]
        val contribution = analysis.getPortContribution(outputProduct, inputProduct).amount.value
        return FloatingPointRepresentation.of(contribution)
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
