package ch.kleis.lcaac.core.lang.evaluator.reducer

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.fixture.*
import ch.kleis.lcaac.core.lang.register.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class LcaExpressionReducerTest {
    private val ops = BasicOperations
    private val sourceOps = mockk<DataSourceOperations<BasicNumber>>()
    private val emptyDataSource = EDataSource<BasicNumber>(
        name = "source",
        "location.csv",
        emptyMap(),
        emptyMap()
    )

    /*
        Block
     */

    @Test
    fun reduce_whenBlockForEach_ImpactBlock_withLocalVariables() {
        // given
        val block = EImpactBlockForEach(
            "row",
            EDataSourceRef("source"),
            mapOf("x" to ERecordEntry(EDataRef("row"), "mass")),
            listOf(
                EImpactBlockEntry(
                    EImpact(
                        EDataRef("x"),
                        IndicatorFixture.climateChange,
                    )
                ),
            )
        )
        val expression = EProcess(
            name = "foo",
            impacts = listOf(block),
        )
        val sourceOps = mockk<DataSourceOperations<BasicNumber>>()
        every { sourceOps.readAll(any()) } returns sequenceOf(
            ERecord(mapOf(
                "mass" to QuantityFixture.oneKilogram,
            )),
            ERecord(mapOf(
                "mass" to QuantityFixture.twoKilograms,
            )),
        )
        val reducer = LcaExpressionReducer(
            DataRegister.empty(),
            DataSourceRegister.from(mapOf(
                DataSourceKey("source") to emptyDataSource,
            )),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProcess(
            name = "foo",
            impacts = listOf(
                EImpactBlockEntry(
                    EImpact(
                        QuantityFixture.oneKilogram,
                        IndicatorFixture.climateChange,
                    )
                ),
                EImpactBlockEntry(
                    EImpact(
                        QuantityFixture.twoKilograms,
                        IndicatorFixture.climateChange,
                    )
                ),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenBlockForEach_BioBlock_withLocalVariables() {
        // given
        val block = EBioBlockForEach(
            "row",
            EDataSourceRef("source"),
            mapOf("x" to ERecordEntry(EDataRef("row"), "mass")),
            listOf(
                EBioBlockEntry(
                    EBioExchange(
                        EDataRef("x"),
                        SubstanceFixture.propanol,
                    )
                ),
            )
        )
        val expression = EProcess(
            name = "foo",
            biosphere = listOf(block),
        )
        val sourceOps = mockk<DataSourceOperations<BasicNumber>>()
        every { sourceOps.readAll(any()) } returns sequenceOf(
            ERecord(mapOf(
                "mass" to QuantityFixture.oneKilogram,
            )),
            ERecord(mapOf(
                "mass" to QuantityFixture.twoKilograms,
            )),
        )
        val reducer = LcaExpressionReducer(
            DataRegister.empty(),
            DataSourceRegister.from(mapOf(
                DataSourceKey("source") to emptyDataSource,
            )),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProcess(
            name = "foo",
            biosphere = listOf(
                EBioBlockEntry(
                    EBioExchange(
                        QuantityFixture.oneKilogram,
                        SubstanceFixture.propanol,
                    )
                ),
                EBioBlockEntry(
                    EBioExchange(
                        QuantityFixture.twoKilograms,
                        SubstanceFixture.propanol,
                    )
                ),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenBlockForEach_TechnoBlock_withLocalVariables() {
        // given
        val block = ETechnoBlockForEach(
            "row",
            EDataSourceRef("source"),
            mapOf("x" to ERecordEntry(EDataRef("row"), "mass")),
            listOf(
                ETechnoBlockEntry(
                    ETechnoExchange(
                        EDataRef("x"),
                        ProductFixture.carrot,
                    )
                ),
            )
        )
        val expression = EProcess(
            name = "foo",
            inputs = listOf(block),
        )
        val sourceOps = mockk<DataSourceOperations<BasicNumber>>()
        every { sourceOps.readAll(any()) } returns sequenceOf(
            ERecord(mapOf(
                "mass" to QuantityFixture.oneKilogram,
            )),
            ERecord(mapOf(
                "mass" to QuantityFixture.twoKilograms,
            )),
        )
        val reducer = LcaExpressionReducer(
            DataRegister.empty(),
            DataSourceRegister.from(mapOf(
                DataSourceKey("source") to emptyDataSource,
            )),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProcess(
            name = "foo",
            inputs = listOf(
                ETechnoBlockEntry(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        ProductFixture.carrot,
                    )
                ),
                ETechnoBlockEntry(
                    ETechnoExchange(
                        QuantityFixture.twoKilograms,
                        ProductFixture.carrot,
                    )
                ),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenBlockForEach_withRecordEntryOverride_shouldThrow() {
        // given
        val block = ETechnoBlockForEach(
            "row",
            EDataSourceRef("source"),
            emptyMap(),
            listOf(
                ETechnoBlockEntry(
                    ETechnoExchange(
                        ERecordEntry(EDataRef("row"), "mass"),
                        ProductFixture.carrot,
                    )
                ),
            )
        )
        val expression = EProcess(
            name = "foo",
            inputs = listOf(block),
        )
        val sourceOps = mockk<DataSourceOperations<BasicNumber>>()
        every { sourceOps.readAll(any()) } returns sequenceOf(
            ERecord(mapOf(
                "mass" to QuantityFixture.oneKilogram,
            )),
            ERecord(mapOf(
                "mass" to QuantityFixture.twoKilograms,
            )),
        )
        val reducer = LcaExpressionReducer(
            DataRegister.from(mapOf(
                DataKey("row") to QuantityFixture.oneLitre,
            )),
            DataSourceRegister.from(mapOf(
                DataSourceKey("source") to emptyDataSource,
            )),
            ops,
            sourceOps,
        )

        // when/then
        val e = assertThrows<RegisterException> {  reducer.reduce(expression) }
        assertEquals("[row] is already bound", e.message)
    }

    @Test
    fun reduce_whenBlockForEach_withRecordEntry() {
        // given
        val block = ETechnoBlockForEach(
            "row",
            EDataSourceRef("source"),
            emptyMap(),
            listOf(
                ETechnoBlockEntry(
                    ETechnoExchange(
                        ERecordEntry(EDataRef("row"), "mass"),
                        ProductFixture.carrot,
                    )
                ),
            )
        )
        val expression = EProcess(
            name = "foo",
            inputs = listOf(block),
        )
        val sourceOps = mockk<DataSourceOperations<BasicNumber>>()
        every { sourceOps.readAll(any()) } returns sequenceOf(
            ERecord(mapOf(
                "mass" to QuantityFixture.oneKilogram,
            )),
            ERecord(mapOf(
                "mass" to QuantityFixture.twoKilograms,
            )),
        )
        val reducer = LcaExpressionReducer(
            DataRegister.empty(),
            DataSourceRegister.from(mapOf(
                DataSourceKey("source") to emptyDataSource,
            )),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProcess(
            name = "foo",
            inputs = listOf(
                ETechnoBlockEntry(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        ProductFixture.carrot,
                    )
                ),
                ETechnoBlockEntry(
                    ETechnoExchange(
                        QuantityFixture.twoKilograms,
                        ProductFixture.carrot,
                    )
                ),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenBlockForEach_shouldFlatten() {
        // given
        val block = ETechnoBlockForEach(
            "row",
            EDataSourceRef("source"),
            emptyMap(),
            listOf(
                ETechnoBlockEntry(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        ProductFixture.carrot,
                    )
                ),
                EBlockForEach(
                    "row2",
                    EDataSourceRef("source"),
                    emptyMap(),
                    listOf(
                        ETechnoBlockEntry(
                            ETechnoExchange(
                                QuantityFixture.oneLitre,
                                ProductFixture.water,
                            )
                        ),
                    )
                )
            )
        )
        val expression = EProcess(
            name = "foo",
            inputs = listOf(block),
        )
        val sourceOps = mockk<DataSourceOperations<BasicNumber>>()
        every { sourceOps.readAll(any()) } returns sequenceOf(
            ERecord(emptyMap()),
            ERecord(emptyMap()),
        )
        val reducer = LcaExpressionReducer(
            DataRegister.empty(),
            DataSourceRegister.from(mapOf(
                DataSourceKey("source") to emptyDataSource,
            )),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProcess(
            name = "foo",
            inputs = listOf(
                ETechnoBlockEntry(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        ProductFixture.carrot,
                    )
                ),
                ETechnoBlockEntry(
                    ETechnoExchange(
                        QuantityFixture.oneLitre,
                        ProductFixture.water,
                    )
                ),
                ETechnoBlockEntry(
                    ETechnoExchange(
                        QuantityFixture.oneLitre,
                        ProductFixture.water,
                    )
                ),
                ETechnoBlockEntry(
                    ETechnoExchange(
                        QuantityFixture.oneKilogram,
                        ProductFixture.carrot,
                    )
                ),
                ETechnoBlockEntry(
                    ETechnoExchange(
                        QuantityFixture.oneLitre,
                        ProductFixture.water,
                    )
                ),
                ETechnoBlockEntry(
                    ETechnoExchange(
                        QuantityFixture.oneLitre,
                        ProductFixture.water,
                    )
                ),
            ),
        )
        assertEquals(expected, actual)
    }

    /*
        Techno Exchange
     */

    @Test
    fun reduce_whenTechnoExchange_shouldReduceLabelSelectors() {
        // given
        val expression = ETechnoExchange(
            QuantityFixture.oneKilogram,
            EProductSpec(
                "a",
                UnitFixture.kg,
                FromProcess(
                    name = "p",
                    matchLabels = MatchLabels(mapOf("geo" to EDataRef("geo"))),
                )
            )
        )
        val reducer = LcaExpressionReducer(
            DataRegister(
                mapOf(DataKey("geo") to EStringLiteral("FR"))
            ),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = ETechnoExchange(
            QuantityFixture.oneKilogram,
            EProductSpec(
                "a",
                QuantityFixture.oneKilogram,
                FromProcess(
                    name = "p",
                    matchLabels = MatchLabels(mapOf("geo" to EStringLiteral("FR"))),
                )
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenProcess_shouldReduceExchanges() {
        // given
        val expression = EProcess(
            name = "p",
            products = listOf(
                ETechnoExchange(EDataRef("q_carrot"), ProductFixture.carrot),
            ),
            inputs = listOf(
                ETechnoBlockEntry(ETechnoExchange(EDataRef("q_water"), ProductFixture.water))
            ),
            biosphere = listOf(
                EBioBlockEntry(EBioExchange(EDataRef("q_propanol"), SubstanceFixture.propanol)),
            ),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = DataRegister(
                mapOf(
                    "q_carrot" to QuantityFixture.oneKilogram,
                    "q_water" to QuantityFixture.oneLitre,
                    "q_propanol" to QuantityFixture.oneKilogram,
                ).mapKeys { DataKey(it.key) }
            ),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProcess(
            name = "p",
            products = listOf(
                ETechnoExchange(
                    QuantityFixture.oneKilogram,
                    ProductFixture.carrot
                ),
            ),
            inputs = listOf(
                ETechnoBlockEntry(
                    ETechnoExchange(
                        QuantityFixture.oneLitre,
                        ProductFixture.water
                    )
                )
            ),
            biosphere = listOf(
                EBioBlockEntry(
                    EBioExchange(
                        QuantityFixture.oneKilogram,
                        SubstanceFixture.propanol
                    )
                ),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenImpact_shouldReduceQuantityAndIndicator() {
        // given
        val expression = EImpact<BasicNumber>(
            EDataRef("q"),
            EIndicatorSpec("cc")
        )
        val reducer = LcaExpressionReducer(
            dataRegister = DataRegister(
                mapOf(
                    DataKey("q") to QuantityFixture.oneKilogram,
                )
            ),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EImpact(
            QuantityFixture.oneKilogram,
            EIndicatorSpec("cc"),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenTechnoExchange_shouldReduceQuantity() {
        // given
        val expression = ETechnoExchange<BasicNumber>(
            EDataRef("q"),
            EProductSpec("carrot"),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = DataRegister(
                mapOf(
                    DataKey("q") to QuantityFixture.oneKilogram,
                )
            ),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = ETechnoExchange(
            QuantityFixture.oneKilogram,
            EProductSpec("carrot"),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenBioExchange_shouldReduceQuantityAndSubstance() {
        // given
        val expression = EBioExchange<BasicNumber>(
            EDataRef("q"),
            ESubstanceSpec("propanol"),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = DataRegister(
                mapOf(
                    DataKey("q") to QuantityFixture.oneKilogram,
                )
            ),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EBioExchange(
            QuantityFixture.oneKilogram,
            ESubstanceSpec("propanol"),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenIndicator_shouldReduceUnit() {
        // given
        val expression = EIndicatorSpec<BasicNumber>(
            "cc",
            EDataRef("kg"),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = DataRegister(
                mapOf(
                    DataKey("kg") to UnitFixture.kg
                )
            ),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EIndicatorSpec(
            "cc",
            QuantityFixture.oneKilogram,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenSubstance_shouldReduceUnit() {
        // given
        val expression = ESubstanceSpec<BasicNumber>(
            "propanol",
            "propanol",
            type = SubstanceType.RESOURCE,
            "air",
            null,
            EDataRef("kg"),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = DataRegister(
                mapOf(
                    DataKey("kg") to UnitFixture.kg
                )
            ),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = ESubstanceSpec(
            "propanol",
            "propanol",
            type = SubstanceType.RESOURCE,
            "air",
            null,
            QuantityFixture.oneKilogram,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenProduct_shouldReduceUnit() {
        // given
        val expression = EProductSpec<BasicNumber>(
            "carrot",
            EDataRef("kg"),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = DataRegister(
                mapOf(
                    DataKey("kg") to UnitFixture.kg
                )
            ),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProductSpec(
            "carrot",
            QuantityFixture.oneKilogram,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_withoutFromProcessRef_shouldReduceProduct() {
        // given
        val expression = EProductSpec<BasicNumber>(
            "carrot",
            EDataRef("kg"),
        )
        val reducer = LcaExpressionReducer(
            dataRegister = DataRegister(
                mapOf(
                    DataKey("kg") to UnitFixture.kg
                )
            ),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProductSpec(
            "carrot",
            QuantityFixture.oneKilogram,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_withConstraintFromProcess_shouldReduceProductAndArguments() {
        // given
        val expression = EProductSpec(
            "carrot",
            UnitFixture.kg,
            FromProcess(
                "p",
                MatchLabels(emptyMap()),
                mapOf(
                    "x" to EDataRef("q")
                )
            )
        )
        val reducer = LcaExpressionReducer(
            dataRegister = DataRegister(
                mapOf(
                    "q" to EQuantityScale(ops.pure(3.0), EDataRef("kg")),
                    "kg" to UnitFixture.kg
                ).mapKeys { DataKey(it.key) }
            ),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = EProductSpec(
            "carrot",
            QuantityFixture.oneKilogram,
            FromProcess(
                "p",
                MatchLabels(emptyMap()),
                mapOf(
                    "x" to EQuantityScale(ops.pure(3.0), UnitFixture.kg)
                )
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun reduce_whenSubstanceCharacterization() {
        // given
        val expression = ESubstanceCharacterization(
            referenceExchange = EBioExchange(
                EDataRef("q_propanol"),
                SubstanceFixture.propanol
            ),
            impacts = listOf(
                EImpactBlockEntry(
                    EImpact(
                        EDataRef("q_cc"),
                        IndicatorFixture.climateChange
                    )
                ),
            )
        )
        val reducer = LcaExpressionReducer(
            dataRegister = DataRegister(
                mapOf(
                    "q_propanol" to QuantityFixture.oneKilogram,
                    "q_cc" to QuantityFixture.oneKilogram,
                ).mapKeys { DataKey(it.key) }
            ),
            Register.empty(),
            ops,
            sourceOps,
        )

        // when
        val actual = reducer.reduce(expression)

        // then
        val expected = ESubstanceCharacterization(
            referenceExchange = EBioExchange(
                QuantityFixture.oneKilogram,
                SubstanceFixture.propanol
            ),
            impacts = listOf(
                EImpactBlockEntry(
                    EImpact(
                        QuantityFixture.oneKilogram,
                        IndicatorFixture.climateChange
                    )
                ),
            )
        )
        assertEquals(expected, actual)
    }
}
