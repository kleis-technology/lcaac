package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class CoreMapperTest {
    private val ops = BasicOperations

    @Test
    fun match_withRef() {
        // given
        val ctx = LcaLangFixture.parser("""
            for_each row in source match "geo" = x {
            }
        """.trimIndent()).technoInputExchange()
        val mapper = CoreMapper(ops)

        // when
        val actual = mapper.technoInputExchange(ctx)

        // then
        val expected = ETechnoBlockForEach<BasicNumber>(
            "row",
            EFilter(EDataSourceRef("source"), mapOf(
                "geo" to EDataRef("x"),
            )),
            emptyMap(),
            emptyList(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun match_forEach_multiple() {
        // given
        val ctx = LcaLangFixture.parser("""
            for_each row in source match ("geo" = "FR", "category" = "abcd") {
            }
        """.trimIndent()).technoInputExchange()
        val mapper = CoreMapper(ops)

        // when
        val actual = mapper.technoInputExchange(ctx)

        // then
        val expected = ETechnoBlockForEach<BasicNumber>(
            "row",
            EFilter(EDataSourceRef("source"), mapOf(
                "geo" to EStringLiteral("FR"),
                "category" to EStringLiteral("abcd"),
            )),
            emptyMap(),
            emptyList(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun match_forEach() {
        // given
        val ctx = LcaLangFixture.parser("""
            for_each row in source match "geo" = "FR" {
            }
        """.trimIndent()).technoInputExchange()
        val mapper = CoreMapper(ops)

        // when
        val actual = mapper.technoInputExchange(ctx)

        // then
        val expected = ETechnoBlockForEach<BasicNumber>(
            "row",
            EFilter(EDataSourceRef("source"), mapOf("geo" to EStringLiteral("FR"))),
            emptyMap(),
            emptyList(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun assignment_regular() {
        // given
        val ctx = LcaLangFixture.parser("""
            x = 1 kg
        """.trimIndent()).assignment()
        val mapper = CoreMapper(ops)

        // when
        val actual = mapper.assignment(ctx)

        // then
        val expected = "x" to EQuantityScale(BasicNumber(1.0), EDataRef("kg"))
        assertEquals(expected, actual)
    }

    @Test
    fun assignment_defaultRecordOf() {
        // given
        val ctx = LcaLangFixture.parser("""
            x from inventory
        """.trimIndent()).assignment()
        val mapper = CoreMapper(ops)

        // when
        val actual = mapper.assignment(ctx)

        // then
        val expected = "x" to EDefaultRecordOf<BasicNumber>(EDataSourceRef("inventory"))
        assertEquals(expected, actual)
    }

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
            sum(source, "mass" * "ratio")
        """.trimIndent()).dataExpression()
        val mapper = CoreMapper(ops)

        // when
        val actual = mapper.dataExpression(ctx)

        // then
        val expected = ESumProduct<BasicNumber>(EDataSourceRef("source"), listOf("mass", "ratio"))
        assertEquals(expected, actual)
    }

    @Test
    fun columnOperation_sum_withMatching() {
        // given
        val ctx = LcaLangFixture.parser("""
            sum(source match "geo" = "FR", "mass" * "ratio")
        """.trimIndent()).dataExpression()
        val mapper = CoreMapper(ops)

        // when
        val actual = mapper.dataExpression(ctx)

        // then
        val expected = ESumProduct<BasicNumber>(
            EFilter(EDataSourceRef("source"), mapOf("geo" to EStringLiteral("FR"))),
            listOf("mass", "ratio"))
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
        val expected = EDataSource(location = "file.csv", schema = mapOf(
            "mass" to ColumnType(EQuantityScale(BasicNumber(1.0), EDataRef("kg"))),
            "geo" to ColumnType(EStringLiteral("FR")),
        ))
        assertEquals(expected, actual)
    }

    @Test
    fun technoInputExchange_blockForEach() {
        // given
        val ctx = LcaLangFixture.parser("""
            for_each row in source {
                variables {
                    x = 1 l
                }
                1 kg co2
            }
        """.trimIndent()).technoInputExchange()
        val mapper = CoreMapper(ops)

        // when
        val actual = mapper.technoInputExchange(ctx)

        // then
        val expected = ETechnoBlockForEach("row", EDataSourceRef("source"), mapOf("x" to EQuantityScale(BasicNumber(1.0), EDataRef("l"))), listOf(ETechnoBlockEntry(ETechnoExchange(
            EQuantityScale(BasicNumber(1.0), EDataRef("kg")),
            EProductSpec("co2"),
        ))))
        assertEquals(expected, actual)
    }

    @Test
    fun impactExchange_blockForEach() {
        // given
        val ctx = LcaLangFixture.parser("""
            for_each row in source {
                variables {
                    x = 1 l
                }
                1 kg co2
            }
        """.trimIndent()).impactExchange()
        val mapper = CoreMapper(ops)

        // when
        val actual = mapper.impactExchange(ctx)

        // then
        val expected = EImpactBlockForEach("row", EDataSourceRef("source"), mapOf("x" to EQuantityScale(BasicNumber(1.0), EDataRef("l"))), listOf(EImpactBlockEntry(EImpact(
            EQuantityScale(BasicNumber(1.0), EDataRef("kg")),
            EIndicatorSpec("co2"),
        ))))
        assertEquals(expected, actual)
    }

    @Test
    fun bioExchange_blockForEach() {
        // given
        val ctx = LcaLangFixture.parser("""
            for_each row in source {
                variables {
                    x = 1 l
                }
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
        val expected = EBioBlockForEach("row", EDataSourceRef("source"), mapOf("x" to EQuantityScale(BasicNumber(1.0), EDataRef("l"))), listOf(EBioBlockEntry(EBioExchange(
            EQuantityScale(BasicNumber(1.0), EDataRef("kg")),
            ESubstanceSpec("co2", compartment = "air", type = substanceType, referenceUnit = referenceUnit),
        ))))
        assertEquals(expected, actual)
    }
}
