package ch.kleis.lcaplugin.imports.ecospold.lcai

import org.junit.Test
import spold2.EcoSpold2
import kotlin.test.assertNotNull

class LcaEcospoldTest {


    @Test
    fun testParsing_WithCustomClass() {
        // Given
        val input = this::class.java.getResourceAsStream("test.xml")

        // When
        val result = EcoSpold2.read(input)

        // Then
        assertNotNull(result.childDataSet)
    }

}