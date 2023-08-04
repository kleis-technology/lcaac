package ch.kleis.lcaplugin.imports.ecospold.lci

import ch.kleis.lcaplugin.core.lang.expression.SubstanceType
import ch.kleis.lcaplugin.imports.ecospold.model.ElementaryExchange
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
            ElementaryExchange(
                "584ffb1c-036d-417b-a9d1-1ec694dc2cdc",
                1.0,
                "1,2-dichlorobenzene",
                "kg",
                "Emissions to air",
                "Emissions to air, unspecified (long-term)",
                SubstanceType.EMISSION,
                "Mapped exchange"
            ),
            mapping.first().value
        )
    }

    @Test
    fun test_buildMapping_whenOrphan_thenNoMap() {
        // given when
        val mapping = this::class.java.getResourceAsStream("orphan.csv")?.use {
            EcospoldMethodMapper.buildMapping(InputStreamReader(it))
        }

        // then
        assertNotNull(mapping)
        assert(mapping.isEmpty())
    }

    @Test
    fun test_buildMapping_whenMixed_thenOnlyMapped() {
        // given when
        val mapping = this::class.java.getResourceAsStream("one-valid-one-invalid.csv")?.use {
            EcospoldMethodMapper.buildMapping(InputStreamReader(it))
        }
        // then
        assertNotNull(mapping)
        assertEquals(1, mapping.size)
        assertEquals("584ffb1c-036d-417b-a9d1-1ec694dc2cdc", mapping.first().key)
        assertEquals(
            ElementaryExchange(
                "584ffb1c-036d-417b-a9d1-1ec694dc2cdc",
                1.0,
                "1,2-dichlorobenzene",
                "kg",
                "Emissions to air",
                "Emissions to air, unspecified (long-term)",
                SubstanceType.EMISSION,
                "Mapped exchange"
            ),
            mapping.first().value
        )
    }
}