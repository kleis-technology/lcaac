package ch.kleis.lcaac.cli.cmd

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import org.junit.jupiter.api.Nested
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SharedOptionTest {
    private class DummyCommand : CliktCommand() {
        val configFile by configFileOption()
        val source by sourceOption()
        val file by fileOption("dummy")
        val labels by labelsOption("dummy")
        val arguments by argumentsOption("dummy")
        val globals by globalsOption("dummy")
        override fun run() {
            // no-op
        }
    }

    @Nested
    inner class ConfigFileOption {
        @Test
        fun `when no option is provided uses default config file`() {
            // given
            val cmd = DummyCommand()

            // when
            cmd.parse(emptyArray())

            // then
            val expected = File("./$defaultLcaacFilename")
            assertEquals(expected, cmd.configFile)
        }

        @Test
        fun `when directory provided should throw`() {
            // given
            val tmpDir = createTempDirectory().toAbsolutePath().toString()
            val cmd = DummyCommand()

            // when + then
            val exception = assertFailsWith<BadParameterValue> {
                cmd.parse(arrayOf("--config", tmpDir))
            }
            assertEquals(exception.message, "file \"$tmpDir\" is a directory.")
        }

        @Test
        fun `when valid file provided should parse successfully`() {
            // given
            val tmpFile = createTempFile().toAbsolutePath().toString()
            val cmd = DummyCommand()

            // when
            cmd.parse(arrayOf("--config", tmpFile))

            assertEquals(tmpFile, cmd.configFile.absoluteFile.toString())
        }
    }

    @Nested
    inner class SourceOption {
        @Test
        fun `when no option is provided uses current work directory`() {
            // given
            val cmd = DummyCommand()

            // when
            cmd.parse(emptyArray())

            // then
            val expected = File(".")
            assertEquals(expected, cmd.source)
        }

        @Test
        fun `when valid file provided should parse successfully`() {
            // given
            val tmpFile = createTempFile().toAbsolutePath().toString()
            val cmd = DummyCommand()

            // when
            cmd.parse(arrayOf("--source", tmpFile))

            assertEquals(tmpFile, cmd.source.absoluteFile.toString())
        }

        @Test
        fun `when valid directory provided should parse successfully`() {
            // given
            val tmpFile = createTempDirectory().toAbsolutePath().toString()
            val cmd = DummyCommand()

            // when
            cmd.parse(arrayOf("--source", tmpFile))

            assertEquals(tmpFile, cmd.source.absoluteFile.toString())
        }
    }

    @Nested
    inner class FileOption {
        @Test
        fun `when directory provided should throw`() {
            // given
            val tmpDir = createTempDirectory().toAbsolutePath().toString()
            val cmd = DummyCommand()

            // when + then
            val exception = assertFailsWith<BadParameterValue> {
                cmd.parse(arrayOf("--file", tmpDir))
            }
            assertEquals(exception.message, "file \"$tmpDir\" is a directory.")
        }

        @Test
        fun `when valid file provided should parse successfully`() {
            // given
            val tmpFile = createTempFile().toAbsolutePath().toString()
            val cmd = DummyCommand()

            // when
            cmd.parse(arrayOf("--file", tmpFile))

            assertEquals(tmpFile, cmd.file?.absoluteFile.toString())
        }

        @Test
        fun `when display help should write example with the corresponding command`() {
            // given
            val cmd = DummyCommand()

            // when
            val helpText = cmd.getFormattedHelp()

            assertTrue(
                helpText!!.contains("-f, --file=<path>        CSV file with parameter values. Example: lcaac dummy"),
                "Help for -f options should contain an example with the corresponding command."
            )
        }
    }

    @Nested
    inner class LabelsOption {
        @Test
        fun `when single label provided should parse correctly`() {
            // given
            val cmd = DummyCommand()

            // when
            cmd.parse(arrayOf("-l", """model="ABC""""))

            // then
            val expected = mapOf("model" to "\"ABC\"")
            assertEquals(expected, cmd.labels)
        }

        @Test
        fun `when multiple arguments provided should parse correctly`() {
            // given
            val cmd = DummyCommand()

            // when
            cmd.parse(arrayOf("-l", """model="ABC"""", "-l", """geo=UK"""))

            // then
            val expected = mapOf("model" to "\"ABC\"", "geo" to "UK")
            assertEquals(expected, cmd.labels)
        }

        @Test
        fun `when display help should write example with the corresponding command`() {
            // given
            val cmd = DummyCommand()

            // when
            val helpText = cmd.getFormattedHelp()

            assertTrue(
                helpText!!.contains("-l, --label=<value>      Specify a process label as a key value pair.\n" +
                        "                           Example: lcaac dummy"),
                "Help for -l options should contain an example with the corresponding command."
            )
        }
    }

    @Nested
    inner class ArgumentsOption {
        @Test
        fun `when single argument provided should parse correctly`() {
            // given
            val cmd = DummyCommand()

            // when
            cmd.parse(arrayOf("-D", """x="12 kg""""))

            // then
            val expected = mapOf("x" to "\"12 kg\"")
            assertEquals(expected, cmd.arguments)
        }

        @Test
        fun `when multiple arguments provided should parse correctly`() {
            // given
            val cmd = DummyCommand()

            // when
            cmd.parse(arrayOf("-D", """x="12 kg"""", "-D", """y=42"""))

            // then
            val expected = mapOf("x" to "\"12 kg\"", "y" to "42")
            assertEquals(expected, cmd.arguments)
        }

        @Test
        fun `when display help should write example with the corresponding command`() {
            // given
            val cmd = DummyCommand()

            // when
            val helpText = cmd.getFormattedHelp()

            assertTrue(
                helpText!!.contains("-D, --parameter=<value>  Override parameter value as a key value pair.\n" +
                        "                           Example: lcaac dummy <process name> -D"),
                "Help for -l options should contain an example with the corresponding command."
            )
        }
    }

    @Nested
    inner class GlobalsOption {
        @Test
        fun `when single global provided should parse correctly`() {
            // given
            val cmd = DummyCommand()

            // when
            cmd.parse(arrayOf("-G", """x="12 kg""""))

            // then
            val expected = mapOf("x" to "\"12 kg\"")
            assertEquals(expected, cmd.globals)
        }

        @Test
        fun `when multiple globals provided should parse correctly`() {
            // given
            val cmd = DummyCommand()

            // when
            cmd.parse(arrayOf("-G", """x="12 kg"""", "-G", """y=42"""))

            // then
            val expected = mapOf("x" to "\"12 kg\"", "y" to "42")
            assertEquals(expected, cmd.globals)
        }

        @Test
        fun `when display help should write example with the corresponding command`() {
            // given
            val cmd = DummyCommand()

            // when
            val helpText = cmd.getFormattedHelp()

            assertTrue(
                helpText!!.contains("-G, --global=<value>     Override global variable as a key value pair.\n" +
                        "                           Example: lcaac dummy"),
                "Help for -l options should contain an example with the corresponding command."
            )
        }
    }
}