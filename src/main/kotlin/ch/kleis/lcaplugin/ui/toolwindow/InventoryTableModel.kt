package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.compute.matrix.InventoryMatrix
import ch.kleis.lcaplugin.compute.model.CharacterizationFactor
import ch.kleis.lcaplugin.compute.model.Exchange
import ch.kleis.lcaplugin.compute.model.Flow
import tech.units.indriya.quantity.Quantities.getQuantity
import javax.measure.Quantity
import javax.measure.Unit
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class InventoryTableModel(private val matrix: InventoryMatrix) : TableModel {
    override fun getRowCount(): Int {
        return matrix.observableFlows.size()
    }

    override fun getColumnCount(): Int {
        return 2 + matrix.controllableFlows.size()
    }

    override fun getColumnName(columnIndex: Int): String {
        if (columnIndex == 0) {
            return "flow"
        }

        if (columnIndex == 1) {
            return "unit"
        }

        return matrix.controllableFlows[columnIndex - 2].getUniqueId()
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        if (columnIndex < 2) {
            return String::class.java
        }

        return matrix.controllableFlows[columnIndex - 2]::class.java
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val outputFlow = matrix.observableFlows[rowIndex]

        if (columnIndex == 0) {
            return outputFlow.getUniqueId()
        }

        if (columnIndex == 1) {
            return outputFlow.getUnit().toString()
        }

        val inputFlow = matrix.controllableFlows[columnIndex - 2]
        val cf = matrix.value(outputFlow, inputFlow)
        return render(cf, inputFlow, outputFlow)
    }

    private fun <Din : Quantity<Din>, Dout : Quantity<Dout>> render(
        cf: CharacterizationFactor,
        inputFlow: Flow<Din>,
        outputFlow: Flow<Dout>
    ): Double {
        val input: Exchange<Din> = cf.input as Exchange<Din>
        val output: Exchange<Dout> = cf.output as Exchange<Dout>
        val numerator: Quantity<Din> = convert(input.quantity, inputFlow.getUnit())
        val denominator: Quantity<Dout> = convert(output.quantity, outputFlow.getUnit())
        return numerator.divide(denominator).value.toDouble()
    }

    // TODO: Find a way to use Quantity.to(...) without conflicting with Pair.to(...)
    private fun <D : Quantity<D>> convert(quantity: Quantity<D>, unit: Unit<D>): Quantity<D> {
        val converter = quantity.unit.getConverterTo(unit)
        return getQuantity(converter.convert(quantity.value), unit)
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {

    }

    override fun addTableModelListener(l: TableModelListener?) {

    }

    override fun removeTableModelListener(l: TableModelListener?) {

    }
}
