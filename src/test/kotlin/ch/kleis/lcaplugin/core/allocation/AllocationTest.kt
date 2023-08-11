package ch.kleis.lcaplugin.core.allocation

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.fixture.FullyQualifiedSubstanceValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.ProductValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceCharacterizationValueFixture.Companion.propanolCharacterization
import ch.kleis.lcaplugin.core.lang.fixture.SystemValueFixture
import ch.kleis.lcaplugin.core.lang.value.BioExchangeValue
import ch.kleis.lcaplugin.core.lang.value.ProcessValue
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import ch.kleis.lcaplugin.core.lang.value.TechnoExchangeValue
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertFailsWith

class AllocationTest {
    @Test
    fun apply_when_no_allocation_should_change_nothing() {
        // Given
        val system = SystemValueFixture.carrotSystem()
        // When
        val actual = Allocation.apply(SystemValueFixture.carrotSystem())
        // Then
        Assert.assertEquals(system, actual)
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
        val actual = Allocation.apply(system).processes.size
        // Then
        Assert.assertEquals(2, actual)
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
        val actual = Allocation.apply(system).processes.toList()[0].products.size
        // Then
        Assert.assertEquals(1, actual)
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
        val actual = Allocation.apply(system).processes.toList()[0].inputs[0].quantity
        // Then
        val expected = QuantityValueFixture.oneLitre
        Assert.assertEquals(expected, actual)
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
        val actual = Allocation.apply(system).processes.toList()[0].biosphere[0].quantity
        // then
        val expected = QuantityValueFixture.oneKilogram
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun totalAmount_whenOneProduct_shouldReturnOne() {
        // given

        val processValue = ProcessValue(
            name = "carrot",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot
                )
            ),
        )
        // when
        val actual = Allocation.totalAmount(processValue)
        // then
        val delta = 1E-9
        Assert.assertEquals(1.0, actual, delta)
    }

    @Test
    fun totalAmount_whenTwoProduct_shouldSumAllocation() {
        // given

        val processValue = ProcessValue(
            name = "carrot",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot,
                    QuantityValueFixture.eightyPercent
                ),
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.salad,
                    QuantityValueFixture.twentyPercent
                )
            ),
        )
        // when
        val actual = Allocation.totalAmount(processValue)
        // then
        val delta = 1E-9
        Assert.assertEquals(1.0, actual, delta)
    }

    @Test
    fun allocationUnitCheck_whenConsistentUnits_shouldNotThrowAnError() {
        // given

        val processValue = ProcessValue(
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
        // when
        Allocation.allocationUnitCheck(processValue)

        // then should not throw.
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
        val actual = Allocation.apply(system).processes.first().inputs.first().quantity.amount

        // then
        val delta = 1E-9
        val totalAllocation = QuantityValueFixture.twentyPercent.amount + QuantityValueFixture.eightyPercent.amount
        val expected =
            QuantityValueFixture.twoLitres.amount * QuantityValueFixture.twentyPercent.amount / totalAllocation
        Assert.assertEquals(expected, actual, delta)
    }

    @Test
    fun apply_shouldKeepAllocation() {
        // given
        val system = SystemValue(
            mutableSetOf(ProcessValue(name = "")),
            mutableSetOf(propanolCharacterization)
        )

        // when
        val actual = Allocation.apply(system).substanceCharacterizations

        // then
        Assert.assertEquals(setOf(propanolCharacterization), actual)
    }

    @Test
    fun allocationUnitCheck_whenAllocationUnitIsNotPercentage_shouldThrowAnError() {
        // given
        val processValue = ProcessValue(
            name = "carrot",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot,
                    QuantityValueFixture.hundredPiece
                )
            ),
        )

        // when + then
        assertFailsWith(
            EvaluatorException::class,
            "Only percent is allowed for allocation unit (process: ${processValue.name})"
        ) { Allocation.allocationUnitCheck(processValue) }
    }

    @Test
    fun allocationUnitCheck_whenAllocationUnitIsPercentage_shouldNotThrowAnError() {
        // given
        val processValue = ProcessValue(
            name = "carrot",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot,
                    QuantityValueFixture.hundredPercent
                )
            ),
        )

        // when, then should not throw
        Allocation.allocationUnitCheck(processValue)
    }

    @Test
    fun allocationUnitCheck_whenSumOfAllocationAreNotHundredPercent_shouldThrowAnError() {
        // given
        val processValue = ProcessValue(
            name = "carrot",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot,
                    QuantityValueFixture.fiftyPercent
                )
            ),
        )

        // when + then
        assertFailsWith(
            EvaluatorException::class,
            "The sum of the allocations should be hundred percent (process: ${processValue.name})"
        ) { Allocation.allocationUnitCheck(processValue) }
    }

    @Test
    fun allocationUnitCheck_whenSumOfAllocationAreHundredPercent_shouldNotThrowAnError() {
        // given
        val processValue = ProcessValue(
            name = "carrot",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot,
                    QuantityValueFixture.fiftyPercent
                ),
                TechnoExchangeValue(
                    QuantityValueFixture.twoKilograms,
                    ProductValueFixture.salad,
                    QuantityValueFixture.fiftyPercent
                )
            ),
        )

        // when V then should not throw.
        Allocation.allocationUnitCheck(processValue)
    }

    @Test
    fun allocationUnitCheck_whenNonConsistentUnits_shouldThrowAnError() {
        // given
        val processValue = ProcessValue(
            name = "carrot",
            products = listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot,
                    QuantityValueFixture.eightyPercent
                ),
                TechnoExchangeValue(
                    QuantityValueFixture.twoKilograms,
                    ProductValueFixture.salad,
                    QuantityValueFixture.twentyPiece
                )
            ),
        )

        // when + then
        assertFailsWith(
            EvaluatorException::class,
            "Only percent is allowed for allocation unit (process: ${processValue.name})"
        ) { Allocation.allocationUnitCheck(processValue) }
    }
}
