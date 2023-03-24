package ch.kleis.lcaplugin.imports

import com.intellij.openapi.diagnostic.Logger
import java.io.Closeable
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists

interface Renderer<T> {
    fun render(block: T, writer: ModelWriter)
}

class ModelWriter(private val packageName: String, private val rootFolder: String) : Closeable {
    private companion object {
        private val LOG = Logger.getInstance(ModelWriter::class.java)
    }

    private val openedFiles: MutableMap<String, FileWriter> = mutableMapOf()

    fun write(relativePath: String, block: CharSequence) {
        if (block.isNotEmpty()) {
            val file = recreateIfNotOpened(relativePath)
            file.write(block.toString())
            file.write("\n")
        }
    }

    private fun recreateIfNotOpened(relativePath: String): FileWriter {
        val existingFile = openedFiles[relativePath]
        return if (existingFile == null) {
            val path = Paths.get(rootFolder + File.separatorChar + relativePath)
            if (path.exists()) path.deleteExisting()
            Files.createDirectories(path.parent)
            val new = FileWriter(Files.createFile(path).toFile(), Charset.forName("UTF-8"))
            openedFiles[relativePath] = new
            new.write("package $packageName\n\n")
            new
        } else {
            existingFile
        }
    }

    override fun close() {
        openedFiles.entries.forEach { (path, writer) ->
            try {
                writer.close()
            } catch (e: IOException) {
                LOG.error("Unable to close file $path", e)
            }
        }
    }

    fun sanitizeString(s: String): String {
        if (s.isBlank()) {
            return s
        }
        val r = if (s[0].isDigit()) "_$s" else s
        val spaces = """\s+""".toRegex()
        val nonAlphaNumeric = """[^a-zA-Z0-9_]+""".toRegex()
        return r
            .trim()
            .replace(spaces, "_")
            .replace("*", "_m_")
            .replace("+", "_p_")
            .replace("&", "_a_")
            .replace(nonAlphaNumeric, "_")
            .trimEnd('_')
    }
}