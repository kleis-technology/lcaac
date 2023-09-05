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
    fun test_buildMapping_whenInvalid_thenError() {
        // given
        val invalidCSVReader = StringReader("")

        // when then
        assertFailsWith(IllegalArgumentException::class) {
            EcospoldMethodMapper.buildMapping(invalidCSVReader)
        }
    }

    @Test
    fun test_buildMapping_whenNoData_thenEmptyMap() {
        // given
        val emptyCSVReader = StringReader(
            listOf(
                "compartment_status",
                "conversion_factor",
                "flow_status",
                "id",
                "method_compartment",
                "method_name",
                "method_subcompartment",
                "method_unit",
                "name",
                "unitName"
            ).joinToString(",")
        )

        // when
        val map = EcospoldMethodMapper.buildMapping(emptyCSVReader)

        // then
        assert(map.isEmpty())
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
            FoundMappingExchange(
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
        assertEquals(
            OrphanMappingExchange(
                "d21da01e-f96f-4db5-9746-7b70db8a1f2c",
                "Ecoinvent orphan. Ecoinvent ID: d21da01e-f96f-4db5-9746-7b70db8a1f2c"
            ),
            mapping.first().value
        )
    }

    @Test
    fun test_getConversionFactor() {
        // given
        val values = listOf("", "1.0", "2.5", "5", "abc")
        val expected = listOf(null, null, 2.5, 5.0, null)

        // when
        val result = values.map(EcospoldMethodMapper::getConversionFactor)

        // then
        expected.zip(result).forEach { (expected, actual) ->
            assertEquals(expected, actual)
        }
    }

    @Test
    fun test_whenMappingMethodFieldsEmpty_thenNull() {
        // given
        val givenReader = StringReader(
            listOf(
                "compartment_status",
                "conversion_factor",
                "flow_status",
                "id",
                "method_compartment",
                "method_name",
                "method_subcompartment",
                "method_unit",
                "name",
                "unitName"
            ).joinToString(",") + "\n" + """
                   mapped,1.0,mapped,abc-you-and-me-an-id,,,,,eiName,eiUnit
                """.trimIndent()
        )

        // when
        val result = EcospoldMethodMapper.buildMapping(givenReader)

        // then
        assertEquals(
            FoundMappingExchange(
                "abc-you-and-me-an-id",
                null,
                null,
                null,
                null,
                null,
                "Ecoinvent ID: abc-you-and-me-an-id. Flow, compartment status: mapped, mapped"
            ),
            result.first().value
        )
    }

    @Test
    fun test_whenMappingMethodFieldsFull_thenNotNull() {
        // given
        val givenReader = StringReader(
            listOf(
                "compartment_status",
                "conversion_factor",
                "flow_status",
                "id",
                "method_compartment",
                "method_name",
                "method_subcompartment",
                "method_unit",
                "name",
                "unitName"
            ).joinToString(",") + "\n" + """
                   mapped,2.57,mapped,abc-you-and-me-an-id,method_comp,method_name,method_subcomp,method_unit,eiName,eiUnit
                """.trimIndent()
        )

        // when
        val result = EcospoldMethodMapper.buildMapping(givenReader)

        // then
        assertEquals(
            FoundMappingExchange(
                "abc-you-and-me-an-id",
                2.57,
                "method_name",
                "method_unit",
                "method_comp",
                "method_subcomp",
                "Ecoinvent ID: abc-you-and-me-an-id. Flow, compartment status: mapped, mapped"
            ),
            result.first().value
        )
    }

    @Test
            /***
             * This test is necessary because PEF will try and use m2*a as a unit, though in
             * Ecoinvent's DB and imported units, 1 a = 0.01 ha (dimension = length * length)
             */
    fun test_whenMappingFrom_m2year_to_m2stara_thenKeep_m2year() {
        // given
        val givenReader = StringReader(
            listOf(
                "compartment_status",
                "conversion_factor",
                "flow_status",
                "id",
                "method_compartment",
                "method_name",
                "method_subcompartment",
                "method_unit",
                "name",
                "unitName"
            ).joinToString(",") + "\n" + """
                   mapped,1.0,mapped,abc-you-and-me-an-id,,,,m2*a,eiName,m2*year
                """.trimIndent()
        )

        // when
        val result = EcospoldMethodMapper.buildMapping(givenReader)

        // then
        assertEquals(
            FoundMappingExchange(
                "abc-you-and-me-an-id",
                null,
                null,
                "m2*year",
                null,
                null,
                "Ecoinvent ID: abc-you-and-me-an-id. Flow, compartment status: mapped, mapped"
            ),
            result.first().value
        )
    }
}