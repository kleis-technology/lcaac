package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.value.UnitValue
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.imports.ImportException
import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.Renderer
import ch.kleis.lcaplugin.language.reservedWords
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
        val existingUnit = getUnit(symbol)

        when {
            existingUnit != null && areCompatible(existingUnit.dimension, dimension) -> { /* Nothing to do */
            }

            existingUnit != null && !areCompatible(existingUnit.dimension, dimension) ->
                throw ImportException("A Unit ${ModelWriter.sanitizeAndCompact(unit.name())} for ${unit.name()} already exists with another dimension, $dimension is not compatible with ${existingUnit.dimension}.")

            isNewDimensionReference(unit) -> {
                addUnit(UnitValue(symbol, 1.0, dimension))
                val block = generateUnitBlockWithNewDimension(symbol, unit.name(), dimensionName)
                writer.write("unit", block, false)
            }

            else -> {
                addUnit(UnitValue(symbol, unit.conversionFactor(), dimension))
                val refUnitSymbol = ModelWriter.sanitizeAndCompact(unit.referenceUnit(), false)

                val refUnit = getUnit(refUnitSymbol)
                if (refUnitSymbol.lowercase() == symbol.lowercase()) {
                    throw ImportException("Unit $symbol is referencing itself in its own declaration")
                } else {
                    val block =
                        generateUnitAliasBlock(symbol, unit.name(), "${unit.conversionFactor()} ${refUnit?.symbol}")
                    writer.write("unit", block, false)
                }
            }
        }
    }

    private fun generateUnitBlockWithNewDimension(symbol: String, unitName: String, dimensionName: String): String {
        val sanitizedSymbol = sanitizeSymbol(symbol)
        val sanitizedComment = getSanitizedSymbolComment(symbol, sanitizedSymbol)
        return """

        unit $sanitizedSymbol {$sanitizedComment
            symbol = "$unitName"
            dimension = "$dimensionName"
        }
        """.trimIndent()
    }

    private fun generateUnitAliasBlock(symbol: String, unitName: String, alias: String): String {
        val sanitizedSymbol = sanitizeSymbol(symbol)
        val sanitizedComment = getSanitizedSymbolComment(symbol, sanitizedSymbol)
        return """
    
        unit $sanitizedSymbol {$sanitizedComment
            symbol = "$unitName"
            alias_for = $alias
        }""".trimIndent()
    }

    fun sanitizeSymbol(symbol: String): String {
        return if (symbol in reservedWords) {
            "_$symbol"
        } else {
            symbol
        }
    }

    fun getSanitizedSymbolComment(symbol: String, sanitizedSymbol: String): String {
        return if (symbol == sanitizedSymbol) {
            ""
        } else {
            " // $symbol"
        }
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