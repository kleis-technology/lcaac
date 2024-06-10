package ch.kleis.lcaac.core.datasource.csv

import ch.kleis.lcaac.core.config.ConnectorConfig
import org.junit.jupiter.api.Assertions.*
import java.io.File
import kotlin.io.path.Path
import kotlin.test.Test

class CsvConnectorConfigKtTest {
    @Test
    fun csv() {
        // given
        val workingDirectory = "/foo"
        val config = ConnectorConfig(
            name = "csv",
            options = mapOf(
                "directory" to "bar"
            )
        )

        // when
        val actual = config.csv(workingDirectory)

        // then
        val expected = CsvConnectorConfig(
            directory = Path("/foo/bar").toFile(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun csv_whenTwoAbsolutePaths() {
        // given
        val workingDirectory = "/foo"
        val config = ConnectorConfig(
            name = "csv",
            options = mapOf(
                "directory" to "/foo/bar"
            )
        )

        // when
        val actual = config.csv(workingDirectory)

        // then
        val expected = CsvConnectorConfig(
            directory = Path("/foo/bar").toFile(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun csv_whenRelativeSubfolder() {
        // given
        val workingDirectory = "/foo"
        val config = ConnectorConfig(
            name = "csv",
            options = mapOf(
                "directory" to "baz/bar"
            )
        )

        // when
        val actual = config.csv(workingDirectory)

        // then
        val expected = CsvConnectorConfig(
            directory = Path("/foo/baz/bar").toFile(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun csv_whenIncompatibleAbsolutePaths_thenTrustLocation() {
        // given
        val workingDirectory = "/foo/dot"
        val config = ConnectorConfig(
            name = "csv",
            options = mapOf(
                "directory" to "/baz/bar"
            )
        )

        // when
        val actual = config.csv(workingDirectory)

        // then
        val expected = CsvConnectorConfig(
            directory = Path("/baz/bar").toFile(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun csv_whenEmptyOptions() {
        // given
        val workingDirectory = "/foo/dot"
        val config = ConnectorConfig(
            name = "csv",
            options = emptyMap(),
        )

        // when
        val actual = config.csv(workingDirectory)

        // then
        val expected = CsvConnectorConfig(
            directory = Path("/foo/dot").toFile(),
        )
        assertEquals(expected, actual)
    }
}
