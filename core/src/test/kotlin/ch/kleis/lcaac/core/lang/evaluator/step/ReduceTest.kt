package ch.kleis.lcaac.core.lang.evaluator.step

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.fixture.ProductValueFixture
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture
import ch.kleis.lcaac.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaac.core.lang.fixture.TemplateFixture
import ch.kleis.lcaac.core.lang.register.DataSourceKey
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.lang.value.FromProcessRefValue
import ch.kleis.lcaac.core.lang.value.ProcessValue
import ch.kleis.lcaac.core.lang.value.TechnoExchangeValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class ReduceTest {
    private val ops = BasicOperations
    private val sourceOps = mockk<DataSourceOperations<BasicNumber>>()

    @Test
    fun eval_withDataSource() {
        // given
        val template = TemplateFixture.carrotProduction
        val instance = EProcessTemplateApplication(
            template,
            mapOf(
                "q_water" to ESumProduct(EDataSourceRef("source"), listOf("mass"))
            )
        )
        val dataSource = EDataSource(
            config = DataSourceConfig(
                name = "source",
                location = "foo.csv",
            ),
            schema = mapOf(
                "mass" to QuantityFixture.oneLitre
            )
        )
        val dataSourceValue = with(ToValue(ops)) {
            dataSource.toValue()
        }
        every { sourceOps.sumProduct(dataSourceValue, listOf("mass")) } returns QuantityFixture.twoLitres
        val symbolTable = SymbolTable(
            dataSources = DataSourceRegister.from(mapOf(
                DataSourceKey("source") to dataSource,
            ))
        )
        val reduceAndComplete = Reduce(symbolTable, ops, sourceOps)

        // when
        val actual = with(ToValue(BasicOperations)) { reduceAndComplete.apply(instance).toValue() }

        // then
        val expected = ProcessValue(
            name = "carrot_production",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot.withFromProcessRef(
                        FromProcessRefValue(
                            name = "carrot_production",
                            arguments = mapOf("q_water" to QuantityValueFixture.twoLitres),
                        )
                    ),
                )
            ),
            inputs = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.twoLitres,
                    ProductValueFixture.water,
                )
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenInstanceOfProcessTemplate_shouldEvaluateToProcessValue() {
        // given
        val template = TemplateFixture.carrotProduction
        val instance = EProcessTemplateApplication(
            template, mapOf(
            Pair(
                "q_water", EQuantityAdd(
                QuantityFixture.oneLitre,
                QuantityFixture.oneLitre,
            )
            )
        )
        )
        val reduceAndComplete = Reduce(SymbolTable.empty(), ops, sourceOps)

        // when
        val actual = with(ToValue(BasicOperations)) { reduceAndComplete.apply(instance).toValue() }

        // then
        val expected = ProcessValue(
            name = "carrot_production",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot.withFromProcessRef(
                        FromProcessRefValue(
                            name = "carrot_production",
                            arguments = mapOf("q_water" to QuantityValueFixture.twoLitres),
                        )
                    ),
                )
            ),
            inputs = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.twoLitres,
                    ProductValueFixture.water,
                )
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenProcessTemplate_shouldAutomaticallyInstantiateWithoutArguments() {
        // given
        val template = EProcessTemplateApplication(TemplateFixture.carrotProduction, emptyMap())
        val reduceAndComplete = Reduce(SymbolTable.empty(), ops, sourceOps)

        // when
        val actual = with(ToValue(BasicOperations)) { reduceAndComplete.apply(template).toValue() }

        // then
        val expected = ProcessValue(
            name = "carrot_production",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot.withFromProcessRef(
                        FromProcessRefValue(
                            name = "carrot_production",
                            arguments = mapOf("q_water" to QuantityValueFixture.oneLitre),
                        )
                    )
                )
            ),
            inputs = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneLitre,
                    ProductValueFixture.water
                )
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun eval_whenContainsUnboundedReference_shouldThrow() {
        // given
        val template = EProcessTemplateApplication(TemplateFixture.withUnboundedRef, emptyMap())
        val reduceAndComplete = Reduce(SymbolTable.empty(), ops, sourceOps)

        // when/then
        assertFailsWith(
            EvaluatorException::class,
            "unbounded references: [q_carrot, q_water]"
        ) { reduceAndComplete.apply(template) }
    }
}
