package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.register.DataRegister
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LoaderTest {
    @Test
    fun load_overriddenGlobals() {
        // given
        val file = LcaLangFixture.parser("""
            variables {
               x = hour
            }
        """.trimIndent()).lcaFile()
        val loader = Loader(
            BasicOperations,
            mapOf(DataKey("x") to EDataRef("day"))
        )

        // when
        val actual = loader.load(sequenceOf(file))
            .getData("x")

        // then
        val expected = EDataRef<BasicNumber>("day")
        assertEquals(expected, actual)
    }

    @Test
    fun load_params_defaultRecord() {
        // given
        val file = LcaLangFixture.parser("""
            datasource source {
                location = "file.csv"
                schema {
                    mass = 1 kg
                }
            }
            
            process p {
                params {
                    row = default_record from source
                }
            }
        """.trimIndent()).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))
            .getTemplate("p")!!
            .params["row"]!!

        // then
        val expected = EDefaultRecordOf<BasicNumber>(EDataSourceRef("source"))
        assertEquals(expected, actual)
    }

    @Test
    fun load_whenSumOpInProduct_referenceUnitClosureShouldContainDataSource() {
        // given
        val file = LcaLangFixture.parser(
            """
            datasource source {
                location = "file.csv"
                schema {
                    mass = 1 kg
                }
            }
            
            process p {
                products {
                    sum(source["mass"]) p
                }
            }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val symbolTable = loader.load(sequenceOf(file))
        val referenceUnit = symbolTable
            .getTemplate("p")!!
            .body.products[0]
            .product.referenceUnit!! as EUnitOf
        val closure = referenceUnit.expression as EQuantityClosure
        val actual = closure.symbolTable


        // then
        val expected = EDataSource(
            config = DataSourceConfig(
                name = "source",
                location = "file.csv",
            ),
            schema = mapOf(
                "mass" to EQuantityScale(BasicNumber(1.0), EDataRef("kg")),
            )
        )
        assertEquals(expected, actual.getDataSource("source"))
    }

    @Test
    fun load_datasource() {
        // given
        val file = LcaLangFixture.parser(
            """
            datasource source {
                location = "file.csv"
                schema {
                    mass = 1 kg
                    geo = "FR"
                }
            }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))
            .getDataSource("source")!!

        // then
        val expected = EDataSource(
            config = DataSourceConfig(
                name = "source",
                location = "file.csv",
            ),
            schema = mapOf(
                "mass" to EQuantityScale(BasicNumber(1.0), EDataRef("kg")),
                "geo" to EStringLiteral("FR"),
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun load_whenFileContainsTest_thenNoError() {
        val file = LcaLangFixture.parser(
            """
                process p {
                    products {
                        1 kg p
                    }
                    impacts {
                        1 kg a
                    }
                }
                
                test another {
                    given {
                        1 kg foo
                    }
                    assert {
                        bar between 40 g and 50 kg
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        assertNotNull(actual.getTemplate("p"))
    }

    @Test
    fun load_twoProcesses_sameNameDifferentLabels() {
        // given
        val file = LcaLangFixture.parser(
            """
                process p {
                    labels {
                        id = "small"
                    }
                    inputs {
                        1 kg a from q
                    }
                }
                
                process p {
                    labels {
                        id = "large"
                    }
                    inputs {
                        2 kg a from q
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        assertNotNull(actual.getTemplate("p", mapOf("id" to "small")))
        assertNotNull(actual.getTemplate("p", mapOf("id" to "large")))
    }

    @Test
    fun load_fromProcess_withoutArguments() {
        // given
        val file = LcaLangFixture.parser(
            """
                process p {
                    inputs {
                        1 kg a from q
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val input = loader.load(sequenceOf(file)).getTemplate("p")!!
            .body
            .inputs[0] as ETechnoBlockEntry<BasicNumber>
        val actual = input.entry
            .product.fromProcess!!

        // then
        val expected = FromProcess<BasicNumber>("q", MatchLabels(emptyMap()))
        assertEquals(expected, actual)
    }

    @Test
    fun load_unit_literal() {
        // given
        val file = LcaLangFixture.parser(
            """
                unit foo {
                    symbol = "foo"
                    dimension = "foo"
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file)).getData("foo") as EUnitLiteral<BasicNumber>

        // then
        val expected = EUnitLiteral<BasicNumber>(UnitSymbol.of("foo"), 1.0, Dimension.of("foo"))
        assertEquals(expected, actual)
    }

    @Test
    fun load_unit_aliasFor() {
        // given
        val file = LcaLangFixture.parser(
            """
                unit foo {
                    symbol = "foo"
                    alias_for = 2 bar
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file)).getData("foo") as EUnitAlias<BasicNumber>

        // then
        val expected = EUnitAlias("foo", EQuantityScale(BasicNumber(2.0), EDataRef("bar")))
        assertEquals(expected, actual)
    }

    @Test
    fun load_twoSubstanceCharacterizations_sameExceptForType() {
        // given
        val file = LcaLangFixture.parser(
            """
                substance co2 {
                    name = "carbon dioxide"
                    type = Emission
                    compartment = "Emissions to air"
                    reference_unit = kg
                    
                    impacts {
                        1 kg GWP
                    }
                }
                
                substance co2 {
                    name = "carbon dioxide"
                    type = Resource
                    compartment = "Emissions to air"
                    reference_unit = kg
                    
                    impacts {
                        -1 kg GWP
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))


        // then
        assertNotNull(actual.getSubstanceCharacterization("co2", SubstanceType.EMISSION, "Emissions to air"))
        assertNotNull(actual.getSubstanceCharacterization("co2", SubstanceType.RESOURCE, "Emissions to air"))
    }

    @Test
    fun load_twoSubstanceCharacterizations_sameExceptForCompartment() {
        // given
        val file = LcaLangFixture.parser(
            """
                substance co2 {
                    name = "carbon dioxide"
                    type = Emission
                    compartment = "foo"
                    reference_unit = kg
                    
                    impacts {
                        1 kg GWP
                    }
                }
                
                substance co2 {
                    name = "carbon dioxide"
                    type = Emission
                    compartment = "bar"
                    reference_unit = kg
                    
                    impacts {
                        -1 kg GWP
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))


        // then
        assertNotNull(actual.getSubstanceCharacterization("co2", SubstanceType.EMISSION, "foo"))
        assertNotNull(actual.getSubstanceCharacterization("co2", SubstanceType.EMISSION, "bar"))
    }

    @Test
    fun load_twoSubstanceCharacterizations_sameExceptForSubCompartment() {
        // given
        val file = LcaLangFixture.parser(
            """
                substance co2 {
                    name = "carbon dioxide"
                    type = Emission
                    compartment = "foo"
                    reference_unit = kg
                    
                    impacts {
                        1 kg GWP
                    }
                }
                
                substance co2 {
                    name = "carbon dioxide"
                    type = Emission
                    compartment = "foo"
                    sub_compartment = "low pop"
                    reference_unit = kg
                    
                    impacts {
                        -1 kg GWP
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))


        // then
        assertNotNull(actual.getSubstanceCharacterization("co2", SubstanceType.EMISSION, "foo"))
        assertNotNull(actual.getSubstanceCharacterization("co2", SubstanceType.EMISSION, "foo", "low pop"))
    }

    @Test
    fun load_substanceCharacterization() {
        // given
        val file = LcaLangFixture.parser(
            """
                substance co2 {
                    name = "carbon dioxide"
                    type = Emission
                    compartment = "Emissions to air"
                    reference_unit = kg
                    
                    impacts {
                        1 kg GWP
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val oneKg = EQuantityScale(BasicNumber(1.0), EDataRef("kg"))
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val compartment = "Emissions to air"
        val expected = ESubstanceCharacterization(
            referenceExchange = EBioExchange(
                EDataRef("kg"), ESubstanceSpec(
                name = "co2",
                displayName = "carbon dioxide",
                type = SubstanceType.EMISSION,
                compartment = compartment,
                subCompartment = null,
                referenceUnit = EUnitOf(EDataRef("kg")),
            )
            ),
            impacts = listOf(
                EImpactBlockEntry(
                    EImpact(
                        oneKg,
                        EIndicatorSpec("GWP", null)
                    )
                )
            ),
        )
        assertEquals(expected, actual.getSubstanceCharacterization("co2", SubstanceType.EMISSION, compartment))
    }

    @Test
    fun load_substanceCharacterization_withSubCompartment() {
        // given
        val file = LcaLangFixture.parser(
            """
                substance co2 {
                    name = "carbon dioxide"
                    type = Emission
                    compartment = "Emissions to air"
                    sub_compartment = "Emissions to air, unspecified"
                    reference_unit = kg
                    
                    impacts {
                        1 kg GWP
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val oneKg = EQuantityScale(BasicNumber(1.0), EDataRef("kg"))
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val compartment = "Emissions to air"
        val subCompartment = "Emissions to air, unspecified"
        val expected = ESubstanceCharacterization(
            referenceExchange = EBioExchange(
                EDataRef("kg"), ESubstanceSpec(
                name = "co2",
                displayName = "carbon dioxide",
                type = SubstanceType.EMISSION,
                compartment = compartment,
                subCompartment = subCompartment,
                referenceUnit = EUnitOf(EDataRef("kg")),
            )
            ),
            impacts = listOf(
                EImpactBlockEntry(
                    EImpact(
                        oneKg,
                        EIndicatorSpec("GWP", null)
                    )
                )
            ),
        )
        assertEquals(
            expected,
            actual.getSubstanceCharacterization("co2", SubstanceType.EMISSION, compartment, subCompartment)
        )
    }

    @Test
    fun load_process_empty() {
        // given
        val file = LcaLangFixture.parser(
            """
                process p {
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val expected = EProcessTemplate<BasicNumber>(
            body = EProcess(
                "p",
            )
        )
        assertEquals(expected, actual.getTemplate("p"))
    }

    @Test
    fun load_process_labels() {
        // given
        val file = LcaLangFixture.parser(
            """
                process p {
                    labels {
                        geo = "FR"
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val expected = EProcessTemplate<BasicNumber>(
            body = EProcess(
                "p",
                labels = mapOf("geo" to EStringLiteral("FR")),
            )
        )
        assertEquals(expected, actual.getTemplate("p", mapOf("geo" to "FR")))
    }

    @Test
    fun load_process_products() {
        // given
        val file = LcaLangFixture.parser(
            """
                process p {
                    products {
                        1 kg out1 allocate 50 percent
                        1 kg out2 allocate 50 percent
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val oneKg = EQuantityScale(BasicNumber(1.0), EDataRef("kg"))
        val fiftyPercent = EQuantityScale(BasicNumber(50.0), EDataRef("percent"))
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val oneKgClosed = EUnitOf(EQuantityClosure(SymbolTable.empty(), oneKg))
        val expected = EProcessTemplate(
            body = EProcess(
                "p",
                products = listOf(
                    ETechnoExchange(oneKg, EProductSpec("out1", oneKgClosed), fiftyPercent),
                    ETechnoExchange(oneKg, EProductSpec("out2", oneKgClosed), fiftyPercent),
                )
            )
        )
        assertEquals(expected, actual.getTemplate("p"))
    }

    @Test
    fun load_process_products_withClosure() {
        // given
        val file = LcaLangFixture.parser(
            """
                variables {
                    x = 1 kg
                }
                process p {
                    variables {
                        y = 1 kg
                    }
                    products {
                        x + y out
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val oneKg = EQuantityScale(BasicNumber(1.0), EDataRef("kg"))
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val sum = EQuantityAdd<BasicNumber>(EDataRef("x"), EDataRef("y"))
        val localTable = SymbolTable(
            data = DataRegister(
                mapOf(
                    "x" to oneKg,
                    "y" to oneKg,
                ).mapKeys { DataKey(it.key) }
            )
        )
        val referenceUnit = EUnitOf(
            EQuantityClosure(
                localTable,
                sum,
            )
        )
        val expected = EProcessTemplate(
            locals = mapOf(
                "y" to oneKg,
            ),
            body = EProcess(
                "p",
                products = listOf(
                    ETechnoExchange(sum, EProductSpec("out", referenceUnit)),
                )
            )
        )
        assertEquals(expected, actual.getTemplate("p"))
    }

    @Test
    fun load_process_params() {
        // given
        val file = LcaLangFixture.parser(
            """
                process p {
                    params {
                        x = 1 kg
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val oneKg = EQuantityScale(BasicNumber(1.0), EDataRef("kg"))
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val expected = EProcessTemplate(
            params = mapOf(
                "x" to oneKg,
            ),
            body = EProcess("p")
        )
        assertEquals(expected, actual.getTemplate("p"))
    }

    @Test
    fun load_process_variables() {
        // given
        val file = LcaLangFixture.parser(
            """
                process p {
                    variables {
                        x = 1 kg
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val oneKg = EQuantityScale(BasicNumber(1.0), EDataRef("kg"))
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val expected = EProcessTemplate(
            locals = mapOf(
                "x" to oneKg,
            ),
            body = EProcess("p")
        )
        assertEquals(expected, actual.getTemplate("p"))
    }

    @Test
    fun load_process_inputs() {
        // given
        val file = LcaLangFixture.parser(
            """
                process p {
                    inputs {
                        1 kg q_in from q(a = 1 kg) match (geo = "FR")
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val oneKg = EQuantityScale(BasicNumber(1.0), EDataRef("kg"))
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val expected = EProcessTemplate(
            body = EProcess(
                "p",
                inputs = listOf(
                    ETechnoBlockEntry(
                        ETechnoExchange(
                            oneKg,
                            EProductSpec(
                                "q_in",
                                referenceUnit = null,
                                FromProcess(
                                    "q",
                                    matchLabels = MatchLabels(mapOf("geo" to EStringLiteral("FR"))),
                                    arguments = mapOf("a" to oneKg),
                                ),
                            ),
                        )
                    )
                )
            )
        )
        assertEquals(expected, actual.getTemplate("p"))
    }

    @Test
    fun load_process_emissions() {
        // given
        val file = LcaLangFixture.parser(
            """
                process p {
                    emissions {
                        1 kg co2
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val oneKg = EQuantityScale(BasicNumber(1.0), EDataRef("kg"))
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val localTable = SymbolTable.empty<BasicNumber>()
        val referenceUnit = EUnitOf(EQuantityClosure(localTable, oneKg))
        val expected = EProcessTemplate(
            body = EProcess(
                "p",
                biosphere = listOf(
                    EBioBlockEntry(
                        EBioExchange(
                            oneKg,
                            ESubstanceSpec(
                                "co2",
                                "co2",
                                SubstanceType.EMISSION,
                                compartment = null,
                                subCompartment = null,
                                referenceUnit,
                            )
                        )
                    )
                )
            )
        )
        assertEquals(expected, actual.getTemplate("p"))
    }

    @Test
    fun load_process_emissions_withDetails() {
        // given
        val file = LcaLangFixture.parser(
            """
                process p {
                    emissions {
                        1 kg co2(compartment = "air", sub_compartment = "low pop")
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val oneKg = EQuantityScale(BasicNumber(1.0), EDataRef("kg"))
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val localTable = SymbolTable.empty<BasicNumber>()
        val referenceUnit = EUnitOf(EQuantityClosure(localTable, oneKg))
        val expected = EProcessTemplate(
            body = EProcess(
                "p",
                biosphere = listOf(
                    EBioBlockEntry(
                        EBioExchange(
                            oneKg,
                            ESubstanceSpec(
                                "co2",
                                "co2",
                                SubstanceType.EMISSION,
                                "air",
                                "low pop",
                                referenceUnit,
                            )
                        )
                    )
                )
            )
        )
        assertEquals(expected, actual.getTemplate("p"))
    }

    @Test
    fun load_process_resources() {
        // given
        val file = LcaLangFixture.parser(
            """
                process p {
                    resources {
                        1 kg co2
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val oneKg = EQuantityScale(BasicNumber(1.0), EDataRef("kg"))
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val localTable = SymbolTable.empty<BasicNumber>()
        val referenceUnit = EUnitOf(EQuantityClosure(localTable, oneKg))
        val expected = EProcessTemplate(
            body = EProcess(
                "p",
                biosphere = listOf(
                    EBioBlockEntry(
                        EBioExchange(
                            oneKg,
                            ESubstanceSpec(
                                "co2",
                                "co2",
                                SubstanceType.RESOURCE,
                                compartment = null,
                                subCompartment = null,
                                referenceUnit,
                            )
                        )
                    )
                )
            )
        )
        assertEquals(expected, actual.getTemplate("p"))
    }

    @Test
    fun load_process_resources_withDetails() {
        // given
        val file = LcaLangFixture.parser(
            """
                process p {
                    resources {
                        1 kg co2(compartment = "air", sub_compartment = "low pop")
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val oneKg = EQuantityScale(BasicNumber(1.0), EDataRef("kg"))
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val localTable = SymbolTable.empty<BasicNumber>()
        val referenceUnit = EUnitOf(EQuantityClosure(localTable, oneKg))
        val expected = EProcessTemplate(
            body = EProcess(
                "p",
                biosphere = listOf(
                    EBioBlockEntry(
                        EBioExchange(
                            oneKg,
                            ESubstanceSpec(
                                "co2",
                                "co2",
                                SubstanceType.RESOURCE,
                                "air",
                                "low pop",
                                referenceUnit,
                            )
                        )
                    )
                )
            )
        )
        assertEquals(expected, actual.getTemplate("p"))
    }

    @Test
    fun load_process_land_use() {
        // given
        val file = LcaLangFixture.parser(
            """
                process p {
                    land_use {
                        1 kg co2
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val oneKg = EQuantityScale(BasicNumber(1.0), EDataRef("kg"))
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val localTable = SymbolTable.empty<BasicNumber>()
        val referenceUnit = EUnitOf(EQuantityClosure(localTable, oneKg))
        val expected = EProcessTemplate(
            body = EProcess(
                "p",
                biosphere = listOf(
                    EBioBlockEntry(
                        EBioExchange(
                            oneKg,
                            ESubstanceSpec(
                                "co2",
                                "co2",
                                SubstanceType.LAND_USE,
                                compartment = null,
                                subCompartment = null,
                                referenceUnit,
                            )
                        )
                    )
                )
            )
        )
        assertEquals(expected, actual.getTemplate("p"))
    }

    @Test
    fun load_process_land_use_withDetails() {
        // given
        val file = LcaLangFixture.parser(
            """
                process p {
                    land_use {
                        1 kg co2(compartment = "air", sub_compartment = "low pop")
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val oneKg = EQuantityScale(BasicNumber(1.0), EDataRef("kg"))
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val localTable = SymbolTable.empty<BasicNumber>()
        val referenceUnit = EUnitOf(EQuantityClosure(localTable, oneKg))
        val expected = EProcessTemplate(
            body = EProcess(
                "p",
                biosphere = listOf(
                    EBioBlockEntry(
                        EBioExchange(
                            oneKg,
                            ESubstanceSpec(
                                "co2",
                                "co2",
                                SubstanceType.LAND_USE,
                                "air",
                                "low pop",
                                referenceUnit,
                            )
                        )
                    )
                )
            )
        )
        assertEquals(expected, actual.getTemplate("p"))
    }

    @Test
    fun load_process_impacts() {
        // given
        val file = LcaLangFixture.parser(
            """
                process p {
                    impacts {
                        1 kg co2
                    }
                }
            """.trimIndent()
        ).lcaFile()
        val oneKg = EQuantityScale(BasicNumber(1.0), EDataRef("kg"))
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val expected = EProcessTemplate(
            body = EProcess(
                "p",
                impacts = listOf(
                    EImpactBlockEntry(
                        EImpact(
                            oneKg,
                            EIndicatorSpec(
                                "co2",
                            )
                        )
                    )
                )
            )
        )
        assertEquals(expected, actual.getTemplate("p"))
    }

    @Test
    fun load_dataExpression_basic() {
        // given
        val file = LcaLangFixture.parser(
            """
                variables {
                    op_sum = x + y
                    op_mul = x * y
                    op_div = x / y
                    op_scale = 2 x
                    op_pow = x^2.0
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val sum = EQuantityAdd<BasicNumber>(EDataRef("x"), EDataRef("y"))
        val mul = EQuantityMul<BasicNumber>(EDataRef("x"), EDataRef("y"))
        val div = EQuantityDiv<BasicNumber>(EDataRef("x"), EDataRef("y"))
        val scale = EQuantityScale(BasicNumber(2.0), EDataRef("x"))
        val pow = EQuantityPow<BasicNumber>(EDataRef("x"), 2.0)
        assertEquals(sum, actual.getData("op_sum"))
        assertEquals(mul, actual.getData("op_mul"))
        assertEquals(div, actual.getData("op_div"))
        assertEquals(scale, actual.getData("op_scale"))
        assertEquals(pow, actual.getData("op_pow"))
    }

    @Test
    fun load_dataExpression_priorityMulAdd() {
        // given
        val file = LcaLangFixture.parser(
            """
                variables {
                    a = x * y + z
                    b = x + y * z
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val a = EQuantityAdd<BasicNumber>(EQuantityMul(EDataRef("x"), EDataRef("y")), EDataRef("z"))
        val b = EQuantityAdd<BasicNumber>(EDataRef("x"), EQuantityMul(EDataRef("y"), EDataRef("z")))
        assertEquals(a, actual.getData("a"))
        assertEquals(b, actual.getData("b"))
    }

    @Test
    fun load_dataExpression_priorityDivAdd() {
        // given
        val file = LcaLangFixture.parser(
            """
                variables {
                    a = x / y + z
                    b = x + y / z
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val a = EQuantityAdd<BasicNumber>(EQuantityDiv(EDataRef("x"), EDataRef("y")), EDataRef("z"))
        val b = EQuantityAdd<BasicNumber>(EDataRef("x"), EQuantityDiv(EDataRef("y"), EDataRef("z")))
        assertEquals(a, actual.getData("a"))
        assertEquals(b, actual.getData("b"))
    }

    @Test
    fun load_dataExpression_priorityScaleMul() {
        // given
        val file = LcaLangFixture.parser(
            """
                variables {
                    a = 3 x * y
                    b = x * 4 y
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val a = EQuantityScale(BasicNumber(3.0), EQuantityMul(EDataRef("x"), EDataRef("y")))
        val b = EQuantityMul(EDataRef("x"), EQuantityScale(BasicNumber(4.0), EDataRef("y")))
        assertEquals(a, actual.getData("a"))
        assertEquals(b, actual.getData("b"))
    }

    @Test
    fun load_dataExpression_priorityScaleDiv() {
        // given
        val file = LcaLangFixture.parser(
            """
                variables {
                    a = 3 x / y
                    b = x / 4 y
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val a = EQuantityScale(BasicNumber(3.0), EQuantityDiv(EDataRef("x"), EDataRef("y")))
        val b = EQuantityDiv(EDataRef("x"), EQuantityScale(BasicNumber(4.0), EDataRef("y")))
        assertEquals(a, actual.getData("a"))
        assertEquals(b, actual.getData("b"))
    }

    @Test
    fun load_dataExpression_priorityPowAdd() {
        // given
        val file = LcaLangFixture.parser(
            """
                variables {
                    a = x + y^2
                    b = x^2 + y
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val a = EQuantityAdd<BasicNumber>(EDataRef("x"), EQuantityPow(EDataRef("y"), 2.0))
        val b = EQuantityAdd<BasicNumber>(EQuantityPow(EDataRef("x"), 2.0), EDataRef("y"))
        assertEquals(a, actual.getData("a"))
        assertEquals(b, actual.getData("b"))
    }

    @Test
    fun load_dataExpression_leftAssociativeDiv() {
        // given
        val file = LcaLangFixture.parser(
            """
                variables {
                    a = x / y / z
                }
            """.trimIndent()
        ).lcaFile()
        val loader = Loader(BasicOperations)

        // when
        val actual = loader.load(sequenceOf(file))

        // then
        val a = EQuantityDiv<BasicNumber>(EQuantityDiv(EDataRef("x"), EDataRef("y")), EDataRef("z"))
        assertEquals(a, actual.getData("a"))
    }
}
