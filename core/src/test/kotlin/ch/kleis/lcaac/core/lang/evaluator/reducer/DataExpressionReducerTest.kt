package ch.kleis.lcaac.core.lang.evaluator.reducer

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.fixture.DimensionFixture
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture
import ch.kleis.lcaac.core.lang.fixture.UnitFixture
import ch.kleis.lcaac.core.lang.register.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.math.pow
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DataExpressionReducerTest {
    private val ops = BasicOperations
    private val sourceOps = mockk<DataSourceOperations<BasicNumber>>()

    /*
        Data Source
     */

    @Test
    fun reduceDataSource_withRefInSchema() {
        // given
        val dataSource = EDataSource(
            config = DataSourceConfig(
                name = "source",
                location = "source.csv",
            ),
            schema = mapOf(
                "col" to EQuantityScale(BasicNumber(2.0), EDataRef("foo")),
            ),
        )
        val dataSourceRegister = DataSourceRegister.from(mapOf(DataSourceKey("source") to dataSource))
        val expression = EDataSourceRef<BasicNumber>("source")
        val dataRegister = DataRegister.from(mapOf(
            DataKey("foo") to QuantityFixture.oneKilogram as DataExpression<BasicNumber>,
        ))
        val reducer = DataExpressionReducer(dataRegister, dataSourceRegister, ops, sourceOps)

        // when
        val actual = reducer.reduceDataSource(expression)

        // then
        val expected = EDataSource(
            config = DataSourceConfig(
                name = "source",
                location = "source.csv",
            ),
            schema = mapOf(
                "col" to QuantityFixture.twoKilograms,
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduceDataSource_filterWithRef() {
        // given
        val dataSource = EDataSource<BasicNumber>(
            config = DataSourceConfig(
                name = "source",
                location = "source.csv",
            ),
            schema = mapOf(
                "id" to EStringLiteral("foo"),
            ),
        )
        val dataRegister = DataRegister.from(mapOf(
            DataKey("x") to EStringLiteral<BasicNumber>("bar") as DataExpression<BasicNumber>,
        ))
        val dataSourceRegister = DataSourceRegister.from(mapOf(DataSourceKey("source") to dataSource))
        val expression = EFilter<BasicNumber>(EDataSourceRef("source"), mapOf("id" to EDataRef("x")))
        val reducer = DataExpressionReducer(dataRegister, dataSourceRegister, ops, sourceOps)

        // when
        val actual = reducer.reduceDataSource(expression)

        // then
        val expected = dataSource.copy(
            filter = mapOf("id" to EStringLiteral("bar"))
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduceDataSource_dataSourceRef() {
        // given
        val dataSource = EDataSource(
            config = DataSourceConfig(
                name = "source",
                location = "source.csv",
            ),
            schema = mapOf(
                "volume" to QuantityFixture.oneLitre,
                "mass" to QuantityFixture.oneKilogram,
            ),
        )
        val dataSourceRegister = DataSourceRegister.from(mapOf(DataSourceKey("source") to dataSource))
        val expression = EDataSourceRef<BasicNumber>("source")
        val reducer = DataExpressionReducer(DataRegister.empty(), dataSourceRegister, ops, sourceOps)

        // when
        val actual = reducer.reduceDataSource(expression)

        // then
        assertEquals(dataSource, actual)
    }

    @Test
    fun reduceDataSource_filter() {
        // given
        val dataSource = EDataSource(
            config = DataSourceConfig(
                name = "source",
                location = "source.csv",
            ),
            schema = mapOf(
                "label" to EStringLiteral("value"),
                "volume" to QuantityFixture.oneLitre,
                "mass" to QuantityFixture.oneKilogram,
            ),
        )
        val dataSourceRegister = DataSourceRegister.from(mapOf(DataSourceKey("source") to dataSource))
        val expression = EFilter(
            EDataSourceRef<BasicNumber>("source"),
            mapOf(
                "label" to EStringLiteral("some_value"),
            )
        )
        val reducer = DataExpressionReducer(DataRegister.empty(), dataSourceRegister, ops, sourceOps)

        // when
        val actual = reducer.reduceDataSource(expression)

        // then
        val expected = dataSource.copy(filter = mapOf("label" to EStringLiteral("some_value")))
        assertEquals(expected, actual)
    }

    @Test
    fun reduceDataSource_filter_accumulation() {
        // given
        val dataSource = EDataSource(
            config = DataSourceConfig(
                name = "source",
                location = "source.csv",
            ),
            schema = mapOf(
                "label" to EStringLiteral("value"),
                "geo" to EStringLiteral("FR"),
                "volume" to QuantityFixture.oneLitre,
                "mass" to QuantityFixture.oneKilogram,
            ), filter = mapOf("geo" to EStringLiteral("UK")))
        val dataSourceRegister = DataSourceRegister.from(mapOf(DataSourceKey("source") to dataSource))
        val expression = EFilter(EDataSourceRef<BasicNumber>("source"), mapOf("label" to EStringLiteral("some_value")))
        val reducer = DataExpressionReducer(DataRegister.empty(), dataSourceRegister, ops, sourceOps)

        // when
        val actual = reducer.reduceDataSource(expression)

        // then
        val expected = dataSource.copy(filter = mapOf(
            "geo" to EStringLiteral("UK"),
            "label" to EStringLiteral("some_value"),
        ))
        assertEquals(expected, actual)
    }

    @Test
    fun reduceDataSource_filter_whenInvalid() {
        // given
        val dataSource = EDataSource(
            config = DataSourceConfig(
                name = "source",
                location = "source.csv",
            ),
            schema = mapOf(
                "volume" to QuantityFixture.oneLitre,
                "mass" to QuantityFixture.oneKilogram,
            ))
        val dataSourceRegister = DataSourceRegister.from(mapOf(DataSourceKey("source") to dataSource))
        val expression = EFilter(EDataSourceRef<BasicNumber>("source"), mapOf("volume" to EStringLiteral("some_value")))
        val reducer = DataExpressionReducer(DataRegister.empty(), dataSourceRegister, ops, sourceOps)

        // when
        val e = assertThrows<EvaluatorException> { reducer.reduceDataSource(expression) }
        assertEquals("data source 'source': cannot match on numeric column(s) [volume]", e.message)
    }

    /*
        Column Operations
     */

    @Test
    fun sumProduct() {
        // given
        val expression = ESumProduct<BasicNumber>(EDataSourceRef("source"), listOf("volume", "mass"))
        val dataSource = EDataSource(
            config = DataSourceConfig(
                name = "source",
                location = "source.csv",
            ),
            schema = mapOf(
                "volume" to QuantityFixture.oneLitre,
                "mass" to QuantityFixture.oneKilogram,
            ),
        )
        val sops = mockk<DataSourceOperations<BasicNumber>>()
        val total = EQuantityScale(BasicNumber(1.0), EUnitLiteral(
            UnitFixture.l.symbol.multiply(UnitFixture.kg.symbol),
            1.0,
            UnitFixture.l.dimension.multiply(UnitFixture.kg.dimension),
        ))
        val dataSourceValue = with(ToValue(ops)) {
            dataSource.toValue()
        }
        every { sops.sumProduct(dataSourceValue, listOf("volume", "mass")) } returns total
        val reducer = DataExpressionReducer(
            DataRegister.empty(),
            DataSourceRegister.from(mapOf(DataSourceKey("source") to dataSource)),
            ops,
            sops,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        assertEquals(total, actual)
        verify { sops.sumProduct(dataSourceValue, listOf("volume", "mass")) }
    }


    @Test
    fun sum_invalidDataSourceRef() {
        // given
        val expression = ESumProduct<BasicNumber>(EDataSourceRef("foo"), listOf("mass"))
        val dataSource = EDataSource(
            config = DataSourceConfig(
                name = "source",
                location = "source.csv",
            ),
            schema = mapOf(
                "mass" to QuantityFixture.oneKilogram,
            ),
        )
        val sops = mockk<DataSourceOperations<BasicNumber>>()
        val total = QuantityFixture.twoKilograms
        val dataSourceValue = with(ToValue(ops)) {
            dataSource.toValue()
        }
        every { sops.sumProduct(dataSourceValue, listOf("mass")) } returns total
        val reducer = DataExpressionReducer(
            DataRegister.empty(),
            DataSourceRegister.from(mapOf(DataSourceKey("source") to dataSource)),
            ops,
            sops,
        )

        // when/then
        val e = assertThrows<EvaluatorException> { reducer.reduce(expression) }
        assertEquals("unknown data source 'foo'", e.message)
    }

    /*
        RECORD
     */

    @Test
    fun reduce_whenFirstRecordOfDataSourceRef() {
        val dataSource = EDataSource(
            config = DataSourceConfig(
                name = "source",
                location = "source.csv",
            ),
            schema = mapOf(
                "mass" to QuantityFixture.oneKilogram
            )
        )
        val firstRecordOf = EFirstRecordOf<BasicNumber>(EDataSourceRef("source"))
        val reducer = DataExpressionReducer(
            DataRegister.empty(),
            DataSourceRegister.from(mapOf(
                DataSourceKey("source") to dataSource
            )),
            ops,
            sourceOps,
        )
        val expected = ERecord(
            mapOf(
                "mass" to QuantityFixture.twoKilograms
            )
        )
        val dataSourceValue = with(ToValue(ops)) { dataSource.toValue() }
        every { sourceOps.getFirst(dataSourceValue) } returns expected

        // when
        val actual = reducer.reduce(firstRecordOf)

        // then
        assertEquals(expected, actual)
        verify { sourceOps.getFirst(dataSourceValue) }
    }

    @Test
    fun reduce_whenDefaultRecordOfDataSourceRef() {
        // given
        val dataSource = EDataSource<BasicNumber>(
            config = DataSourceConfig(
                name = "source",
                location = "source.csv",
            ),
            schema = mapOf(
                "x" to EDataRef("a"),
            ))
        val record = EDefaultRecordOf<BasicNumber>(EDataSourceRef("source"))
        val reducer = DataExpressionReducer(
            Register.from(mapOf(
                DataKey("a") to QuantityFixture.oneKilogram,
            )),
            Register.from(mapOf(
                DataSourceKey("source") to dataSource,
            )),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(record)

        // then
        val expected = ERecord(mapOf(
            "x" to QuantityFixture.oneKilogram,
        ))
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenDefaultRecordOfDataSourceRef_invalidRef() {
        // given
        val dataSource = EDataSource<BasicNumber>(
            config = DataSourceConfig(
                name = "source",
                location = "source.csv",
            ),
            schema = mapOf(
                "x" to EDataRef("a"),
            ))
        val record = EDefaultRecordOf<BasicNumber>(EDataSourceRef("foo"))
        val reducer = DataExpressionReducer(
            Register.from(mapOf(
                DataKey("a") to QuantityFixture.oneKilogram,
            )),
            Register.from(mapOf(
                DataSourceKey("source") to dataSource,
            )),
            ops,
            sourceOps,
        )

        // when/then
        val e = assertThrows<EvaluatorException> { reducer.reduce(record) }
        assertEquals("unknown data source 'foo'", e.message)
    }

    @Test
    fun reduce_whenRecord() {
        // given
        val q = QuantityFixture.oneKilogram
        val record = ERecord<BasicNumber>(mapOf("mass" to EDataRef("m")))
        val reducer = DataExpressionReducer(
            Register.from(mapOf(DataKey("m") to q)),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(record)

        // then
        val expected = ERecord(mapOf("mass" to q))
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenRecordEntry_withRef() {
        // given
        val q = QuantityFixture.oneKilogram
        val record = ERecord<BasicNumber>(mapOf("mass" to EDataRef("m")))
        val entry = ERecordEntry<BasicNumber>(EDataRef("my_map"), "mass")
        val reducer = DataExpressionReducer(
            Register.from(mapOf(
                DataKey("m") to q,
                DataKey("my_map") to record,
            )),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(entry)

        // then
        assertEquals(q, actual)
    }

    @Test
    fun reduce_whenRecordEntry() {
        // given
        val q = QuantityFixture.oneKilogram
        val record = ERecord<BasicNumber>(mapOf("mass" to EDataRef("m")))
        val entry = ERecordEntry(record, "mass")
        val reducer = DataExpressionReducer(
            Register.from(mapOf(DataKey("m") to q)),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(entry)

        // then
        assertEquals(q, actual)
    }

    @Test
    fun reduce_whenRecordEntry_invalidIndex() {
        // given
        val q = QuantityFixture.oneKilogram
        val record = ERecord<BasicNumber>(mapOf("mass" to EDataRef("m")))
        val entry = ERecordEntry(record, "foo")
        val reducer = DataExpressionReducer(
            Register.from(mapOf(DataKey("m") to q)),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val e = assertThrows<EvaluatorException> { reducer.reduce(entry) }
        assertEquals("invalid index: 'foo' not in [mass]", e.message)
    }

    /*
        QUANTITIES
     */

    @Test
    fun reduce_whenUnitLiteral_shouldSetToNormalForm() {
        // given
        val unit = EUnitLiteral<BasicNumber>(UnitSymbol.of("a"), 123.0, Dimension.of("a"))
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(unit)

        // then
        val expected = EQuantityScale(ops.pure(1.0), unit)
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenScale_shouldReduce() {
        // given
        val innerQuantity = QuantityFixture.oneKilogram
        val quantity = EQuantityScale(ops.pure(2.0), innerQuantity)
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(quantity)

        // then
        val expected = QuantityFixture.twoKilograms
        assertEquals(expected, actual)
    }

    @Test
    fun test_reduce_whenScaleOfScale_shouldReduce() {
        // given
        val quantity = EQuantityScale(ops.pure(1.0), QuantityFixture.twoKilograms)
        val reducer = DataExpressionReducer(Register.empty(), Register.empty(), ops, sourceOps)
        val expected = EQuantityScale(ops.pure(2.0), UnitFixture.kg)

        // when
        val actual = reducer.reduce(quantity)

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenScaleAndUnboundRef_shouldDoNothing() {
        // given
        val innerQuantity = EDataRef<BasicNumber>("a")
        val quantity = EQuantityScale(ops.pure(2.0), innerQuantity)
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(quantity)

        // then
        assertEquals(quantity, actual)
    }

    @Test
    fun reduce_whenLiteral_shouldReduceUnit() {
        // given
        val quantityEnvironment = DataRegister(mapOf(DataKey("kg") to UnitFixture.kg))
        val quantity = EQuantityScale(ops.pure(1.0), EDataRef("kg"))
        val reducer = DataExpressionReducer(quantityEnvironment, Register.empty(), ops, sourceOps)

        // when
        val actual = reducer.reduce(quantity)

        // then
        val expected = QuantityFixture.oneKilogram
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_add_whenSameDimension_shouldAddAndSelectBiggestScale() {
        // given
        val a = EQuantityScale(ops.pure(2.0), UnitFixture.kg)
        val b = EQuantityScale(ops.pure(1000.0), UnitFixture.g)
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(EQuantityAdd(a, b))

        // then
        val expected = EQuantityScale(ops.pure(3.0), UnitFixture.kg)
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_add_whenSameUnitLiteral_shouldAddAsScale() {
        // given
        val a = UnitFixture.kg
        val b = UnitFixture.kg
        val expected = EQuantityScale(ops.pure(2.0), UnitFixture.kg)
        val reducer = DataExpressionReducer(Register.empty(), Register.empty(), ops, sourceOps)

        // when
        val actual = reducer.reduce(EQuantityAdd(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_add_whenNotSameDimension_shouldThrowEvaluatorException() {
        // given
        val a = EQuantityScale(ops.pure(2.0), UnitFixture.kg)
        val b = EQuantityScale(ops.pure(1000.0), UnitFixture.m)
        val quantity = EQuantityAdd(a, b)
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when/then
        assertFailsWith(EvaluatorException::class, "incompatible dimensions: mass vs length in left=2.0 kg and right=1000.0 m") { reducer.reduce(quantity) }
    }

    @Test
    fun reduce_add_whenSameDimensionLiteral_shouldAddAsScale() {
        // given
        val a = UnitFixture.g
        val b = UnitFixture.kg
        val expected = EQuantityScale(ops.pure(1.001), EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass))
        val reducer = DataExpressionReducer(Register.empty(), Register.empty(), ops, sourceOps)

        // when
        val actual = reducer.reduce(EQuantityAdd(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_add_whenOnlyOneScaleLeft_shouldAddAsScale() {
        // given
        val a = QuantityFixture.twoKilograms
        val b = UnitFixture.kg
        val expected = EQuantityScale(ops.pure(3.0), EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass))
        val reducer = DataExpressionReducer(Register.empty(), Register.empty(), ops, sourceOps)

        // when
        val actual = reducer.reduce(EQuantityAdd(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_add_whenOnlyOneScaleRight_shouldAddAsScale() {
        // given
        val a = UnitFixture.kg
        val b = QuantityFixture.twoKilograms
        val expected = EQuantityScale(ops.pure(3.0), EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass))
        val reducer = DataExpressionReducer(Register.empty(), Register.empty(), ops, sourceOps)

        // when
        val actual = reducer.reduce(EQuantityAdd(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_sub_whenSameDimension_shouldSubAndSelectBiggestScale() {
        // given
        val a = EQuantityScale(ops.pure(2.0), UnitFixture.kg)
        val b = EQuantityScale(ops.pure(1000.0), UnitFixture.g)
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(EQuantitySub(a, b))

        // then
        val expected = EQuantityScale(ops.pure(1.0), UnitFixture.kg)
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_sub_whenNotSameDimension_shouldThrowEvaluatorException() {
        // given
        val a = EQuantityScale(ops.pure(2.0), UnitFixture.kg)
        val b = EQuantityScale(ops.pure(1000.0), UnitFixture.m)
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )
        val eQuantitySub = EQuantitySub(a, b)

        // when/then
        assertFailsWith(EvaluatorException::class, "incompatible dimensions: mass vs length in left=2.0 kg and right=1000.0 m") { reducer.reduce(eQuantitySub) }
    }

    @Test
    fun reduce_sub_whenSameUnitLiteral_shouldSubAsScale() {
        // given
        val a = UnitFixture.kg
        val b = UnitFixture.kg
        val expected = EQuantityScale(ops.pure(0.0), UnitFixture.kg)
        val reducer = DataExpressionReducer(Register.empty(), Register.empty(), ops, sourceOps)

        // when
        val actual = reducer.reduce(EQuantitySub(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_sub_whenSameDimensionLiteral_shouldSubAsScale() {
        // given
        val a = UnitFixture.kg
        val b = UnitFixture.g
        val expected = EQuantityScale(ops.pure(0.999), EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass))
        val reducer = DataExpressionReducer(Register.empty(), Register.empty(), ops, sourceOps)

        // when
        val actual = reducer.reduce(EQuantitySub(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_sub_whenOnlyOneScaleLeft_shouldSubAsScale() {
        // given
        val a = QuantityFixture.twoKilograms
        val b = UnitFixture.kg
        val expected = EQuantityScale(ops.pure(1.0), EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass))
        val reducer = DataExpressionReducer(Register.empty(), Register.empty(), ops, sourceOps)

        // when
        val actual = reducer.reduce(EQuantitySub(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_sub_whenOnlyOneScaleRight_shouldSubAsScale() {
        // given
        val a = UnitFixture.kg
        val b = EQuantityScale(ops.pure(0.5), UnitFixture.kg)
        val expected = EQuantityScale(ops.pure(0.5), EUnitLiteral(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass))
        val reducer = DataExpressionReducer(Register.empty(), Register.empty(), ops, sourceOps)

        // when
        val actual = reducer.reduce(EQuantitySub(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_mul_whenTwoScales_shouldMultiply() {
        // given
        val a = EQuantityScale(ops.pure(2.0), UnitFixture.person)
        val b = EQuantityScale(ops.pure(2.0), UnitFixture.km)
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(EQuantityMul(a, b))

        // then
        val expected = EQuantityScale(ops.pure(4.0), EUnitLiteral(UnitSymbol.of("person").multiply(UnitSymbol.of("km")), 1.0 * 1000.0, Dimension.None.multiply(DimensionFixture.length)))
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_mul_whenTwoUnits_shouldMultiply() {
        // given
        val a = UnitFixture.person
        val b = UnitFixture.km
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(EQuantityMul(a, b))

        // then
        val expected = EQuantityScale(ops.pure(1.0), EUnitLiteral(UnitSymbol.of("person").multiply(UnitSymbol.of("km")), 1.0 * 1000.0, Dimension.None.multiply(DimensionFixture.length)))
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_mul_whenOnlyOneScaleLeft_shouldMultiply() {
        // given
        val a = UnitFixture.kg
        val b = QuantityFixture.twoKilograms
        val expected = EQuantityScale(ops.pure(2.0), EUnitLiteral(UnitSymbol.of("kg").pow(2.0), 1.0, DimensionFixture.mass.multiply(DimensionFixture.mass)))

        // when
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(EQuantityMul(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_mul_whenOnlyOneScaleRight_shouldMultiply() {
        // given
        val a = QuantityFixture.twoKilograms
        val b = UnitFixture.kg
        val expected = EQuantityScale(ops.pure(2.0), EUnitLiteral(UnitSymbol.of("kg").pow(2.0), 1.0, DimensionFixture.mass.multiply(DimensionFixture.mass)))

        // when
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(EQuantityMul(a, b))

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_div_whenTwoScales_shouldDiv() {
        // given
        val a = EQuantityScale(ops.pure(4.0), UnitFixture.km)
        val b = EQuantityScale(ops.pure(2.0), UnitFixture.hour)
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(EQuantityDiv(a, b))

        // then
        val expected = EQuantityScale(ops.pure(2.0), EUnitLiteral(UnitSymbol.of("km").divide(UnitSymbol.of("hour")), 1000.0 / 3600.0, DimensionFixture.length.divide(DimensionFixture.time)))
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_div_whenTwoUnits_shouldDiv() {
        // given
        val a = UnitFixture.km
        val b = UnitFixture.hour
        val reducer = DataExpressionReducer(Register.empty(), Register.empty(), ops, sourceOps)

        // when
        val actual = reducer.reduce(EQuantityDiv(a, b))

        // then
        val expected = EQuantityScale(ops.pure(1.0), EUnitLiteral(UnitSymbol.of("km").divide(UnitSymbol.of("hour")), 1000.0 / 3600.0, DimensionFixture.length.divide(DimensionFixture.time)))
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_div_whenOnlyOneScaleLeft_shouldDiv() {
        // given
        val a = EQuantityScale(ops.pure(4.0), UnitFixture.km)
        val b = UnitFixture.hour
        val expected = EQuantityScale(ops.pure(4.0), EUnitLiteral(UnitSymbol.of("km").divide(UnitSymbol.of("hour")), 1000.0 / 3600.0, DimensionFixture.length.divide(DimensionFixture.time)))

        val reducer = DataExpressionReducer(Register.empty(), Register.empty(), ops, sourceOps)

        // when
        val actual = reducer.reduce(EQuantityDiv(a, b))

        // then
        assertEquals(expected, actual)

    }

    @Test
    fun reduce_pow_shouldExponentiate() {
        // given
        val a = EQuantityScale(ops.pure(4.0), UnitFixture.km)
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(EQuantityPow(a, 2.0))

        // then
        val expected = EQuantityScale(ops.pure(16.0), EUnitLiteral(UnitSymbol.of("km").pow(2.0), 1e6, DimensionFixture.length.pow(2.0)))
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenRef_shouldReadEnvironment() {
        // given
        val a = EDataRef<BasicNumber>("a")
        val reducer = DataExpressionReducer(
            DataRegister(mapOf(DataKey("a") to EQuantityScale(ops.pure(1.0), UnitFixture.kg))),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(a)

        // then
        val expected = QuantityFixture.oneKilogram
        assertEquals(expected, actual)
    }

    /*
        UNITS
     */

    @Test
    fun reduce_whenUnitComposition_shouldReturnNormalForm() {
        // given
        val kg = EUnitLiteral<BasicNumber>(UnitSymbol.of("kg"), 1.0, Dimension.of("mass"))
        val quantityConversion = EQuantityScale(ops.pure(2.2), kg)
        val unitComposition = EUnitAlias("lbs", quantityConversion)
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )
        // when
        val actual = reducer.reduce(unitComposition)
        // then
        val expect = EQuantityScale(ops.pure(1.0), EUnitLiteral(UnitSymbol.of("lbs"), scale = 2.2, Dimension.of("mass")))
        assertEquals(actual, expect)
    }

    @Test
    fun reduce_whenUnitComposition_shouldRespectScaling() {
        // given
        val g = EUnitLiteral<BasicNumber>(UnitSymbol.of("g"), 1.0E-3, Dimension.of("mass"))
        val quantityConversion = EQuantityScale(ops.pure(2200.0), g)
        val unitComposition = EUnitAlias("lbs", quantityConversion)
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )
        // when
        val actual = reducer.reduce(unitComposition)
        // then
        val expect = EQuantityScale(ops.pure(1.0), EUnitLiteral(UnitSymbol.of("lbs"), scale = 2.2, Dimension.of("mass")))
        assertEquals(actual, expect)
    }

    @Test
    fun reduce_whenUnitCompositionComposition_shouldDeepReduce() {
        // given
        val expr = EUnitAlias("foo", EUnitAlias("bar", UnitFixture.kg))
        val reducer = DataExpressionReducer(Register.empty(), Register.empty(), ops, sourceOps)

        // when
        val actual = reducer.reduce(expr)

        // then
        val expected = EQuantityScale(ops.pure(1.0), EUnitLiteral(UnitSymbol.of("foo"), 1.0, DimensionFixture.mass))
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenUnitClosure_shouldReduceWithGivenTable() {
        // given
        val symbolTable = SymbolTable(
            data = DataRegister(mapOf(DataKey("a") to UnitFixture.kg)),
        )
        val unit = EQuantityClosure(symbolTable, EDataRef("a"))
        val reducer = DataExpressionReducer(
            DataRegister(mapOf(DataKey("a") to UnitFixture.l)),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(unit)

        // then
        val expected = QuantityFixture.oneKilogram
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenLiteral_shouldReturnNormalForm() {
        // given
        val kg = UnitFixture.kg
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(kg)

        // then
        val expected = QuantityFixture.oneKilogram
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenUnitOf_withDataSourceExpression() {
        // given
        val dataSource = EDataSource(
            config = DataSourceConfig(
                name = "source",
                location = "source.csv",
            ),
            schema = mapOf(
                "n_items" to EQuantityScale(BasicNumber(0.0), UnitFixture.unit),
                "mass" to EQuantityScale(BasicNumber(0.0), UnitFixture.kg),
            )
        )
        val expr = EUnitOf<BasicNumber>(
            ESumProduct(EDataSourceRef("source"), listOf("n_items", "mass"))
        )
        val reducer = DataExpressionReducer(
            Register.empty(),
            DataSourceRegister.from(mapOf(
                DataSourceKey("source") to dataSource,
            )), ops, sourceOps)


        // when
        val actual = reducer.reduce(expr)

        // then
        val expected = EQuantityScale(
            BasicNumber(1.0),
            EUnitLiteral(
                UnitSymbol.of("u").multiply(UnitSymbol.of("kg")),
                1.0,
                Dimension.of("none").multiply(Dimension.of("mass"))
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenUnitOfUnitLiteral_shouldReturnNormalForm() {
        // given
        val expr = EUnitOf(UnitFixture.l)
        val expected = QuantityFixture.oneLitre
        val reducer = DataExpressionReducer(Register.empty(), Register.empty(), ops, sourceOps)

        // when
        val actual = reducer.reduce(expr)

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenUnitOfRef_shouldReturnAsIs() {
        // given
        val expr = EUnitOf(EDataRef<BasicNumber>("beer"))
        val reducer = DataExpressionReducer(Register.empty(), Register.empty(), ops, sourceOps)

        // when
        val actual = reducer.reduce(expr)

        // then
        assertEquals(actual, expr)
    }

    @Test
    fun reduce_whenUnitOfComplexExpression_shouldReturnNormalForm() {
        // given
        val expr = EUnitOf(EQuantityMul(UnitFixture.kg, QuantityFixture.twoLitres))
        val expected = EQuantityScale(ops.pure(1.0), EUnitLiteral(UnitSymbol.of("kg").multiply(UnitSymbol.of("l")), 1.0e-3, DimensionFixture.mass.multiply(DimensionFixture.volume)))
        val reducer = DataExpressionReducer(Register.empty(), Register.empty(), ops, sourceOps)

        // when
        val actual = reducer.reduce(expr)

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenUnitOfComplexExpression_opposite_shouldReturnNormalForm() {
        // given
        val expr = EUnitOf(EQuantityMul(QuantityFixture.twoLitres, UnitFixture.kg))
        val expected = EQuantityScale(ops.pure(1.0), EUnitLiteral(UnitSymbol.of("l").multiply(UnitSymbol.of("kg")), 1.0e-3, DimensionFixture.mass.multiply(DimensionFixture.volume)))
        val reducer = DataExpressionReducer(Register.empty(), Register.empty(), ops, sourceOps)

        // when
        val actual = reducer.reduce(expr)

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenUnitOfUnitOfRef_shouldMerge() {
        // given
        val expr = EUnitOf(EUnitOf(EDataRef<BasicNumber>("beer")))
        val reducer = DataExpressionReducer(Register.empty(), Register.empty(), ops, sourceOps)

        // when
        val actual = reducer.reduce(expr)

        // then
        val expected = EUnitOf(EDataRef<BasicNumber>("beer"))
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenDiv_shouldDivide() {
        // given
        val kg = UnitFixture.kg
        val l = UnitFixture.l
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(EQuantityDiv(kg, l))

        // then
        val expected = EQuantityScale(ops.pure(1.0), EUnitLiteral(
            UnitSymbol.of("kg").divide(UnitSymbol.of("l")),
            1.0 / 1.0e-3,
            DimensionFixture.mass.divide(DimensionFixture.volume),
        ))
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenMul_shouldMultiply() {
        // given
        val kg = UnitFixture.kg
        val l = UnitFixture.l
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(EQuantityMul(kg, l))

        // then
        val expected = EQuantityScale(ops.pure(1.0), EUnitLiteral(
            UnitSymbol.of("kg").multiply(UnitSymbol.of("l")),
            1.0 * 1.0e-3,
            DimensionFixture.mass.multiply(DimensionFixture.volume),
        ))
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenPow_shouldPow() {
        // given
        val m = UnitFixture.m
        val reducer = DataExpressionReducer(
            Register.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(EQuantityPow(m, 2.0))

        // then
        val expected = EQuantityScale(ops.pure(1.0), EUnitLiteral(
            UnitSymbol.of("m").pow(2.0),
            1.0.pow(2.0),
            DimensionFixture.length.pow(2.0),
        ))
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenRef_shouldReadEnv() {
        // given
        val ref = EDataRef<BasicNumber>("kg")
        val units = DataRegister(mapOf(DataKey("kg") to UnitFixture.kg))
        val reducer = DataExpressionReducer(
            units,
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(ref)

        // then
        val expected = QuantityFixture.oneKilogram
        assertEquals(expected, actual)
    }


    /*
        Strings
     */

    @Test
    fun reduce_whenStringLiteral() {
        // given
        val expression = EStringLiteral<BasicNumber>("FR")
        val reducer = DataExpressionReducer(
            DataRegister.empty(),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        assertEquals(expression, actual)
    }

    @Test
    fun reduce_refToString_whenFound() {
        // given
        val expression = EDataRef<BasicNumber>("geo")
        val reducer = DataExpressionReducer(
            DataRegister(mapOf(
                DataKey("geo") to EDataRef("geo2"),
                DataKey("geo2") to EStringLiteral("FR"),
            )),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EStringLiteral<BasicNumber>("FR")
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_refToString_whenNotFound() {
        // given
        val expression = EDataRef<BasicNumber>("foo")
        val reducer = DataExpressionReducer(
            DataRegister(mapOf(
                DataKey("geo") to EDataRef("geo2"),
                DataKey("geo2") to EStringLiteral("FR"),
            )),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        assertEquals(expression, actual)
    }
}
