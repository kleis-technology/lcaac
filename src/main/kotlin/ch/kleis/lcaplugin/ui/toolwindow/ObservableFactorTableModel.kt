package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.compute.matrix.ObservableFactorMatrix
import ch.kleis.lcaplugin.compute.model.*
import tech.units.indriya.quantity.Quantities.getQuantity
import javax.measure.Quantity
import javax.measure.Unit
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class ObservableFactorTableModel(private val matrix: ObservableFactorMatrix) : TableModel {
    override fun getRowCount(): Int {
        return matrix.getObservableFlows().size()
    }

    override fun getColumnCount(): Int {
        return matrix.getIndicators().size()
    }

    override fun getColumnName(columnIndex: Int): String {
        return matrix.getIndicators()[columnIndex].getUniqueId()
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return matrix.getIndicators()[columnIndex].javaClass
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val flow = matrix.getObservableFlows()[rowIndex]
        val indicator = matrix.getIndicators()[columnIndex]
        val cf = matrix.value(flow, indicator)
        return render(cf, indicator, flow)
    }

    private fun <Din : Quantity<Din>, Dout : Quantity<Dout>> render(
        cf: CharacterizationFactor,
        indicator: Indicator<Din>,
        flow: IntermediaryFlow<Dout>
    ): Double {
        val input: Exchange<Din, Flow<Din>> = cf.input as Exchange<Din, Flow<Din>>
        val output: Exchange<Dout, Indicator<Dout>> = cf.output as Exchange<Dout, Indicator<Dout>>
        val numerator: Quantity<Din> = convert(input.quantity, indicator.getUnit())
        val denominator: Quantity<Dout> = convert(output.quantity, flow.getUnit())
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
