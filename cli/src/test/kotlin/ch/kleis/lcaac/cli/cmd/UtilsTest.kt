package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.EQuantityMul
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.prelude.Prelude
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue


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
    fun parseQuantityWithDefaultUnit_invalidExpression() {
        // given
        val s = "a@bc"
        val defaultUnit = Prelude.unitMap<BasicNumber>()["kg"]!!

        // when/then
        val actual = assertThrows<EvaluatorException> { smartParseQuantityWithDefaultUnit(s, defaultUnit) }
        assertEquals("'a@bc' is not a valid quantity", actual.message)
    }

    @Test
    fun parseQuantityWithDefaultUnit_invalidExpression_multipleParts() {
        // given
        val s = "12 3 4"
        val defaultUnit = Prelude.unitMap<BasicNumber>()["kg"]!!

        // when/then
        val actual = assertThrows<EvaluatorException> { smartParseQuantityWithDefaultUnit(s, defaultUnit) }
        assertEquals("'12 3 4' is not a valid quantity", actual.message)
    }

    @Test
    fun parseQuantityWithDefaultUnit_invalidExpression_invalidUnit() {
        // given
        val s = "12 $3"
        val defaultUnit = Prelude.unitMap<BasicNumber>()["kg"]!!

        // when/then
        val actual = assertThrows<EvaluatorException> { smartParseQuantityWithDefaultUnit(s, defaultUnit) }
        assertEquals("'12 \$3' is not a valid quantity", actual.message)
    }

    @Test
    fun parseQuantityWithDefaultUnit_whenNumber() {
        // given
        val s = "12.0"
        val defaultUnit = Prelude.unitMap<BasicNumber>()["kg"]!!

        // when
        val actual = smartParseQuantityWithDefaultUnit(s, defaultUnit)

        // then
        assertEquals(EQuantityScale(BasicNumber(12.0), defaultUnit), actual)
    }

    @Test
    fun parseQuantityWithDefaultUnit_whenExpression() {
        // given
        val s = "12.0 kg"
        val defaultUnit = Prelude.unitMap<BasicNumber>()["kg"]!!

        // when
        val actual = smartParseQuantityWithDefaultUnit(s, defaultUnit)

        // then
        assertEquals(EQuantityScale(BasicNumber(12.0), EDataRef("kg")), actual)
    }

    @Test
    fun parseQuantityWithDefaultUnit_whenComplexExpression() {
        // given
        val s = "12.0 kg * hour"
        val kg = Prelude.unitMap<BasicNumber>()["kg"]!!
        val hour = Prelude.unitMap<BasicNumber>()["hour"]!!
        val defaultUnit = EQuantityMul(kg, hour)

        // when
        val actual = smartParseQuantityWithDefaultUnit(s, defaultUnit)

        // then
        assertEquals(
            EQuantityScale(
                BasicNumber(12.0),
                EQuantityMul(EDataRef("kg"), EDataRef("hour"))),
            actual)
    }
}
