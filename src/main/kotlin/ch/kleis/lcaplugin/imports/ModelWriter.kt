package ch.kleis.lcaplugin.imports

import ch.kleis.lcaplugin.imports.util.AsynchronousWatcher
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.newvfs.RefreshQueue
import com.intellij.util.applyIf
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.io.Closeable
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists


private const val MAX_FILE_SIZE = 2000000
typealias Line = CharSequence
typealias Text = List<CharSequence>

data class FileWriterWithSize(val writer: FileWriter, val currentIndex: Int, var currentSize: Int = 0) :
    Closeable {

    constructor(path: Path, currentSize: Int) :
            this(FileWriter(Files.createFile(path).toFile(), Charset.forName("UTF-8")), currentSize)


    fun write(block: CharSequence) {
        val str = "$block\n"
        writer.write(str)
        currentSize += str.length
    }

    fun isFull(): Boolean {
        return currentSize > MAX_FILE_SIZE
    }

    override fun close() {
        writer.close()
    }
}

class ModelWriter(
    private val packageName: String,
    private val rootFolder: String,
    private val imports: List<String> = listOf(),
    private val watcher: AsynchronousWatcher
) : Closeable {
    companion object {
        private val LOG = Logger.getInstance(ModelWriter::class.java)
        const val BASE_PAD = 4
        private val multipleSeparator = Regex("_{2,}")

        fun sanitizeAndCompact(s: String, toLowerCase: Boolean = true): String {
            return sanitize(s, toLowerCase)
                .replace(multipleSeparator, "_")
        }

        fun sanitize(s: String, toLowerCase: Boolean = true): String {
            if (s.isBlank()) {
                return s
            }
            val r = if (s[0].isDigit()) "_$s" else s
            val spaces = """\s+""".toRegex()
            val nonAlphaNumeric = """[^a-zA-Z0-9_]+""".toRegex()
            return r
                .applyIf(toLowerCase, String::lowercase)
                .trim()
                .replace(spaces, "_")
                .replace("*", "_m_")
                .replace("+", "_p_")
                .replace("&", "_a_")
                .replace(">", "_gt_")
                .replace("<", "_lt_")
                .replace("/", "_sl_")
                .replace(nonAlphaNumeric, "_")
                .trimEnd('_')
        }

        fun compactText(s: CharSequence): String {
            if (s.isBlank()) {
                return ""
            }
            return s.toString()
                .replace("\"", "'")
                .trimEnd('\n')
                .trimEnd()
        }

        fun pad(text: List<CharSequence>, number: Int = BASE_PAD): CharSequence {
            val sep = " ".repeat(number)
            return text.joinToString("\n") { "$sep$it" }
        }

        fun compactAndPad(s: CharSequence, number: Int = BASE_PAD): String {
            val text = s.split("\n")
                .map { compactText(it) }
                .filter { it.isNotBlank() }
            return padButFirst(text, number)
        }

        fun padButFirst(text: List<CharSequence>, number: Int = BASE_PAD): String {
            val sep = " ".repeat(number)
            return text.joinToString("\n$sep")
        }

        private fun padList(text: List<CharSequence>, pad: Int): List<CharSequence> {
            val sep = " ".repeat(pad)
            return text.map { "$sep$it" }
        }


        fun asComment(str: String?): ImmutableList<String> {
            return (str ?: "")
                .split("\n")
                .filter { it.isNotBlank() }
                .map { "// $it" }
                .toImmutableList()
        }

        fun block(title: CharSequence, blockLines: List<CharSequence>, pad: Int = BASE_PAD): CharSequence {
            val lines: MutableList<CharSequence> = mutableListOf(title)
            val elements = padList(blockLines, pad)
            lines.addAll(elements)
            lines.add("}")
            return pad(lines, pad)
        }

        fun blockKeyValue(metas: MutableSet<MutableMap.MutableEntry<String, String?>>, pad: Int): CharSequence {
            val builder = StringBuilder()
            metas.forEach { (k, v) ->
                val split = v
                    ?.split("\n")
                    ?.map { compactText(it) }
                    ?.filterIndexed { k2, v2 -> v2.isNotBlank() || k2 == 0 }
                if (!split.isNullOrEmpty()) {
                    val sep = " ".repeat(pad)
                    builder.append(
                        """$sep"$k" = "${padButFirst(split, pad + 4)}"
"""
                    )
                }
            }
            return builder.dropLast(1)
        }

    }

    private val openedFiles: MutableMap<String, FileWriterWithSize> = mutableMapOf()

    fun write(relativePath: String, block: CharSequence, index: Boolean = true, closeAfterWrite: Boolean = false) {
        if (block.isNotBlank()) {
            watcher.notifyCurrentWork(relativePath)
            val file = recreateIfNeeded(relativePath, index)
            file.write(block)
            if (closeAfterWrite) {
                openedFiles.remove(relativePath)?.close()
            }
        }
    }

    private fun recreateIfNeeded(relativePath: String, index: Boolean): FileWriterWithSize {
        val existingFile = openedFiles[relativePath]
        return if (existingFile == null) {
            createNewFile(relativePath, 1, index)
        } else if (existingFile.isFull() && index) {
            existingFile.close()
            @Suppress("KotlinConstantConditions")
            createNewFile(relativePath, existingFile.currentIndex + 1, index)
        } else {
            existingFile
        }
    }

    private fun createNewFile(relativePath: String, currentIndex: Int, index: Boolean): FileWriterWithSize {
        val extension = if (index) "_$currentIndex.lca" else ".lca"
        val path = Paths.get(rootFolder + File.separatorChar + relativePath + extension)
        if (path.exists()) path.deleteExisting()
        Files.createDirectories(path.parent)
        val new = FileWriterWithSize(path, currentIndex)
        openedFiles[relativePath] = new
        new.write("package $packageName")
        imports.forEach { new.write("import $it") }
        return new
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