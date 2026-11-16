package ch.kleis.lcaac.core.lang.resolver

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
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
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows

class CachedProcessResolverTest {
    private val ops = BasicOperations
    private val sourceOps = mockk<DataSourceOperations<BasicNumber>>()

    @Nested
    inner class Resolve {
        @Test
        fun `when process without dependencies should return same process`() {
            // Given
            val cache = InMemoryKache<Pair<EProcessTemplate<BasicNumber>, EProductSpec<BasicNumber>>, EProcess<BasicNumber>>(
                maxSize = 1024
            ) {
                strategy = KacheStrategy.LRU
            }
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
            val sut = CachedProcessResolver(symbolTable, ops, sourceOps, cache)

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
            val cache = InMemoryKache<Pair<EProcessTemplate<BasicNumber>, EProductSpec<BasicNumber>>, EProcess<BasicNumber>>(
                maxSize = 1024
            ) {
                strategy = KacheStrategy.LRU
            }
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
            val sut = CachedProcessResolver(symbolTable, ops, sourceOps, cache)

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
            val cache = InMemoryKache<Pair<EProcessTemplate<BasicNumber>, EProductSpec<BasicNumber>>, EProcess<BasicNumber>>(
                maxSize = 1024
            ) {
                strategy = KacheStrategy.LRU
            }
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
            val sut = CachedProcessResolver(symbolTable, ops, sourceOps, cache)

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

        @Test
        fun `when resolve cached process twice should use cache`() {
            val cache = spyk(InMemoryKache<Pair<EProcessTemplate<BasicNumber>, EProductSpec<BasicNumber>>, EProcess<BasicNumber>>(
                maxSize = 1024
            ) {
                strategy = KacheStrategy.LRU
            })
            val template = EProcessTemplate(
                body = EProcess(
                    name = "carrot_production",
                    products = listOf(
                        ETechnoExchange(twoKilograms, carrot),
                    ),
                    inputs = listOf(),
                    biosphere = listOf(),
                    impacts = listOf()
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

            val sut = CachedProcessResolver(symbolTable, ops, sourceOps, cache)

            // When
            sut.resolve(template, spec)
            sut.resolve(template, spec)

            // Then
            // Evaluated once: the second resolve reads the cached process
            verify(exactly = 2) {
                runBlocking {
                    cache.get(template to spec)
                }
            }
            verify(exactly = 1) {
                runBlocking {
                    cache.put(template to spec, any<EProcess<BasicNumber>>())
                }
            }
        }
    }

    @Test
    fun resolve_whenTheEvaluationFailsBeforeItsResultIsAwaited_shouldThrowItsError() {
        // Given: a cache that runs the evaluation in the calling thread, so that it fails and is removed
        // from the cache before its result is awaited
        val cache = InMemoryKache<Pair<EProcessTemplate<BasicNumber>, EProductSpec<BasicNumber>>, EProcess<BasicNumber>>(
            maxSize = 1024
        ) {
            creationScope = CoroutineScope(Dispatchers.Unconfined)
        }
        val template = EProcessTemplate(
            body = EProcess(
                name = "steel_mill",
                products = listOf(ETechnoExchange(oneKilogram, EProductSpec("steel"))),
                inputs = listOf(
                    ETechnoBlockEntry(ETechnoExchange(oneKilogram, EProductSpec("iron", fromProcess = FromProcess("other", MatchLabels(emptyMap()))))),
                ),
            ),
            annotations = setOf(ProcessAnnotation.CACHED),
        )
        val other = EProcessTemplate(
            body = EProcess(name = "other", products = listOf(ETechnoExchange(oneKilogram, EProductSpec("copper")))),
        )
        val symbolTable = SymbolTable(
            processTemplates = ProcessTemplateRegister.from(
                mapOf(ProcessKey("steel_mill") to template, ProcessKey("other") to other)
            )
        )
        val sut = CachedProcessResolver(symbolTable, ops, sourceOps, cache)

        // When
        val error = assertThrows<EvaluatorException> { sut.resolve(template, EProductSpec("steel")) }

        // Then: the error of the evaluation, with its frame
        assertEquals("no process 'other' providing 'iron' found", error.message)
        assertEquals(1, error.frames.size)
    }
}
