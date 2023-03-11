package ch.kleis.lcaplugin.core.lang.preprocessor

import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.fixture.ProductFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnitFixture
import org.junit.Assert.assertEquals
import org.junit.Test

class PreProcessorTest {
    @Test
    fun assemble_whenProducts_shouldReindex() {
        // given
        val pkg = Package(
            "hello",
            emptyList(),
            Environment(
                products = Register(
                    mapOf(
                        "carrot" to ProductFixture.carrot
                    )
                )
            )
        )
        val preprocessor = PreProcessor(
            pkg,
            emptyList()
        )

        // when
        val actual = preprocessor.assemble().products["hello.carrot"]

        // then
        assertEquals(ProductFixture.carrot, actual)
    }

    @Test
    fun assemble_whenUnits_shouldReindex() {
        // given
        val pkg = Package(
            "hello",
            emptyList(),
            Environment(
                units = Register(
                    mapOf(
                        "kg" to UnitFixture.kg,
                    )
                )
            )
        )
        val preprocessor = PreProcessor(
            pkg,
            emptyList()
        )

        // when
        val actual = preprocessor.assemble().units["hello.kg"]

        // then
        assertEquals(UnitFixture.kg, actual)
    }

    @Test
    fun assemble_whenProductsAndUnits_shouldSubstituteUnitRefOccurrences() {
        // given
        val pkg = Package(
            "hello",
            emptyList(),
            Environment(
                units = Register(
                    mapOf(
                        "kg" to UnitFixture.kg,
                    )
                ),
                products = Register(
                    mapOf(
                        "carrot" to EProduct("carrot", EUnitRef("kg"))
                    )
                )
            )
        )
        val preprocessor = PreProcessor(
            pkg,
            emptyList()
        )

        // when
        val actual = preprocessor.assemble().products["hello.carrot"]

        // then
        val expected = EProduct("carrot", EUnitRef("hello.kg"))
        assertEquals(expected, actual)
    }

    @Test
    fun assemble_whenComplexUnits_shouldSubstitute() {
        // given
        val pkg = Package(
            "hello",
            emptyList(),
            Environment(
                units = Register(
                    mapOf(
                        "kg" to UnitFixture.kg,
                        "l" to UnitFixture.l,
                        "foo" to EUnitMul(EUnitRef("kg"), EUnitRef("l"))
                    )
                ),
            )
        )
        val preprocessor = PreProcessor(
            pkg,
            emptyList()
        )

        // when
        val actual = preprocessor.assemble().units["hello.foo"]

        // then
        val expected = EUnitMul(EUnitRef("hello.kg"), EUnitRef("hello.l"))
        assertEquals(expected, actual)
    }

    @Test
    fun assemble_whenProductRef() {
        // given
        val pkg = Package(
            "hello",
            emptyList(),
            Environment(
                quantities = Register(
                    mapOf(
                        "q" to QuantityFixture.oneKilogram,
                    )
                ),
                products = Register(
                    mapOf(
                        "carrot" to ProductFixture.carrot,
                    )
                ),
                processTemplates = Register(
                    mapOf(
                        "p" to EProcessTemplate(
                            emptyMap(),
                            emptyMap(),
                            EProcess(
                                products = listOf(
                                    ETechnoExchange(EQuantityRef("q"), EProductRef("carrot"))
                                ),
                                inputs = emptyList(),
                                biosphere = emptyList(),
                            )
                        )
                    )
                )
            )
        )
        val preprocessor = PreProcessor(
            pkg,
            emptyList()
        )

        // when
        val actual = (preprocessor.assemble().processTemplates["hello.p"] as EProcessTemplate)
            .body as EProcess

        // then
        val expected = EProcess(
            products = listOf(
                ETechnoExchange(EQuantityRef("hello.q"), EProductRef("hello.carrot"))
            ),
            inputs = emptyList(),
            biosphere = emptyList(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun assemble_whenTemplateRef() {
        // given
        val pkg = Package(
            "hello",
            emptyList(),
            Environment(
                products = Register(
                    mapOf(
                        "carrot" to ProductFixture.carrot,
                    )
                ),
                processTemplates = Register(
                    mapOf(
                        "p" to EProcessTemplate(
                            emptyMap(),
                            emptyMap(),
                            EProcess(
                                products = listOf(
                                    ETechnoExchange(
                                        QuantityFixture.oneKilogram,
                                        EConstrainedProduct(
                                            EProductRef("carrot"),
                                            FromProcessRef(
                                                ETemplateRef("p2"),
                                                emptyMap(),
                                            )
                                        )
                                    )
                                ),
                                inputs = emptyList(),
                                biosphere = emptyList(),
                            )
                        ),
                        "p2" to ETemplateRef("p3")
                    )
                )
            )
        )
        val preprocessor = PreProcessor(
            pkg,
            emptyList()
        )

        // when
        val actual = (preprocessor.assemble().processTemplates["hello.p"] as EProcessTemplate)
            .body as EProcess

        // then
        val expected = EProcess(
            products = listOf(
                ETechnoExchange(
                    QuantityFixture.oneKilogram,
                    EConstrainedProduct(
                        EProductRef("hello.carrot"),
                        FromProcessRef(
                            ETemplateRef("hello.p2"),
                            emptyMap(),
                        )
                    )
                )
            ),
            inputs = emptyList(),
            biosphere = emptyList(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun assemble_whenImportSymbol_thenSubstitute() {
        // given
        val pkg = Package(
            "hello",
            listOf(
                ImportSymbol("prelude.units", "kg")
            ),
            Environment(
                products = Register(
                    mapOf(
                        "carrot" to EProduct("carrot", EUnitRef("kg"))
                    )
                )
            )
        )
        val deps = listOf(
            Package(
                "prelude.units",
                emptyList(),
                Environment(
                    units = Register(mapOf(
                        "kg" to UnitFixture.kg,
                        "l" to UnitFixture.l,
                    ))
                )
            )
        )
        val preprocessor = PreProcessor(
            pkg,
            deps
        )

        // when
        val actual = preprocessor.assemble().products["hello.carrot"]

        // then
        val expected = EProduct("carrot", EUnitRef("prelude.units.kg"))
        assertEquals(expected, actual)
    }

    @Test
    fun assemble_whenImportSymbol_thenDoNotImportEverything() {
        // given
        val pkg = Package(
            "hello",
            listOf(
                ImportSymbol("prelude.units", "kg")
            ),
            Environment(
                products = Register(
                    mapOf(
                        "water" to EProduct("water", EUnitRef("l"))
                    )
                )
            )
        )
        val deps = listOf(
            Package(
                "prelude.units",
                emptyList(),
                Environment(
                    units = Register(mapOf(
                        "kg" to UnitFixture.kg,
                        "l" to UnitFixture.l,
                    ))
                )
            )
        )
        val preprocessor = PreProcessor(
            pkg,
            deps
        )

        // when
        val actual = preprocessor.assemble().products["hello.water"]

        // then
        val expected = EProduct("water", EUnitRef("l"))
        assertEquals(expected, actual)
    }

    @Test
    fun assemble_whenImportWildcard_thenImportEverything() {
        // given
        val pkg = Package(
            "hello",
            listOf(
                ImportWildCard("prelude.units")
            ),
            Environment(
                products = Register(
                    mapOf(
                        "water" to EProduct("water", EUnitRef("l")),
                        "carrot" to EProduct("carrot", EUnitRef("kg")),
                    )
                )
            )
        )
        val deps = listOf(
            Package(
                "prelude.units",
                emptyList(),
                Environment(
                    units = Register(mapOf(
                        "kg" to UnitFixture.kg,
                        "l" to UnitFixture.l,
                    ))
                )
            )
        )
        val preprocessor = PreProcessor(
            pkg,
            deps
        )

        // when
        val water = preprocessor.assemble().products["hello.water"]
        val carrot = preprocessor.assemble().products["hello.carrot"]

        // then
        assertEquals(EProduct("water", EUnitRef("prelude.units.l")), water)
        assertEquals(EProduct("carrot", EUnitRef("prelude.units.kg")), carrot)
    }
}
