package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.value.UnitValue
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.imports.ImportException
import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.Renderer
import org.openlca.simapro.csv.refdata.UnitRow

class UnitRenderer(private val knownUnits: MutableMap<String, UnitValue>) : Renderer<UnitRow> {
    data class AliasFor(val alias: Dimension, val aliasFor: Dimension) {
        constructor(alias: String, aliasFor: Dimension) : this(Dimension.of(alias), aliasFor)
    }

    private val dimAlias = listOf(
        AliasFor("volume", Prelude.volume),
        AliasFor("area", Prelude.area),
        AliasFor("power", Prelude.power),
        AliasFor("amount", Prelude.none),
        AliasFor("land use", Prelude.land_use),
        AliasFor("transport", Prelude.transport),
        AliasFor("length.time", Prelude.length_time),
        AliasFor("person.distance", Prelude.person_distance),
        AliasFor("mass.time", Prelude.mass_time),
        AliasFor("volume.time", Prelude.volume_time),
    ).associateBy { it.alias }

    companion object {
        fun of(existingUnits: Map<String, UnitValue>): UnitRenderer {
            val newMap = existingUnits.entries.map { (k, v) -> k.lowercase() to v }.associate { it }
            return UnitRenderer(newMap.toMutableMap())
        }
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun render(unit: UnitRow, writer: ModelWriter) {
        val dimensionName = unit.quantity().lowercase()
        val dimension = Dimension.of(dimensionName)
        val symbol = ModelWriter.sanitizeAndCompact(unit.name(), false)
        val existingUnits = getUnit(symbol)
        val block = if (existingUnits == null) {
            if (isNewDimensionReference(unit)) {
                addUnit(UnitValue(symbol, 1.0, dimension))
                """

                unit $symbol {
                    symbol = "${unit.name()}"
                    dimension = "$dimensionName"
                }
                """.trimIndent()
            } else {
                addUnit(UnitValue(symbol, unit.conversionFactor(), dimension))
                val refUnitSymbol = ModelWriter.sanitizeAndCompact(unit.referenceUnit(), false)
                val refUnit = getUnit(refUnitSymbol)
                if (refUnitSymbol.lowercase() == symbol.lowercase()) {
                    throw ImportException("Unit $symbol is referencing itself in its own declaration")
                } else {
                    """
                    unit $symbol {
                        symbol = "${unit.name()}"
                        alias_for = ${unit.conversionFactor()} ${refUnit?.symbol}
                    }""".trimIndent()
                }
            }
        } else {
            if (areCompatible(existingUnits.dimension, dimension)) {
                ""
            } else {
                throw ImportException("A Unit ${ModelWriter.sanitizeAndCompact(unit.name())} for ${unit.name()} already exists with another dimension, $dimension is not compatible with ${existingUnits.dimension}.")
            }
        }
        writer.write("unit", block, false)
    }

    private fun getUnit(symbol: String): UnitValue? {
        return knownUnits[symbol.lowercase()]
    }

    private fun addUnit(value: UnitValue) {
        knownUnits[value.symbol.lowercase()] = value
    }

    private fun isNewDimensionReference(unit: UnitRow): Boolean {
        val unitDim = Dimension.of(unit.quantity().lowercase())
        val allDimWithReference = knownUnits.values
            .filter { it.scale == 1.0 }
            .map { it.dimension }
        val isCompatibleWithNoOne = allDimWithReference
            .map { areCompatible(unitDim, it) }
            .none { it }
        return unit.conversionFactor() == 1.0 && isCompatibleWithNoOne
    }

    fun areCompatible(dim1: Dimension, dim2: Dimension): Boolean {
        return areCompatibleSym(dim1, dim2) || areCompatibleSym(dim2, dim1)
    }

    private fun areCompatibleSym(dim1: Dimension, dim2: Dimension): Boolean {
        return dim1 == dim2 || dimAlias[dim1]?.aliasFor == dim2
    }

}