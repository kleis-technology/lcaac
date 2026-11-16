package ch.kleis.lcaac.core.lang.source

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.ProcessKey
import ch.kleis.lcaac.core.math.basic.BasicNumber
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SourceMapTest {
    private val process = ProcessKey("p")

    @Test
    fun get_whenUnknown_shouldReturnNull() {
        assertNull(SourceMap.empty()[SourcePath.Process(process)])
    }

    @Test
    fun plus_shouldMergeLocations() {
        // given
        val a = SourceMap(mapOf(SourcePath.Process(process) to SourceLocation("a.lca", 1, 1)))
        val b = SourceMap(mapOf(SourcePath.Global("x") to SourceLocation("b.lca", 2, 5)))

        // when
        val actual = a + b

        // then
        assertEquals(SourceLocation("a.lca", 1, 1), actual[SourcePath.Process(process)])
        assertEquals(SourceLocation("b.lca", 2, 5), actual[SourcePath.Global("x")])
    }

    @Test
    fun location_toString() {
        assertEquals("src/main.lca:7:5", SourceLocation("src/main.lca", 7, 5).toString())
    }

    private fun techno(name: String) = ETechnoExchange<BasicNumber>(EDataRef("q"), EProductSpec(name))
    private fun bio(name: String) = EBioExchange<BasicNumber>(EDataRef("q"), ESubstanceSpec(name))

    private val body = EProcess(
        name = "p",
        products = listOf(techno("out")),
        inputs = listOf(
            ETechnoBlockEntry(techno("a")),
            ETechnoBlockForEach("row", EDataSourceRef("source"), emptyMap(), listOf(ETechnoBlockEntry(techno("b")))),
        ),
        biosphere = listOf(EBioBlockEntry(bio("co2"))),
    )

    @Test
    fun blockAt_shouldResolveEachSection() {
        assertEquals(EBlockEntry(techno("out")), body.blockAt(BlockPath.of(Section.PRODUCTS, 0)))
        assertEquals(ETechnoBlockEntry(techno("a")), body.blockAt(BlockPath.of(Section.INPUTS, 0)))
        assertEquals(body.inputs[1], body.blockAt(BlockPath.of(Section.INPUTS, 1)))
        assertEquals(EBioBlockEntry(bio("co2")), body.blockAt(BlockPath.of(Section.BIOSPHERE, 0)))
    }

    @Test
    fun blockAt_shouldResolveForEachBody() {
        assertEquals(ETechnoBlockEntry(techno("b")), body.blockAt(BlockPath(Section.INPUTS, listOf(1, 0))))
    }

    @Test
    fun blockAt_whenNoBlock_shouldReturnNull() {
        assertNull(body.blockAt(BlockPath.of(Section.INPUTS, 2)))
        assertNull(body.blockAt(BlockPath.of(Section.IMPACTS, 0)))
        assertNull(body.blockAt(BlockPath(Section.INPUTS, listOf(0, 0))))
        assertNull(body.blockAt(BlockPath(Section.PRODUCTS, listOf(0, 0))))
        assertNull(body.blockAt(BlockPath(Section.INPUTS, emptyList())))
    }
}
