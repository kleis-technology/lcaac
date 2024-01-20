package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.mockk
import io.mockk.mockkObject
import kotlin.test.Test
import kotlin.test.assertEquals

/*
    TODO: Boyscout rule: Add mapper tests.
 */

class CoreMapperTest {
    private val ops = BasicOperations

    @Test
    fun recordEntry() {
        // given
        val ctx = LcaLangFixture.parser("""
            row["mass"]
        """.trimIndent()).dataExpression()
        val mapper = CoreMapper(ops)

        // when
        val actual = mapper.dataExpression(ctx)

        // then
        val expected = ERecordEntry<BasicNumber>(EDataRef("row"), "mass")
        assertEquals(expected, actual)
    }

    @Test
    fun columnOperation_sum() {
        // given
        val ctx = LcaLangFixture.parser("""
            sum(source, "mass")
        """.trimIndent()).dataExpression()
        val mapper = CoreMapper(ops)

        // when
        val actual = mapper.dataExpression(ctx)

        // then
        val expected = ESum<BasicNumber>("source", "mass")
        assertEquals(expected, actual)
    }

    @Test
    fun datasource() {
        // given
        val ctx = LcaLangFixture.parser("""
            datasource source {
                location = "file.csv"
                schema {
                    "mass" = 1 kg
                    "geo" = "FR"
                }
            }
        """.trimIndent()).dataSourceDefinition()
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

    @Test
    fun technoInputExchange_blockForEach() {
        // given
        val ctx = LcaLangFixture.parser("""
            for_each row in source {
                1 kg co2
            }
        """.trimIndent()).technoInputExchange()
        val mapper = CoreMapper(ops)

        // when
        val actual = mapper.technoInputExchange(ctx)

        // then
        val expected = ETechnoBlockForEach(
            "row",
            "source",
            listOf(
                ETechnoBlockEntry(
                    ETechnoExchange(
                        EQuantityScale(BasicNumber(1.0), EDataRef("kg")),
                        EProductSpec("co2"),
                    )
                )
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun impactExchange_blockForEach() {
        // given
        val ctx = LcaLangFixture.parser("""
            for_each row in source {
                1 kg co2
            }
        """.trimIndent()).impactExchange()
        val mapper = CoreMapper(ops)

        // when
        val actual = mapper.impactExchange(ctx)

        // then
        val expected = EImpactBlockForEach(
            "row",
            "source",
            listOf(
                EImpactBlockEntry(
                    EImpact(
                        EQuantityScale(BasicNumber(1.0), EDataRef("kg")),
                        EIndicatorSpec("co2"),
                    )
                )
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun bioExchange_blockForEach() {
        // given
        val ctx = LcaLangFixture.parser("""
            for_each row in source {
                1 kg co2(compartment="air")
            }
        """.trimIndent()).bioExchange()
        val symbolTable = mockk<SymbolTable<BasicNumber>>()
        val substanceType = SubstanceType.EMISSION
        val mapper = CoreMapper(ops)

        // when
        val actual = mapper.bioExchange(ctx, symbolTable, substanceType)

        // then
        val referenceUnit = EUnitOf(EQuantityClosure(symbolTable, EQuantityScale(BasicNumber(1.0), EDataRef("kg"))))
        val expected = EBioBlockForEach(
            "row",
            "source",
            listOf(
                EBioBlockEntry(
                    EBioExchange(
                        EQuantityScale(BasicNumber(1.0), EDataRef("kg")),
                        ESubstanceSpec("co2", compartment = "air", type=substanceType, referenceUnit = referenceUnit),
                    )
                )
            )
        )
        assertEquals(expected, actual)
    }
}
