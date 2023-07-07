package ch.kleis.lcaplugin.imports.shared.serializer

import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol
import ch.kleis.lcaplugin.core.lang.value.UnitValue
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.imports.ImportException
import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.model.ImportedUnit
import ch.kleis.lcaplugin.imports.simapro.sanitizeSymbol

class UnitRenderer(private val knownUnits: MutableMap<String, UnitValue>) {
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
    var nbUnit = 0

    companion object {
        fun of(existingUnits: Map<String, UnitValue>): UnitRenderer {
            val newMap = existingUnits.entries.map { (k, v) -> k to v }.associate { it }
            return UnitRenderer(newMap.toMutableMap())
        }
    }


    fun render(unit: ImportedUnit, writer: ModelWriter) {
        val dimensionName = unit.dimension.lowercase()
        val dimension = Dimension.of(dimensionName)
        val symbol = sanitizeSymbol(ModelWriter.sanitizeAndCompact(unit.name, false))
        val existingUnit = getUnit(unit.name)

        when {
            existingUnit != null && areCompatible(existingUnit.dimension, dimension) -> { /* Nothing to do */
            }

            existingUnit != null && !areCompatible(existingUnit.dimension, dimension) ->
                throw ImportException("A Unit ${ModelWriter.sanitizeAndCompact(unit.name)} for ${unit.name} already exists with another dimension, $dimension is not compatible with ${existingUnit.dimension}.")

            isNewDimensionReference(dimension, unit.scaleFactor) -> {
                addUnit(UnitValue(UnitSymbol.of(symbol), 1.0, dimension))
                val block = generateUnitBlockWithNewDimension(symbol, unit.name, dimensionName, unit.comment)
                writer.write("unit", block, false)
            }

            else -> {
                addUnit(UnitValue(UnitSymbol.of(symbol), unit.scaleFactor, dimension))
                val refUnitSymbol = unit.refUnitName

                if (refUnitSymbol == symbol) {
                    throw ImportException("Unit $symbol is referencing itself in its own declaration")
                } else {
                    val block =
                        generateUnitAliasBlock(symbol, unit.name, "${unit.scaleFactor} $refUnitSymbol", unit.comment)
                    writer.write("unit", block, false)
                }
            }
        }
        nbUnit++
    }

    private fun generateUnitBlockWithNewDimension(
        symbol: String,
        unitName: String,
        dimensionName: String,
        comment: String?
    ): String {
        val sanitizedComment = if (comment != null) " // $comment" else ""
        return """

        unit $symbol {$sanitizedComment
            symbol = "$unitName"
            dimension = "$dimensionName"
        }
        """.trimIndent()
    }

    private fun generateUnitAliasBlock(symbol: String, unitName: String, alias: String, comment: String?): String {
        val sanitizedComment = if (comment != null) " // $comment" else ""
        return """
    
        unit $symbol {$sanitizedComment
            symbol = "$unitName"
            alias_for = $alias
        }""".trimIndent()
    }

    private fun getUnit(symbolName: String): UnitValue? {
        val symbol = ModelWriter.sanitizeAndCompact(symbolName, false)
        return knownUnits[symbol]
    }

    private fun addUnit(value: UnitValue) {
        knownUnits[value.symbol.toString().lowercase()] = value
    }

    private fun isNewDimensionReference(dimension: Dimension, scaleFactor: Double): Boolean {
        val allDimWithReference = knownUnits.values
            .filter { it.scale == 1.0 }
            .map { it.dimension }
        val isCompatibleWithNoOne = allDimWithReference
            .map { areCompatible(dimension, it) }
            .none { it }
        return scaleFactor == 1.0 && isCompatibleWithNoOne
    }

    fun areCompatible(dim1: Dimension, dim2: Dimension): Boolean {
        return areCompatibleSym(dim1, dim2) || areCompatibleSym(dim2, dim1)
    }

    private fun areCompatibleSym(dim1: Dimension, dim2: Dimension): Boolean {
        return dim1 == dim2 || dimAlias[dim1]?.aliasFor == dim2
    }

}
