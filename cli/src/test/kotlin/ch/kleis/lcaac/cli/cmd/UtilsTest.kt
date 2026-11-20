package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.source.SourceLocation
import ch.kleis.lcaac.core.lang.source.SourceMap
import ch.kleis.lcaac.core.lang.source.SourcePath
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.grammar.SyntaxException
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows


class UtilsTest {
    @Nested
    inner class ParseSourceTest {
        @Test
        fun `directory should be returned as-is`() {
            // given
            val tmpDir = createTempDirectory().toFile()

            // when
            val result = parseSource(tmpDir)

            // then
            assertEquals(tmpDir.absoluteFile, result.absoluteFile)
        }

        @Test
        fun `archived sources should be extracted`() {
            val extensions = listOf("zip", "tar.gz", "tgz")
            extensions.forEach { extension ->
                // given
                val zipFile = File("src/test/resources/main.$extension")

                // when
                val result = parseSource(zipFile)

                // then
                val extractedMainFile = File(result, "src/main.lca")
                assertTrue(
                    extractedMainFile.exists() && extractedMainFile.isFile,
                    "Expected main.lca to exist inside extracted directory"
                )
            }
        }

        @Test
        fun `unsupported file format should throw`() {
            val tmpFile = Files.createTempFile("test", ".txt").toFile()
            val exception = assertFailsWith<IllegalStateException> {
                parseSource(tmpFile)
            }
            assertEquals(exception.message, "Unsupported file format: ${tmpFile.name}. Supported file formats are zip, tar.gz and tgz.")
        }
    }

    @Nested
    inner class ParseLcaacConfig {
        @Test
        fun `when file exists decode it`() {
            // given
            val path = File("src/test/resources/validLcaacConfig.yaml")

            // when
            val config = parseLcaacConfig(path)

            //
            assertEquals("Valid LCAAC Config", config.name)
        }

        @Test
        fun `when file does not exist return default config`() {
            // given
            val path = File("")

            // when
            val config = parseLcaacConfig(path)

            //
            assertEquals("", config.name)
        }
    }

    @Test
    fun lcaFiles_whenArchive_shouldNameFilesInsideTheArchive() {
        // given
        val dir = createTempDirectory().toFile()
        val archive = File(dir, "model.zip")
        ZipOutputStream(archive.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("src/main.lca"))
            zip.write("process broken {\n    products {\n        1 kg\n    }\n}\n".toByteArray())
            zip.closeEntry()
        }

        // when/then
        val actual = assertThrows<SyntaxException> { lcaFiles(parseSource(archive), archive) }
        assertTrue(actual.message!!.startsWith("${archive.path}!/src/main.lca:4:5: "), actual.message)
    }

    @Test
    fun lcaFiles_whenSyntaxError_shouldLocateIt() {
        // given
        val root = createTempDirectory().toFile()
        File(root, "ok.lca").writeText("process ok {\n    products {\n        1 kg ok\n    }\n}\n")
        val broken = File(root, "broken.lca")
        broken.writeText("process broken {\n    products {\n        1 kg\n    }\n}\n")

        // when/then
        val actual = assertThrows<SyntaxException> { lcaFiles(root) }
        assertEquals(1, actual.errors.size)
        assertTrue(actual.message!!.startsWith("${broken.path}:4:5: "), actual.message)
    }

    @Test
    fun withGivenGlobals_shouldDescribeTheGivenValue() {
        // given
        val global = SourcePath.Global("efficiency")
        val symbolTable = SymbolTable<BasicNumber>(sourceMap = SourceMap(mapOf(global to SourceLocation("main.lca", 2, 5))))

        // when
        val actual = symbolTable.withGivenGlobals(mapOf("efficiency" to "50 percent")).sourceMap

        // then
        assertEquals(null, actual[global])
        assertEquals("global 'efficiency' = 50 percent (given with -G)", actual.nameOf(global))
    }
}
