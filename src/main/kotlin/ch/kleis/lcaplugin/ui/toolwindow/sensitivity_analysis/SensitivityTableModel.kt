package ch.kleis.lcaplugin.ui.toolwindow.sensitivity_analysis

import ch.kleis.lcaplugin.core.assessment.SensitivityAnalysis
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.math.dual.DualNumber
import ch.kleis.lcaplugin.ui.toolwindow.FloatingPointRepresentation
import org.jetbrains.kotlinx.multik.ndarray.data.get
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class SensitivityTableModel(
    private val analysis: SensitivityAnalysis,
    observablePortComparator: Comparator<MatrixColumnIndex<DualNumber>>,
) : TableModel {
    private val sortedControllablePorts = analysis.getControllablePorts().getElements().sortedBy { it.getUID() }
    private val target = analysis
        .getObservablePorts().getElements()
        .sortedWith(observablePortComparator)
        .first()

    override fun getRowCount(): Int {
        return analysis.getParameters().size()
    }

    override fun getColumnCount(): Int {
        return 3 + analysis.getControllablePorts().size()
    }

    override fun getColumnName(columnIndex: Int): String {
        return when (columnIndex) {
            0 -> "name"
            1 -> "amount"
            2 -> "unit"
            else -> {
                val index = columnIndex - 3
                sortedControllablePorts[index].getDisplayName()
            }
        }
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        if (columnIndex < 3) {
            return String::class.java
        }
        val index = columnIndex - 3
        return sortedControllablePorts[index]::class.java
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        return when (columnIndex) {
            0 -> analysis.getParameters().getName(rowIndex).uid
            1 -> FloatingPointRepresentation.of(analysis.getParameters().getValue(rowIndex).amount.zeroth)
            2 -> analysis.getParameters().getValue(rowIndex).unit.symbol
            else -> {
                val parameterValue = analysis.getParameters().getValue(rowIndex).amount.zeroth
                val impactFactor = analysis.getImpactFactors().valueRatio(target, sortedControllablePorts[columnIndex - 3]).amount
                val base = impactFactor.zeroth
                val absoluteSensibility = impactFactor.first[rowIndex]
                return FloatingPointRepresentation.of(absoluteSensibility * parameterValue / base)
            }
        }
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
