package ch.kleis.lcaplugin.ui.toolwindow.sensitivity_analysis

import ch.kleis.lcaplugin.core.assessment.SensitivityAnalysis
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.math.dual.DualNumber
import ch.kleis.lcaplugin.ui.toolwindow.FloatingPointRepresentation
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel


class TransposedSensitivityTableModel(
    private val analysis: SensitivityAnalysis,
    var target: MatrixColumnIndex<DualNumber>,
) : TableModel {
    private val sortedControllablePorts = analysis.getControllablePorts().getElements().sortedBy { it.getUID() }

    override fun getRowCount(): Int {
        return analysis.getControllablePorts().size()
    }

    override fun getColumnCount(): Int {
        return 3 + analysis.getParameters().size()
    }

    override fun getColumnName(columnIndex: Int): String {
        return when (columnIndex) {
            0 -> "name"
            1 -> "amount"
            2 -> "unit"
            else -> {
                val index = columnIndex - 3
                analysis.getParameters().getName(index).uid
            }
        }
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return when (columnIndex) {
            0, 2 -> String::class.java
            else -> FloatingPointRepresentation::class.java
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    private fun repr(d: Double, suffix: String? = null): FloatingPointRepresentation {
        return FloatingPointRepresentation.of(d, 3, suffix)
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        return when (columnIndex) {
            0 -> sortedControllablePorts[rowIndex].getDisplayName()
            1 -> {
                val indicator = sortedControllablePorts[rowIndex]
                val contribution = analysis.getPortContribution(target, indicator)
                repr(contribution.amount.zeroth)
            }

            2 -> {
                val indicator = sortedControllablePorts[rowIndex]
                val contribution = analysis.getPortContribution(target, indicator)
                contribution.unit.symbol
            }

            else -> {
                val relativeSensibility = analysis.getRelativeSensibility(
                    target,
                    sortedControllablePorts[rowIndex],
                    analysis.getParameters().getName(columnIndex - 3)
                )
                return repr(relativeSensibility)
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
