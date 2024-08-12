package ch.kleis.lcaac.core.datasource.resilio_db.api

import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.prelude.Prelude
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class RdbResponseTest {
    @Test
    fun decode() {
        // given
        val body = RdbFixture.responseBody
        val deserializer = RdbResponseDeserializer(BasicOperations)

        // when
        val actual = deserializer.decodeFromString(
            "server-01",
            body,
        )

        // then
        assertEquals("server-01", actual.id)
        assertEquals(EQuantityScale(
            BasicNumber(207.11966698769282),
            Prelude.unitMap<BasicNumber>()["kg_CO2_Eq"]!!,
        ), actual.manufacturing.entries["GWP"])
    }
}
