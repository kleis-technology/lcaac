package ch.kleis.lcaac.core.datasource.csv

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture
import ch.kleis.lcaac.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.InputStream
import kotlin.test.assertEquals

class CsvConnectorTest {
    private fun mockFileLoader(): (String) -> InputStream {
        val content = """
            geo,n_items,mass
            FR,1,1
            FR,2,2
            FR,1,2
            UK,2,2
            UK,1,2
            UK,2,1
        """.trimIndent()
        return { content.byteInputStream() }
    }

    private val ops = BasicOperations

    @Nested
    inner class CsvCache {
        @Test
        fun whenSameLocationsShouldCacheCsvRecords() {
            // Given
            val location = "source.csv"
            val spiedFileLoader: (String) -> InputStream = mockk()

            every { spiedFileLoader.invoke(location) } answers {
                mockFileLoader().invoke(location)
            }

            val sut = CsvConnector(mockk(), ops, spiedFileLoader)

            val source = DataSourceValue(
                config = DataSourceConfig(
                    name = "source",
                    location = location,
                ),
                schema = mapOf(
                    "geo" to StringValue("FR"),
                    "n_items" to QuantityValueFixture.oneUnit,
                    "mass" to QuantityValueFixture.oneKilogram,
                ),
                filter = mapOf(
                    "geo" to StringValue("FR")
                )
            )

            val config = DataSourceConfig(name = "source", location = location)

            // When
            sut.getAll(mockk(), config, source)
            sut.getAll(mockk(), config, source)

            // Then
            verify(exactly = 1) { spiedFileLoader.invoke(location) }
        }

        @Test
        fun whenDifferentLocationsShouldCacheDifferentCsvRecords() {
            // Given
            val spiedFileLoader: (String) -> InputStream = mockk()

            val location1 = "source.csv"
            every { spiedFileLoader.invoke(location1) } answers {
                mockFileLoader().invoke(location1)
            }

            val source1 = DataSourceValue(
                config = DataSourceConfig(
                    name = "source",
                    location = location1,
                ),
                schema = mapOf(
                    "geo" to StringValue("FR"),
                    "n_items" to QuantityValueFixture.oneUnit,
                    "mass" to QuantityValueFixture.oneKilogram,
                ),
                filter = mapOf(
                    "geo" to StringValue("FR")
                )
            )

            val config1 = DataSourceConfig(name = "source", location = location1)

            val location2 = "source.csv"
            every { spiedFileLoader.invoke(location2) } answers {
                mockFileLoader().invoke(location2)
            }

            val source2 = DataSourceValue(
                config = DataSourceConfig(
                    name = "source",
                    location = location2,
                ),
                schema = mapOf(
                    "geo" to StringValue("FR"),
                    "n_items" to QuantityValueFixture.oneUnit,
                    "mass" to QuantityValueFixture.oneKilogram,
                ),
                filter = mapOf(
                    "geo" to StringValue("FR")
                )
            )

            val config2 = DataSourceConfig(name = "source", location = location2)

            val sut = CsvConnector(mockk(), ops, spiedFileLoader)

            // When
            sut.getAll(mockk(), config1, source1)
            sut.getAll(mockk(), config2, source2)

            // Then
            verify(exactly = 1) { spiedFileLoader.invoke(location1) }
            verify(exactly = 1) { spiedFileLoader.invoke(location2) }
        }
    }

    @Test
    fun getAll() {
        // given
        val connector = CsvConnector(
            mockk(),
            ops,
            mockFileLoader(),
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = "source",
                location = "source.csv",
            ),
            schema = mapOf(
                "geo" to StringValue("FR"),
                "n_items" to QuantityValueFixture.oneUnit,
                "mass" to QuantityValueFixture.oneKilogram,
            ),
            filter = mapOf(
                "geo" to StringValue("FR")
            )
        )
        val config = DataSourceConfig(
            name = "source",
            location = "source.csv",
        )

        // when
        val actual = connector.getAll(mockk(), config, source).toList()

        // then
        val expected = listOf(
            ERecord(mapOf(
                "geo" to EStringLiteral("FR"),
                "n_items" to QuantityFixture.oneUnit,
                "mass" to QuantityFixture.oneKilogram,
            )),
            ERecord(mapOf(
                "geo" to EStringLiteral("FR"),
                "n_items" to QuantityFixture.twoUnits,
                "mass" to QuantityFixture.twoKilograms,
            )),
            ERecord(mapOf(
                "geo" to EStringLiteral("FR"),
                "n_items" to QuantityFixture.oneUnit,
                "mass" to QuantityFixture.twoKilograms,
            )),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun getFirst() {
        // given
        val connector = CsvConnector(
            mockk(),
            ops,
            mockFileLoader(),
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = "source",
                location = "source.csv",
            ),
            schema = mapOf(
                "geo" to StringValue("FR"),
                "n_items" to QuantityValueFixture.oneUnit,
                "mass" to QuantityValueFixture.oneKilogram,
            ),
            filter = mapOf(
                "geo" to StringValue("UK")
            )
        )
        val config = DataSourceConfig(
            name = "source",
            location = "source.csv",
        )

        // when
        val actual = connector.getFirst(mockk(), config, source)

        // then
        val expected = ERecord(mapOf(
            "geo" to EStringLiteral("UK"),
            "n_items" to QuantityFixture.twoUnits,
            "mass" to QuantityFixture.twoKilograms,
        ))
        assertEquals(expected, actual)
    }

    @Test
    fun getFirst_whenEmpty_throwEvaluatorException() {
        // given
        val connector = CsvConnector(
            mockk(),
            ops,
            mockFileLoader(),
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = "source",
                location = "source.csv",
            ),
            schema = mapOf(
                "geo" to StringValue("FR"),
                "n_items" to QuantityValueFixture.oneUnit,
                "mass" to QuantityValueFixture.oneKilogram,
            ),
            filter = mapOf(
                "geo" to StringValue("A_NON_EXISTING_COUNTRY")
            )
        )
        val config = DataSourceConfig(
            name = "source",
            location = "source.csv",
        )

        // when/then
        val e = assertThrows<EvaluatorException> { connector.getFirst(mockk(), config, source) }
        assertEquals("no record found in datasource 'source' [source.csv] matching {geo=A_NON_EXISTING_COUNTRY}", e.message)
    }
}
