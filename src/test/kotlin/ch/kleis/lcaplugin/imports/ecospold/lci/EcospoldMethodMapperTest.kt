package ch.kleis.lcaplugin.imports.ecospold.lci

import com.jetbrains.rd.util.first
import java.io.InputStreamReader
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class EcospoldMethodMapperTest {
    @Test
    fun test_buildMapping_whenNoData_thenError() {
        // given
        val emptyCSVReader = StringReader("")

        // when then
        assertFailsWith(IllegalArgumentException::class) {
            EcospoldMethodMapper.buildMapping(emptyCSVReader)
        }
    }

    @Test
    fun test_buildMapping_whenMapped_thenMap() {
        // given when
        val mapping = this::class.java.getResourceAsStream("one-record-map.csv")?.use {
            EcospoldMethodMapper.buildMapping(InputStreamReader(it))
        }
        // then
        assertNotNull(mapping)
        assertEquals("584ffb1c-036d-417b-a9d1-1ec694dc2cdc", mapping.first().key)
        assertEquals(
            MappingExchange(
                "584ffb1c-036d-417b-a9d1-1ec694dc2cdc",
                null,
                "1,2-dichlorobenzene",
                "kg",
                "Emissions to air",
                "Emissions to air, unspecified (long-term)",
                "Ecoinvent ID: 584ffb1c-036d-417b-a9d1-1ec694dc2cdc. Flow, compartment status: mapped, mapped"
            ),
            mapping.first().value
        )
    }

    @Test
    fun test_buildMapping_whenOrphan_thenMap() {
        // given when
        val mapping = this::class.java.getResourceAsStream("orphan.csv")?.use {
            EcospoldMethodMapper.buildMapping(InputStreamReader(it))
        }

        // then
        assertNotNull(mapping)
        assertEquals(MappingExchange(
            "d21da01e-f96f-4db5-9746-7b70db8a1f2c",
            null, null, null, null, null,
            "Ecoinvent orphan. Ecoinvent ID: d21da01e-f96f-4db5-9746-7b70db8a1f2c"),
            mapping.first().value
        )
    }
}