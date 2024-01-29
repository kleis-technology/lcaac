package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.testing.RangeAssertion
import kotlin.test.Test
import kotlin.test.assertEquals


class CoreTestMapperTest {
    @Test
    fun test() {
        // given
        val content = """
            test foo {
                variables {
                    x = 1 kg
                }
                given {
                    1 kWh electricity
                }
                assert {
                    co2 between 1 kg and 2 kg
                }
            }
        """.trimIndent()
        val ctx = LcaLangFixture.parser(content).testDefinition()
        val mapper = CoreTestMapper()

        // when
        val actual = mapper.test(ctx)

        // then
        assertEquals(ctx, actual.source)
        assertEquals("foo", actual.name)
        assertEquals(
            listOf(
                RangeAssertion(
                    ref = "co2",
                    lo = EQuantityScale(BasicNumber(1.0), EDataRef("kg")),
                    hi = EQuantityScale(BasicNumber(2.0), EDataRef("kg")),
                )
            ), actual.assertions
        )
        assertEquals(
            EProcessTemplate(
                locals = mapOf(
                    "x" to EQuantityScale(BasicNumber(1.0), EDataRef("kg")),
                ),
                body = EProcess(
                    name = "__test__foo",
                    products = listOf(
                        ETechnoExchange(
                            EQuantityScale(BasicNumber(1.0), EDataRef("u")),
                            EProductSpec("__test__product__foo", EDataRef("u"))
                        )
                    ),
                    inputs = listOf(
                        ETechnoBlockEntry(
                            ETechnoExchange(
                                EQuantityScale(BasicNumber(1.0), EDataRef("kWh")),
                                EProductSpec("electricity"),
                            )
                        )
                    )
                )
            ), actual.template
        )
        assertEquals(emptyMap(), actual.arguments)
    }
}
