package ch.kleis.lcaplugin.project

import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.expression.EUnitLiteral
import ch.kleis.lcaplugin.core.prelude.Prelude
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import kotlin.io.path.deleteExisting

typealias UnitBlock = CharSequence

class UnitLcaFileFromPreludeGenerator<Q> {

    private val existingRefUnit = mutableMapOf<Dimension, EUnitLiteral<Q>>()

    fun recreate(path: Path, version: String) {
        path.deleteExisting()
        val file = path.toFile()
        file.createNewFile()
        FileOutputStream(file)
            .use { fileOut ->
                val jar = JarOutputStream(fileOut)
                val manifest = Manifest()
                val global: Attributes = manifest.mainAttributes
                global[Attributes.Name.MANIFEST_VERSION] = version
                global[Attributes.Name("Created-By")] = "Lca Team"
                global[Attributes.Name.CONTENT_TYPE] = "application/lca"
                val je = JarEntry("unit/built_in_units.lca")
                je.comment = "built_in_units";
                jar.putNextEntry(je)
                OutputStreamWriter(jar, StandardCharsets.UTF_8)
                    .use { w ->
                        Prelude.unitMap<Q>().values
                            .filter { it.scale == 1.0 }
                            .mapNotNull { mapUnitWithNewDimension(it) }
                            .forEach { w.write(it.toString()) }
                        Prelude.unitMap<Q>().values
                            .mapNotNull { mapUnitAsAlias(it) }
                            .forEach { w.write(it.toString()) }
                    }
            }

    }


    private fun mapUnitAsAlias(unit: EUnitLiteral<Q>): UnitBlock? {
        val refUnit = existingRefUnit[unit.dimension]!!
        return if (refUnit == unit) {
            null
        } else {
            """

unit ${unit.symbol} {
    symbol = "${unit.symbol}"
    alias_for = ${unit.scale}  ${refUnit.symbol}
}
"""
        }
    }

    private fun mapUnitWithNewDimension(unit: EUnitLiteral<Q>): UnitBlock? {
        return if (existingRefUnit.containsKey(unit.dimension)) {
            null
        } else {
            existingRefUnit[unit.dimension] = unit
            """
    
unit ${unit.symbol} {
    symbol = "${unit.symbol}"
    dimension = "${unit.dimension}"
}
    """
        }
    }

}