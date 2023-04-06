package ch.kleis.lcaplugin.imports

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.newvfs.RefreshQueue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
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
    companion object {
        private val LOG = Logger.getInstance(ModelWriter::class.java)
        private const val BASE_PAD = 4

        fun sanitizeString(s: String): String {
            if (s.isBlank()) {
                return s
            }
            val r = if (s[0].isDigit()) "_$s" else s
            val spaces = """\s+""".toRegex()
            val nonAlphaNumeric = """[^a-zA-Z0-9_]+""".toRegex()
            return r
                .lowercase()
                .trim()
                .replace(spaces, "_")
                .replace("*", "_m_")
                .replace("+", "_p_")
                .replace("&", "_a_")
                .replace(">", "_more_")
                .replace("<", "_less_")
                .replace(nonAlphaNumeric, "_")
                .replace("___", "_")
                .replace("__", "_")
                .trimEnd('_')
        }

        fun compactText(s: String): String {
            if (s.isBlank()) {
                return s
            }
            return s.replace("\"", "'")
                .trimEnd('\n').trimEnd()
        }

        fun pad(text: List<CharSequence>, number: Int = BASE_PAD): CharSequence {
            val sep = " ".repeat(number)
            return text.joinToString("\n") { "$sep$it" }
        }

        fun padList(text: List<CharSequence>, number: Int): List<CharSequence> {
            val sep = " ".repeat(number)
            return text.map { "$sep$it" }
        }

        fun asComment(str: String): ImmutableList<String> {
            return str.let { str.split("\n").map { "// $it" } }.toImmutableList()
        }

        fun optionalBlock(title: String, blockLines: List<String>, pad: Int = BASE_PAD): CharSequence {
            return if (blockLines.isNotEmpty()) {
                val lines: MutableList<CharSequence> = mutableListOf(title)
                val elements = padList(blockLines, pad)
                lines.addAll(elements)
                lines.add("}")
                pad(lines, pad)
            } else {
                ""
            }
        }

        fun block(title: String, blockLines: List<String>, pad: Int = BASE_PAD): CharSequence {
            val lines: MutableList<CharSequence> = mutableListOf(title)
            val elements = padList(blockLines, pad)
            lines.addAll(elements)
            lines.add("}")
            return pad(lines, pad)
        }

    }

    private val openedFiles: MutableMap<String, FileWriter> = mutableMapOf()

    fun write(relativePath: String, block: CharSequence) {
        if (block.isNotEmpty()) {
            val file = recreateIfNotOpened(relativePath)
            file.write(block.toString())
            file.write("\n")
            file.flush()
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
                val fullPath = Paths.get(rootFolder + File.separatorChar + path)
                val virtualFile = LocalFileSystem.getInstance().findFileByPath(fullPath.toString())
                virtualFile?.let {
                    RefreshQueue.getInstance()
                        .refresh(false, false, null, ModalityState.current(), it)
                }
            } catch (e: IOException) {
                LOG.error("Unable to close file $path", e)
            }
        }
    }


}