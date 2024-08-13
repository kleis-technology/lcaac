package ch.kleis.lcaac.core.datasource.resilio_db.api

import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.prelude.Prelude
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class RdbResponseDeserializerTest {
    private val lcStepMapping = LcStepMapping(
        key = "lc_step",
        manufacturing = "manufacturing",
        transport = "transport",
        use = "use",
        endOfLife = "end-of-life",
    )

    @Test
    fun decode_shouldMapImpact() {
        // given
        val body = RdbFixture.responseBody
        val deserializer = RdbResponseDeserializer(
            primaryKey = "foo_id",
            lcStepMapping = lcStepMapping,
            ops = BasicOperations,
        )

        // when
        val actual = deserializer.decodeFromString(
            "server-01",
            body,
        )

        // then
        assertEquals(EQuantityScale(
            BasicNumber(207.11966698769282),
            Prelude.unitMap<BasicNumber>()["kg_CO2_Eq"]!!,
        ), actual.manufacturing.entries["GWP"])
    }

    @Test
    fun decode_shouldMapId() {
        // given
        val body = RdbFixture.responseBody
        val deserializer = RdbResponseDeserializer(
            primaryKey = "foo_id",
            lcStepMapping = lcStepMapping,
            ops = BasicOperations,
        )

        // when
        val actual = deserializer.decodeFromString(
            "server-01",
            body,
        )

        // then
        assertEquals("server-01", actual.id)
        assertEquals(EStringLiteral<BasicNumber>("server-01"),
            actual.manufacturing.entries["foo_id"])
    }

    @Test
    fun decode_shouldMapLcStep() {
        // given
        val body = RdbFixture.responseBody
        val deserializer = RdbResponseDeserializer(
            primaryKey = "foo_id",
            lcStepMapping = lcStepMapping,
            ops = BasicOperations,
        )

        // when
        val actual = deserializer.decodeFromString(
            "server-01",
            body,
        )

        // then
        assertEquals(EStringLiteral<BasicNumber>("manufacturing"),
            actual.manufacturing.entries["lc_step"])
        assertEquals(EStringLiteral<BasicNumber>("transport"),
            actual.transport.entries["lc_step"])
        assertEquals(EStringLiteral<BasicNumber>("use"),
            actual.use.entries["lc_step"])
        assertEquals(EStringLiteral<BasicNumber>("end-of-life"),
            actual.endOfLife.entries["lc_step"])
    }
}
