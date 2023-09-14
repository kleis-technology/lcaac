package ch.kleis.lcaplugin.project

import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.expression.EUnitLiteral
import ch.kleis.lcaplugin.core.prelude.Prelude
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.xml.bind.DatatypeConverter
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists


typealias UnitBlock = CharSequence

class UnitLcaFileFromPreludeGenerator<Q> {

    private val existingRefUnit = mutableMapOf<Dimension, EUnitLiteral<Q>>()

    fun recreate(path: Path) {
        val newContent = getContent()
        val newHash = hash(newContent.toByteArray(StandardCharsets.UTF_8))
        if (haveToRecreate(path, newHash)) {
            if (path.exists()) path.deleteExisting()
            val file = path.toFile()
            file.createNewFile()
            FileOutputStream(file)
                .use { fileOut ->
                    ZipOutputStream(fileOut).use { jar ->
                        val je = ZipEntry("built_in_units.lca")
                        je.comment = "built_in_units";
                        jar.putNextEntry(je)
                        jar.write(newContent.toByteArray())
                        jar.closeEntry()
                        val jeMd5 = ZipEntry("built_in_units.lca.md5")
                        jeMd5.comment = "built_in_units_mda";
                        jar.putNextEntry(jeMd5)
                        jar.write(newHash.toByteArray())
                        jar.closeEntry()
                    }
                }
        }
    }

    private fun haveToRecreate(path: Path, newHash: String): Boolean {
        if (path.exists()) {
            val oldHash = readEntry(path, "built_in_units.lca.md5")
            if (oldHash == newHash) {
                return false
            }
        }
        return true
    }

    private fun readEntry(path: Path, entryName: String): String? {
        FileInputStream(path.toFile()).use { fis ->
            BufferedInputStream(fis).use { bis ->
                ZipInputStream(bis).use { stream ->
                    var entry: ZipEntry?
                    while ((stream.nextEntry.also { entry = it } != null)) {
                        if (entry!!.name == entryName) {
                            InputStreamReader(stream).use { reader ->
                                return reader.readText()
                            }
                        }
                    }
                    return null
                }
            }
        }
    }

    private fun hash(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("MD5")
        md.update(bytes)
        val digest = md.digest()
        return DatatypeConverter.printHexBinary(digest).uppercase(Locale.getDefault())
    }

    private fun getContent(): String {
        val buffer = StringBuilder()
        Prelude.primitiveUnits<Q>().values
        buffer.append("package internal\n")
        Prelude.primitiveUnits<Q>().values
            .filter { it.scale == 1.0 }
            .mapNotNull { mapUnitWithNewDimension(it) }
            .forEach { buffer.append(it.toString()) }
        Prelude.primitiveUnits<Q>().values
            .mapNotNull { mapUnitAsAlias(it) }
            .forEach { buffer.append(it.toString()) }
        Prelude.compositeUnits<Q>()
            .map { mapUnitWithAlias(it.key, it.value) }
            .forEach { buffer.append(it.toString()) }
        return buffer.toString()
    }


    private fun mapUnitWithAlias(unit: EUnitLiteral<Q>, alias: String): UnitBlock {
        return """

unit ${unit.symbol} {
    symbol = "${unit.symbol}"
    alias_for = $alias
}
"""
    }

    private fun mapUnitAsAlias(unit: EUnitLiteral<Q>): UnitBlock? {
        val refUnit = existingRefUnit[unit.dimension]!!
        return if (refUnit == unit) {
            null
        } else {
            """

unit ${unit.symbol} {
    symbol = "${unit.symbol}"
    alias_for = ${unit.scale} ${refUnit.symbol}
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
