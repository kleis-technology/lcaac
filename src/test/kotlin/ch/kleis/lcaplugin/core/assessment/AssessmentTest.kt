package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.fixture.ProductValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.SystemValueFixture.Companion.carrotSystem
import ch.kleis.lcaplugin.core.lang.value.BioExchangeValue
import ch.kleis.lcaplugin.core.lang.value.ProcessValue
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import ch.kleis.lcaplugin.core.lang.value.TechnoExchangeValue
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class AssessmentTest {
    @Test
    fun applyAllocation_when_no_allocation_should_change_nothing(){
        // Given
        val system = carrotSystem
        val assessment = Assessment(carrotSystem)
        // When
        val actual = assessment.applyAllocation(carrotSystem)
        // Then
        assertEquals(system, actual)
    }

    @Test
    fun applyAllocation_when_coProducts_should_duplicate_process(){
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
        val assessment = Assessment(system)
        // When
        val actual = assessment.applyAllocation(system).processes.size
        // Then
        assertEquals(2, actual)
    }

    @Test
    fun applyAllocation_when_coProducts_should_keep_only_one_product_per_process(){
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
        val assessment = Assessment(system)
        // When
        val actual = assessment.applyAllocation(system).processes.toList()[0].products.size
        // Then
        assertEquals(1, actual)
    }

    @Test
    fun applyAllocation_when_allocation_should_divide_inputs_quantities(){
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
        val assessment = Assessment(system)
        // When
        val actual = assessment.applyAllocation(system).processes.toList()[0].inputs[0].quantity
        // Then
        val expected = QuantityValueFixture.oneLitre
        assertEquals(expected, actual)
    }

    @Test
    fun applyAllocation_when_allocation_should_divide_biosphere_quantities(){
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
        val assessment = Assessment(system)
        // when
        val actual = assessment.applyAllocation(system).processes.toList()[0].biosphere[0].quantity
        // then
        val expected = QuantityValueFixture.oneKilogram
        assertEquals(expected, actual)
    }

    @Test
    fun totalAllocation_whenOneProduct_shouldReturnOne(){
        // given
        val assessment = Assessment(SystemValue.empty())
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
        val actual = assessment.totalAllocation(processValue)
        // then
        val delta = 1E-9
        assertEquals(1.0, actual, delta)
    }

    @Test
    fun totalAllocation_whenTwoProduct_shouldSumAllocation(){
        // given
        val assessment = Assessment(SystemValue.empty())
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
        val actual = assessment.totalAllocation(processValue)
        // then
        val delta = 1E-9
        assertEquals(50.0, actual, delta)
    }

    @Test
    fun allocationUnitCheck_whenConsistentUnits_shouldNotThrowAnError(){
        // given
        val assessment = Assessment(SystemValue.empty())
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
            assessment.allocationUnitCheck(processValue)
        } catch (e: AssertionError) {
            fail("Should not fail")
        }
    }

    @Test
    fun allocationUnitCheck_whenNonConsistentUnits_shouldThrowAnError(){
        // given
        val assessment = Assessment(SystemValue.empty())
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
            assessment.allocationUnitCheck(processValue)
            fail("Should throw an error")
        } catch (e: EvaluatorException) {
            assertEquals("non-consistent allocation units for process carrot", e.message)
        }
    }

    @Test
    fun applyAllocation_whenTwoProduct_shouldWeightAllocations(){
        // given
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
        val assessment = Assessment(system)
        // when
        val actual = assessment.applyAllocation(system).processes.first().inputs.first().quantity.amount

        // then
        val delta = 1E-9
        val totalAllocation = QuantityValueFixture.twenyPiece.amount + QuantityValueFixture.thirtyPiece.amount
        val expected = QuantityValueFixture.twoLitres.amount*QuantityValueFixture.twenyPiece.amount/totalAllocation
        assertEquals(expected, actual, delta)
    }
}