package ch.kleis.lcaac.core.allocation

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.fixture.FullyQualifiedSubstanceValueFixture
import ch.kleis.lcaac.core.lang.fixture.ProductValueFixture
import ch.kleis.lcaac.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaac.core.lang.fixture.SubstanceCharacterizationValueFixture.Companion.propanolCharacterization
import ch.kleis.lcaac.core.lang.fixture.SystemValueFixture
import ch.kleis.lcaac.core.lang.fixture.UnitValueFixture
import ch.kleis.lcaac.core.lang.value.BioExchangeValue
import ch.kleis.lcaac.core.lang.value.ProcessValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.SystemValue
import ch.kleis.lcaac.core.lang.value.TechnoExchangeValue
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.junit.jupiter.api.Test

class AllocationTest {
    private val ops = BasicOperations
    
    @Test
    fun apply_when_no_allocation_should_change_nothing() {
        // Given
        val system = SystemValueFixture.carrotSystem()
        // When
        val actual = Allocation(ops).apply(SystemValueFixture.carrotSystem())
        // Then
        assertEquals(system, actual)
    }

    @Test
    fun apply_when_coProducts_should_duplicate_process() {
        // Given
        val system = SystemValue(
            processes = mutableSetOf(
                ProcessValue(
                    name = "carrot",
                    products = listOf(
                        TechnoExchangeValue(
                            QuantityValueFixture.oneKilogram,
                            ProductValueFixture.carrot,
                            QuantityValueFixture.fiftyPercent
                        ),
                        TechnoExchangeValue(
                            QuantityValueFixture.oneKilogram,
                            ProductValueFixture.salad,
                            QuantityValueFixture.fiftyPercent
                        )
                    ),
                )
            ),
        )

        // When
        val actual = Allocation(ops).apply(system).processes.size
        // Then
        assertEquals(2, actual)
    }

    @Test
    fun apply_when_coProducts_should_keep_only_one_product_per_process() {
        // Given
        val system = SystemValue(
            processes = mutableSetOf(
                ProcessValue(
                    name = "carrot",
                    products = listOf(
                        TechnoExchangeValue(
                            QuantityValueFixture.oneKilogram,
                            ProductValueFixture.carrot,
                            QuantityValueFixture.fiftyPercent
                        ),
                        TechnoExchangeValue(
                            QuantityValueFixture.oneKilogram,
                            ProductValueFixture.salad,
                            QuantityValueFixture.fiftyPercent
                        )
                    ),
                )
            ),
        )

        // When
        val actual = Allocation(ops).apply(system).processes.toList()[0].products.size
        // Then
        assertEquals(1, actual)
    }

    @Test
    fun apply_when_allocation_should_divide_inputs_quantities() {
        // Given
        val system = SystemValue(
            processes = mutableSetOf(
                ProcessValue(
                    name = "carrot",
                    products = listOf(
                        TechnoExchangeValue(
                            QuantityValueFixture.oneKilogram,
                            ProductValueFixture.carrot,
                            QuantityValueFixture.fiftyPercent
                        ),
                        TechnoExchangeValue(
                            QuantityValueFixture.oneKilogram,
                            ProductValueFixture.salad,
                            QuantityValueFixture.fiftyPercent
                        )
                    ),
                    inputs = listOf(
                        TechnoExchangeValue(
                            QuantityValueFixture.twoLitres,
                            ProductValueFixture.water
                        )
                    ),
                )
            ),
        )

        // When
        val actual = Allocation(ops).apply(system).processes.toList()[0].inputs[0].quantity
        // Then
        val expected = QuantityValueFixture.oneLitre
        assertEquals(expected, actual)
    }

    @Test
    fun apply_when_allocation_should_divide_biosphere_quantities() {
        // given
        val system = SystemValue(
            processes = mutableSetOf(
                ProcessValue(
                    name = "carrot",
                    products = listOf(
                        TechnoExchangeValue(
                            QuantityValueFixture.oneKilogram,
                            ProductValueFixture.carrot,
                            QuantityValueFixture.fiftyPercent
                        ),
                        TechnoExchangeValue(
                            QuantityValueFixture.oneKilogram,
                            ProductValueFixture.salad,
                            QuantityValueFixture.fiftyPercent
                        )
                    ),
                    biosphere = listOf(
                        BioExchangeValue(
                            QuantityValueFixture.twoKilograms,
                            FullyQualifiedSubstanceValueFixture.propanol
                        )
                    ),
                )
            ),
        )

        // when
        val actual = Allocation(ops).apply(system).processes.toList()[0].biosphere[0].quantity
        // then
        val expected = QuantityValueFixture.oneKilogram
        assertEquals(expected, actual)
    }

    @Test
    fun applyAllocation_whenTwoProduct_shouldWeightAllocations() {
        // given

        val system = SystemValue(
            processes = mutableSetOf(
                ProcessValue(
                    name = "carrot",
                    products = listOf(
                        TechnoExchangeValue(
                            QuantityValueFixture.oneKilogram,
                            ProductValueFixture.carrot,
                            QuantityValueFixture.twentyPercent
                        ),
                        TechnoExchangeValue(
                            QuantityValueFixture.oneKilogram,
                            ProductValueFixture.salad,
                            QuantityValueFixture.eightyPercent
                        )
                    ),
                    inputs = listOf(
                        TechnoExchangeValue(
                            QuantityValueFixture.twoLitres,
                            ProductValueFixture.water
                        )
                    ),
                ),
            ),
        )

        // when
        val actual = Allocation(ops).apply(system).processes.first().inputs.first().quantity.amount

        // then
        val delta = 1E-9
        with (BasicOperations) {
            val totalAllocation = QuantityValueFixture.twentyPercent.amount + QuantityValueFixture.eightyPercent.amount
            val expected =
                QuantityValueFixture.twoLitres.amount * QuantityValueFixture.twentyPercent.amount / totalAllocation
            assertEquals(expected.value, actual.value, delta)
        }
    }

    @Test
    fun apply_shouldKeepAllocation() {
        // given
        val system = SystemValue(
            mutableSetOf(ProcessValue(name = "")),
            mutableSetOf(propanolCharacterization)
        )

        // when
        val actual = Allocation(ops).apply(system).substanceCharacterizations

        // then
        assertEquals(setOf(propanolCharacterization), actual)
    }

    private fun product(name: String, allocation: QuantityValue<BasicNumber>?) = TechnoExchangeValue(
        QuantityValueFixture.oneKilogram,
        ProductValueFixture.carrot.copy(name = name),
        allocation,
    )

    private fun process(vararg products: TechnoExchangeValue<BasicNumber>) =
        ProcessValue(name = "p", products = products.toList())

    private fun quantity(amount: Double, unit: UnitValue<BasicNumber>) = QuantityValue(ops.pure(amount), unit)

    @Test
    fun ratioOf_whenOneProduct_shouldNeedNoAllocation() {
        val carrot = product("carrot", QuantityValueFixture.fiftyPercent)
        assertEquals(1.0, Allocation(ops).ratioOf(carrot, process(carrot)), 1E-9)
    }

    @Test
    fun ratioOf_shouldDivideTheWeightByTheTotalWeight() {
        val carrot = product("carrot", QuantityValueFixture.eightyPercent)
        val salad = product("salad", QuantityValueFixture.twentyPercent)
        assertEquals(0.8, Allocation(ops).ratioOf(carrot, process(carrot, salad)), 1E-9)
    }

    @Test
    fun ratioOf_whenWeightsAreNotPercentages() {
        val carrot = product("carrot", quantity(1.0, UnitValueFixture.kg()))
        val salad = product("salad", quantity(3.0, UnitValueFixture.kg()))
        assertEquals(0.25, Allocation(ops).ratioOf(carrot, process(carrot, salad)), 1E-9)
    }

    @Test
    fun ratioOf_whenUnitsDifferWithTheSameDimension() {
        val carrot = product("carrot", quantity(1.0, UnitValueFixture.kg()))
        val salad = product("salad", quantity(500.0, UnitValueFixture.g()))
        assertEquals(2.0 / 3.0, Allocation(ops).ratioOf(carrot, process(carrot, salad)), 1E-9)
    }

    @Test
    fun problemOf_whenValid_shouldBeNull() {
        val carrot = product("carrot", quantity(1.0, UnitValueFixture.kg()))
        val salad = product("salad", quantity(500.0, UnitValueFixture.g()))
        assertNull(Allocation(ops).problemOf(process(carrot, salad)))
        assertNull(Allocation(ops).problemOf(process(product("carrot", null))))
    }

    @Test
    fun problemOf_whenDimensionsDiffer_shouldGiveTheProduct() {
        val carrot = product("carrot", quantity(1.0, UnitValueFixture.kg()))
        val salad = product("salad", QuantityValueFixture.twentyPercent)
        assertEquals(
            AllocationProblem(
                "incompatible dimensions: allocation of product 'salad' is none, allocation of product 'carrot' is mass (process: p)",
                1,
            ),
            Allocation(ops).problemOf(process(carrot, salad)),
        )
    }

    @Test
    fun problemOf_whenAllocationIsMissing_shouldGiveTheProduct() {
        val carrot = product("carrot", QuantityValueFixture.eightyPercent)
        val salad = product("salad", null)
        assertEquals(
            AllocationProblem("missing allocation for product 'salad' (process: p)", 1),
            Allocation(ops).problemOf(process(carrot, salad)),
        )
    }

    @Test
    fun problemOf_whenTotalIsZero() {
        val carrot = product("carrot", quantity(0.0, UnitValueFixture.kg()))
        val salad = product("salad", quantity(0.0, UnitValueFixture.kg()))
        assertEquals(
            AllocationProblem("the allocations of the products sum to zero (process: p)"),
            Allocation(ops).problemOf(process(carrot, salad)),
        )
    }

    @Test
    fun apply_whenAllocationIsInvalid_shouldThrow() {
        val system = SystemValue(mutableSetOf(process(product("carrot", null), product("salad", null))), mutableSetOf())
        assertFailsWith(EvaluatorException::class, "missing allocation for product 'carrot' (process: p)") {
            Allocation(ops).apply(system)
        }
    }
}
