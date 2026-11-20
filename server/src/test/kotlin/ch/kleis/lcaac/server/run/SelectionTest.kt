package ch.kleis.lcaac.server.run

import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parseLcaText
import ch.kleis.lcaac.server.api.ApiDefinitionLoader
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Selections of rows on the pattern of the Cloud Assess trusted library: an entry process calls itself, with
 * the label `phase`, once per life cycle step. The entry and its steps share their product and process name.
 */
class SelectionTest {
    private val model = """
        process vm_fn {
            products {
                1 hour vm
            }
            inputs {
                1 hour vm from vm_fn(lc_step = "manufacturing", gwp = 2 kg_CO2_Eq) match (phase = "embodied")
                1 hour vm from vm_fn(lc_step = "end-of-life", gwp = 0.5 kg_CO2_Eq) match (phase = "embodied")
                1 hour vm from vm_fn() match (phase = "use")
            }
        }

        process vm_fn {
            labels {
                phase = "embodied"
            }
            params {
                lc_step = "manufacturing"
                gwp = 1 kg_CO2_Eq
            }
            products {
                1 hour vm
            }
            impacts {
                gwp GWP
            }
        }

        process vm_fn {
            labels {
                phase = "use"
            }
            products {
                1 hour vm
            }
            inputs {
                10 kWh electricity from grid()
            }
        }

        process grid {
            products {
                1 kWh electricity
            }
            impacts {
                0.1 kg_CO2_Eq GWP
            }
        }
    """.trimIndent()

    private val runner = OperationRunner(
        Loader(BasicOperations).load(sequenceOf(parseLcaText(model, "main.lca")), listOf(LoaderOption.WITH_PRELUDE)),
        LcaacConfig(),
        emptyMap(),
    )

    /** Runs an operation on `vm_fn` with the given outputs, without a request. */
    private fun run(outputs: String): OperationResult {
        val operation = ApiDefinitionLoader.load(
            "api:\n  name: a\n  version: \"1\"\n  operations:\n    vm:\n      process: vm_fn\n      outputs:\n" +
                outputs.trimIndent().prependIndent("        ")
        )!!.definition.operations["vm"]!!
        return runner.run(operation, JsonObject(emptyMap()))
    }

    private fun success(outputs: String): JsonObject =
        rounded(assertIs<OperationResult.Success>(run(outputs)).body["outputs"]!!.jsonObject)

    private fun json(text: String): JsonElement = Json.parseToJsonElement(text)

    private fun rounded(json: JsonObject): JsonObject = Json.parseToJsonElement(
        Regex("\"amount\":(-?[0-9.Ee-]+)").replace(json.toString()) {
            "\"amount\":${Math.round(it.groupValues[1].toDouble() * 1e9) / 1e9}"
        }
    ).jsonObject

    @Test
    fun run_whenLabelIsNull_shouldSelectTheRowsWithoutIt() {
        val actual = success("""
            total: { select: [labels, GWP], where: { process: vm_fn, labels: { phase: null } } }
        """)

        assertEquals(json("""
            { "total": [ { "labels": {}, "GWP": { "amount": 3.5, "unit": "kg CO2-Eq" } } ] }
        """), actual)
    }

    @Test
    fun run_whenArgumentIsNull_shouldSelectTheRowsWithoutIt() {
        val actual = success("""
            use: { select: [labels], where: { process: vm_fn, labels: { phase: use }, arguments: { lc_step: null } } }
            none: { select: [labels], where: { process: vm_fn, labels: { phase: embodied }, arguments: { lc_step: null } } }
        """)

        assertEquals(json("""{ "use": [ { "labels": { "phase": "use" } } ], "none": [] }"""), actual)
    }

    @Test
    fun run_whenListOfValues_shouldSelectTheRowsWithAnyOfThem() {
        val actual = success("""
            steps: { select: [GWP], where: { process: vm_fn, labels: { phase: [embodied, use] } } }
        """)

        assertEquals(3, (actual["steps"] as JsonArray).size)
    }

    @Test
    fun run_whenOne_shouldGiveTheOnlyRow() {
        val actual = success("""
            total: { select: [GWP], where: { process: vm_fn, labels: { phase: null } }, aggregate: one }
        """)

        assertEquals(json("""{ "total": { "GWP": { "amount": 3.5, "unit": "kg CO2-Eq" } } }"""), actual)
    }

    @Test
    fun run_whenOneAndSeveralRows_shouldFailWith422() {
        assertEquals(
            OperationResult.Failure(422, ErrorBody("output 'total': expected exactly one matching row, found 4")),
            run("""
                total: { select: [GWP], where: { process: vm_fn }, aggregate: one }
            """),
        )
    }

    @Test
    fun run_whenSum_shouldAddTheRows() {
        val actual = success("""
            embodied: { select: [GWP], where: { process: vm_fn, labels: { phase: embodied } }, aggregate: sum }
            steps: { select: [amount, GWP], where: { product: vm, labels: { phase: [embodied, use] } }, aggregate: sum }
        """)

        assertEquals(json("""
            {
              "embodied": { "GWP": { "amount": 2.5, "unit": "kg CO2-Eq" } },
              "steps": { "amount": { "amount": 3.0, "unit": "hour" }, "GWP": { "amount": 3.5, "unit": "kg CO2-Eq" } }
            }
        """), actual)
    }

    @Test
    fun run_whenSumOfRowsUpstreamOfOthers_shouldFailWith422() {
        val actual = assertIs<OperationResult.Failure>(run("""
            total: { select: [GWP], where: { process: vm_fn }, aggregate: sum }
        """))

        assertEquals(422, actual.status)
        assertEquals(true, actual.body.error.startsWith("output 'total': the matching row 'vm from vm_fn{phase="))
        assertEquals(true, actual.body.error.endsWith("is upstream of another one, and would be counted twice"))
    }

    @Test
    fun run_whenSumOfNoRow_shouldBeZeroInTheUnitOfTheColumn() {
        val actual = success("""
            none: { select: [amount, GWP], where: { product: electricity, labels: { phase: use } }, aggregate: sum }
        """)

        assertEquals(json("""
            { "none": { "amount": { "amount": 0.0, "unit": "kWh" }, "GWP": { "amount": 0.0, "unit": "kg CO2-Eq" } } }
        """), actual)
    }

    @Test
    fun run_whenSumOfNoRowAndNoSuchColumn_shouldBeNull() {
        val actual = success("""
            none: { select: [amount, ADPe], where: { product: steel }, aggregate: sum }
        """)

        assertEquals(json("""{ "none": { "amount": null, "ADPe": null } }"""), actual)
    }
}
