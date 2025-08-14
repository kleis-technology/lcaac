package ch.kleis.lcaac.core.lang.resolver

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.fixture.IndicatorFixture.Companion.climateChange
import ch.kleis.lcaac.core.lang.fixture.ProductFixture.Companion.carrot
import ch.kleis.lcaac.core.lang.fixture.ProductFixture.Companion.salad
import ch.kleis.lcaac.core.lang.fixture.ProductFixture.Companion.water
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture.fiftyPercent
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture.oneKilogram
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture.oneLitre
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture.oneUnit
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture.threeKilograms
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture.twoKilograms
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture.twoLitres
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture.twoUnits
import ch.kleis.lcaac.core.lang.fixture.SubstanceFixture.Companion.propanol
import ch.kleis.lcaac.core.lang.fixture.UnitFixture
import ch.kleis.lcaac.core.lang.register.ProcessKey
import ch.kleis.lcaac.core.lang.register.ProcessTemplateRegister
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class CachedProcessResolverTest {
    private val ops = BasicOperations
    private val sourceOps = mockk<DataSourceOperations<BasicNumber>>()

    @Test
    fun `when process without dependencies should return same process`() {
        // Given
        val template = EProcessTemplate(
            body = EProcess(
                name = "carrot_production",
                products = listOf(
                    ETechnoExchange(twoKilograms, carrot, fiftyPercent),
                    ETechnoExchange(threeKilograms, salad, fiftyPercent),
                ),
                inputs = listOf(
                    ETechnoBlockEntry(ETechnoExchange(twoLitres, water)),
                ),
                biosphere = listOf(
                    EBioBlockEntry(EBioExchange(oneKilogram, propanol))
                ),
                impacts = listOf(
                    EImpactBlockEntry(EImpact(oneUnit, climateChange)),
                )
            )
        )
        val spec = carrot
        val symbolTable = SymbolTable(
            processTemplates = ProcessTemplateRegister.from(
                mapOf(
                    ProcessKey("carrot_production") to template,
                )
            )
        )
        val sut = CachedProcessResolver(symbolTable, ops, sourceOps)

        // When
        val actual = sut.resolve(template, spec)

        // Then
        assertEquals(2, actual.products.size)
        assertEquals(twoKilograms, actual.products[0].quantity)
        assertEquals(carrot.name, actual.products[0].product.name)
        assertEquals(threeKilograms, actual.products[1].quantity)
        assertEquals(salad.name, actual.products[1].product.name)

        assertEquals(1, actual.inputs.size)
        assertEquals(twoLitres, (actual.inputs[0] as ETechnoBlockEntry<BasicNumber>).entry.quantity)
        assertEquals(water.name, (actual.inputs[0] as ETechnoBlockEntry<BasicNumber>).entry.product.name)

        assertEquals(1, actual.biosphere.size)
        assertEquals(oneKilogram, (actual.biosphere[0] as EBioBlockEntry<BasicNumber>).entry.quantity)
        assertEquals(propanol.name, (actual.biosphere[0] as EBioBlockEntry<BasicNumber>).entry.substance.name)

        assertEquals(1, actual.impacts.size)
        assertEquals(oneUnit, (actual.impacts[0] as EImpactBlockEntry<BasicNumber>).entry.quantity)
        assertEquals(climateChange.name, (actual.impacts[0] as EImpactBlockEntry<BasicNumber>).entry.indicator.name)
    }

    @Test
    fun `when process with dependencies should return process without dependencies`() {
        // Given
        val template = EProcessTemplate(
            body = EProcess(
                name = "carrot_production",
                products = listOf(
                    ETechnoExchange(twoKilograms, carrot, fiftyPercent),
                    ETechnoExchange(threeKilograms, salad, fiftyPercent),
                ),
                inputs = listOf(
                    ETechnoBlockEntry(ETechnoExchange(twoLitres, water)),
                ),
                biosphere = listOf(
                    EBioBlockEntry(EBioExchange(oneKilogram, propanol))
                ),
                impacts = listOf(
                    EImpactBlockEntry(EImpact(oneUnit, climateChange)),
                )
            )
        )

        val waterTemplate = EProcessTemplate(
            body = EProcess(
                name = "water_production",
                products = listOf(
                    ETechnoExchange(oneLitre, water)
                ),
                inputs = listOf(
                    ETechnoBlockEntry(
                        ETechnoExchange(
                            twoKilograms, EProductSpec(
                                "detergent", oneKilogram
                            )
                        )
                    ),
                ),
                biosphere = listOf(
                    EBioBlockEntry(EBioExchange(twoKilograms, propanol))
                ),
                impacts = listOf(
                    EImpactBlockEntry(EImpact(twoUnits, climateChange)),
                )
            )
        )

        val spec = carrot
        val symbolTable = SymbolTable(
            processTemplates = ProcessTemplateRegister.from(
                mapOf(
                    ProcessKey("carrot_production") to template,
                    ProcessKey("water_production") to waterTemplate
                )
            )
        )
        val sut = CachedProcessResolver(symbolTable, ops, sourceOps)

        // When
        val actual = sut.resolve(template, spec)

        // Then
        assertEquals(2, actual.products.size)
        assertEquals(twoKilograms, actual.products[0].quantity)
        assertEquals(carrot.name, actual.products[0].product.name)
        assertEquals(threeKilograms, actual.products[1].quantity)
        assertEquals(salad.name, actual.products[1].product.name)

        assertEquals(1, actual.inputs.size)
        assertEquals(
            EQuantityScale(
                ops.pure(4.0),
                UnitFixture.kg
            ), (actual.inputs[0] as ETechnoBlockEntry<BasicNumber>).entry.quantity
        )
        assertEquals("detergent", (actual.inputs[0] as ETechnoBlockEntry<BasicNumber>).entry.product.name)

        assertEquals(1, actual.biosphere.size)
        // 2 * 2 propanol from water biosphere + 1 propanol from carrot biosphere
        assertEquals(
            EQuantityScale(
                ops.pure(5.0),
                UnitFixture.kg
            ), (actual.biosphere[0] as EBioBlockEntry<BasicNumber>).entry.quantity
        )
        assertEquals(propanol.name, (actual.biosphere[0] as EBioBlockEntry<BasicNumber>).entry.substance.name)

        assertEquals(1, actual.impacts.size)
        assertEquals(
            // 2 * 2 climate change from water impact + 1 climate change from carrot impact
            EQuantityScale(
                ops.pure(5.0),
                UnitFixture.unit
            ), (actual.impacts[0] as EImpactBlockEntry<BasicNumber>).entry.quantity
        )
        assertEquals(climateChange.name, (actual.impacts[0] as EImpactBlockEntry<BasicNumber>).entry.indicator.name)
    }

    @Test
    fun `when process with deep dependencies should return process without dependencies`() {
        // Given
        val template = EProcessTemplate(
            body = EProcess(
                name = "carrot_production",
                products = listOf(
                    ETechnoExchange(twoKilograms, carrot, fiftyPercent),
                    ETechnoExchange(threeKilograms, salad, fiftyPercent),
                ),
                inputs = listOf(
                    ETechnoBlockEntry(ETechnoExchange(twoLitres, water)),
                )
            )
        )
        val detergent = EProductSpec("detergent", oneKilogram)
        val waterTemplate = EProcessTemplate(
            body = EProcess(
                name = "water_production",
                products = listOf(
                    ETechnoExchange(oneLitre, water)
                ),
                inputs = listOf(
                    ETechnoBlockEntry(ETechnoExchange(twoKilograms, detergent)),
                )
            )
        )

        val detergentTemplate = EProcessTemplate(
            body = EProcess(
                name = "detergent_production",
                products = listOf(
                    ETechnoExchange(oneKilogram, detergent),
                ),
                impacts = listOf(
                    EImpactBlockEntry(EImpact(twoUnits, climateChange)),
                )
            )
        )

        val spec = carrot
        val symbolTable = SymbolTable(
            processTemplates = ProcessTemplateRegister.from(
                mapOf(
                    ProcessKey("carrot_production") to template,
                    ProcessKey("water_production") to waterTemplate,
                    ProcessKey("detergent_production") to detergentTemplate
                )
            )
        )
        val sut = CachedProcessResolver(symbolTable, ops, sourceOps)

        // When
        val actual = sut.resolve(template, spec)

        // Then
        assertEquals(2, actual.products.size)
        assertEquals(twoKilograms, actual.products[0].quantity)
        assertEquals(carrot.name, actual.products[0].product.name)
        assertEquals(threeKilograms, actual.products[1].quantity)
        assertEquals(salad.name, actual.products[1].product.name)

        assertEquals(0, actual.inputs.size)
        assertEquals(0, actual.biosphere.size)

        assertEquals(1, actual.impacts.size)
        assertEquals(
            // 2 * 2 * 2 climate change from detergent impact
            EQuantityScale(
                ops.pure(8.0),
                UnitFixture.unit
            ), (actual.impacts[0] as EImpactBlockEntry<BasicNumber>).entry.quantity
        )
        assertEquals(climateChange.name, (actual.impacts[0] as EImpactBlockEntry<BasicNumber>).entry.indicator.name)
    }
}