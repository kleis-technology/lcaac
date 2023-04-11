package ch.kleis.lcaplugin.core.allocation

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.fixture.ProductValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceCharacterizationValueFixture.Companion.propanolCharacterization
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.SystemValueFixture
import ch.kleis.lcaplugin.core.lang.value.*
import org.junit.Assert
import org.junit.Test

class AllocationTest {
    @Test
    fun apply_when_no_allocation_should_change_nothing(){
        // Given
        val system = SystemValueFixture.carrotSystem
        val allocation = Allocation()
        // When
        val actual = allocation.apply(SystemValueFixture.carrotSystem)
        // Then
        Assert.assertEquals(system, actual)
    }

    @Test
    fun apply_when_coProducts_should_duplicate_process(){
        // Given
        val system = SystemValue(
            setOf(
                ProcessValue(
                    "carrot",
                    listOf(
                        TechnoExchangeValue(
                            QuantityValueFixture.oneKilogram,
                            ProductValueFixture.carrot
                        ),
                        TechnoExchangeValue(
                            QuantityValueFixture.oneKilogram,
                            ProductValueFixture.salad
                        )
                    ),
                    listOf(),
                    listOf()
                )
            ),
            setOf()
        )
        val allocation = Allocation()
        // When
        val actual = allocation.apply(system).processes.size
        // Then
        Assert.assertEquals(2, actual)
    }

    @Test
    fun apply_when_coProducts_should_keep_only_one_product_per_process(){
        // Given
        val system = SystemValue(
            setOf(
                ProcessValue(
                    "carrot",
                    listOf(
                        TechnoExchangeValue(
                            QuantityValueFixture.oneKilogram,
                            ProductValueFixture.carrot
                        ),
                        TechnoExchangeValue(
                            QuantityValueFixture.oneKilogram,
                            ProductValueFixture.salad
                        )
                    ),
                    listOf(),
                    listOf()
                )
            ),
            setOf()
        )
        val allocation = Allocation()
        // When
        val actual = allocation.apply(system).processes.toList()[0].products.size
        // Then
        Assert.assertEquals(1, actual)
    }

    @Test
    fun apply_when_allocation_should_divide_inputs_quantities(){
        // Given
        val system = SystemValue(
            setOf(
                ProcessValue(
                    "carrot",
                    listOf(
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
                    listOf(
                        TechnoExchangeValue(
                            QuantityValueFixture.twoLitres,
                            ProductValueFixture.water
                        )
                    ),
                    listOf()
                )
            ),
            setOf()
        )
        val allocation = Allocation()
        // When
        val actual = allocation.apply(system).processes.toList()[0].inputs[0].quantity
        // Then
        val expected = QuantityValueFixture.oneLitre
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun apply_when_allocation_should_divide_biosphere_quantities(){
        // given
        val system = SystemValue(
            setOf(
                ProcessValue(
                    "carrot",
                    listOf(
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
                    listOf(),
                    listOf(
                        BioExchangeValue(
                            QuantityValueFixture.twoKilograms,
                            SubstanceValueFixture.propanol
                        )
                    )
                )
            ),
            setOf()
        )
        val allocation = Allocation()
        // when
        val actual = allocation.apply(system).processes.toList()[0].biosphere[0].quantity
        // then
        val expected = QuantityValueFixture.oneKilogram
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun totalAmount_whenOneProduct_shouldReturnOne(){
        // given
        val allocation = Allocation()
        val processValue = ProcessValue(
            "carrot",
            listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot
                )
            ),
            listOf(),
            listOf()
        )
        // when
        val actual = allocation.totalAmount(processValue)
        // then
        val delta = 1E-9
        Assert.assertEquals(1.0, actual, delta)
    }

    @Test
    fun totalAmount_whenTwoProduct_shouldSumAllocation(){
        // given
        val allocation = Allocation()
        val processValue = ProcessValue(
            "carrot",
            listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot,
                    QuantityValueFixture.twenyPiece
                ),
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.salad,
                    QuantityValueFixture.thirtyPiece
                )
            ),
            listOf(),
            listOf()
        )
        // when
        val actual = allocation.totalAmount(processValue)
        // then
        val delta = 1E-9
        Assert.assertEquals(50.0, actual, delta)
    }

    @Test
    fun allocationUnitCheck_whenConsistentUnits_shouldNotThrowAnError(){
        // given
        val allocation = Allocation()
        val processValue = ProcessValue(
            "carrot",
            listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot,
                    QuantityValueFixture.twenyPiece
                ),
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.salad,
                    QuantityValueFixture.thirtyPiece
                )
            ),
            listOf(),
            listOf()
        )
        // when + then
        try {
            allocation.allocationUnitCheck(processValue)
        } catch (e: AssertionError) {
            Assert.fail("Should not fail")
        }
    }

    @Test
    fun allocationUnitCheck_whenNonConsistentUnits_shouldThrowAnError(){
        // given
        val allocation = Allocation()
        val processValue = ProcessValue(
            "carrot",
            listOf(
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.carrot,
                    QuantityValueFixture.twenyPiece
                ),
                TechnoExchangeValue(
                    QuantityValueFixture.oneKilogram,
                    ProductValueFixture.salad,
                    QuantityValueFixture.oneKilogram
                )
            ),
            listOf(),
            listOf()
        )
        // when + then
        try {
            allocation.allocationUnitCheck(processValue)
            Assert.fail("Should throw an error")
        } catch (e: EvaluatorException) {
            Assert.assertEquals("non-consistent allocation units for process carrot", e.message)
        }
    }

    @Test
    fun applyAllocation_whenTwoProduct_shouldWeightAllocations(){
        // given
        val allocation = Allocation()
        val system = SystemValue(
            setOf(
                ProcessValue(
                    "carrot",
                    listOf(
                        TechnoExchangeValue(
                            QuantityValueFixture.oneKilogram,
                            ProductValueFixture.carrot,
                            QuantityValueFixture.twenyPiece
                        ),
                        TechnoExchangeValue(
                            QuantityValueFixture.oneKilogram,
                            ProductValueFixture.salad,
                            QuantityValueFixture.thirtyPiece
                        )
                    ),
                    listOf(
                        TechnoExchangeValue(
                            QuantityValueFixture.twoLitres,
                            ProductValueFixture.water
                        )
                    ),
                    listOf()
                ),
            ),
            setOf()
        )
        // when
        val actual = allocation.apply(system).processes.first().inputs.first().quantity.amount

        // then
        val delta = 1E-9
        val totalAllocation = QuantityValueFixture.twenyPiece.amount + QuantityValueFixture.thirtyPiece.amount
        val expected = QuantityValueFixture.twoLitres.amount* QuantityValueFixture.twenyPiece.amount/totalAllocation
        Assert.assertEquals(expected, actual, delta)
    }

    @Test
    fun apply_shouldKeepAllocation(){
        // given
        val system = SystemValue(
            setOf(ProcessValue("", listOf(), listOf(), listOf())),
            setOf(propanolCharacterization)
        )
        // when
        val actual = Allocation().apply(system).substanceCharacterizations
        // then
        Assert.assertEquals(setOf(propanolCharacterization), actual)
    }
}