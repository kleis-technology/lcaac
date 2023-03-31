package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang.fixture.ProcessValueFixture.Companion.carrotProcessValue
import ch.kleis.lcaplugin.core.lang.fixture.ProductValueFixture.Companion.carrot
import org.junit.Test

class ObservableMatrixTest {
    @Test
    fun observableMatrix(){
        // Given
        val processes = setOf(carrotProcessValue)
        val observableProducts = setOf(carrot)
        // When
        val actual = ObservableMatrix(processes, setOf(), observableProducts, setOf()).matrix.value(0,0)
        // Then
        assert(actual == 1.0)
    }
}