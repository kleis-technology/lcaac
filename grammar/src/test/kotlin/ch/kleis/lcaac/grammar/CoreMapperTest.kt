package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import kotlin.test.Test
import kotlin.test.assertEquals

/*
    TODO: Boyscout rule: Add mapper tests.
 */

class CoreMapperTest {
    private val ops = BasicOperations

    @Test
    fun datasource() {
        // given
        val content = """
            datasource source {
                location = "file.csv"
                schema {
                    "mass" = 1 kg
                    "geo" = "FR"
                }
            }
        """.trimIndent()
        val ctx = LcaLangFixture.datasource(content)
        val mapper = CoreMapper(ops)

        // when
        val actual = mapper.dataSourceDefinition(ctx)

        // then
        val expected = ECsvSource(
            location = "file.csv",
            schema = mapOf(
                "mass" to ColumnType(EQuantityScale(BasicNumber(1.0), EDataRef("kg"))),
                "geo" to ColumnType(EStringLiteral("FR")),
            )
        )
        assertEquals(expected, actual)
    }
}
