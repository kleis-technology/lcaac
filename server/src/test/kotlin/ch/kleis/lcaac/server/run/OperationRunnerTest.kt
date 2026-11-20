package ch.kleis.lcaac.server.run

import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parseLcaText
import ch.kleis.lcaac.server.api.ApiDefinitionLoader
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class OperationRunnerTest {
    private val model = """
        datasource vm_inventory {
            location = "vm_inventory.csv"
            schema {
                id = "vm-001"
                vcpu = 2 u
                hours = 720 hour
            }
        }

        process vm_fleet {
            products {
                1 p fleet
            }
            inputs {
                for_each vm from vm_inventory {
                    vm.hours vm_hour from virtual_machine(id = vm.id, vcpu = vm.vcpu)
                }
            }
        }

        process virtual_machine {
            params {
                id = "vm-001"
                vcpu = 2 u
            }
            products {
                1 hour vm_hour
            }
            inputs {
                vcpu * 0.01 kWh electricity from grid(region = "FR")
            }
        }

        process grid {
            params {
                region = "FR"
            }
            products {
                1 kWh electricity
            }
            impacts {
                0.05 kg_CO2_Eq GWP
            }
        }
    """.trimIndent()

    private val api = ApiDefinitionLoader.load("""
        api:
          name: cloud-assess
          version: 2.1.0
          units:
            impacts: { GWP: kg_CO2_Eq }
            products: { vm_hour: hour, electricity: kWh }
          operations:
            vm_fleet:
              process: vm_fleet
              data:
                vms: { datasource: vm_inventory }
              outputs:
                total:
                  select: [GWP]
                  where: { product: fleet, process: vm_fleet }
                per_vm:
                  select: [arguments, amount, GWP]
                  where: { product: vm_hour, process: virtual_machine }
                electricity:
                  select: [product, process, labels, arguments, amount]
                  where: { product: electricity }
            virtual_machine:
              process: virtual_machine
              inputs:
                cpus: { parameter: vcpu }
              outputs:
                impacts:
                  select: [GWP]
                  where: { process: virtual_machine }
    """.trimIndent())!!.definition

    private val runner = OperationRunner(
        Loader(BasicOperations).load(sequenceOf(parseLcaText(model, "main.lca")), listOf(LoaderOption.WITH_PRELUDE)),
        LcaacConfig(),
        emptyMap(),
        api.units,
    )

    private fun run(operation: String, request: String): OperationResult =
        runner.run(api.operations[operation]!!, Json.parseToJsonElement(request).jsonObject)

    /** The outputs of a successful run, after checking that the response repeats the request. */
    private fun success(operation: String, request: String): JsonObject {
        val body = assertIs<OperationResult.Success>(run(operation, request)).body
        assertEquals(setOf("request", "outputs"), body.keys)
        assertEquals(json(request), body["request"])
        return body["outputs"]!!.jsonObject
    }

    private fun json(text: String) = Json.parseToJsonElement(text)

    @Test
    fun run_withRows_shouldSelectTheCellsOfTheTrace() {
        val actual = success("vm_fleet", """
            { "vms": [
                { "id": "vm-001", "vcpu": "4 u", "hours": "100 hour" },
                { "id": "vm-002", "vcpu": 2, "hours": { "amount": 300, "unit": "hour" } }
            ] }
        """)

        assertEquals(json("""
            {
              "total": [ { "GWP": { "amount": 0.5, "unit": "kg CO2-Eq" } } ],
              "per_vm": [
                { "arguments": { "id": "vm-001", "vcpu": "4.0 u" },
                  "amount": { "amount": 100.0, "unit": "hour" },
                  "GWP": { "amount": 0.2, "unit": "kg CO2-Eq" } },
                { "arguments": { "id": "vm-002", "vcpu": "2.0 u" },
                  "amount": { "amount": 300.0, "unit": "hour" },
                  "GWP": { "amount": 0.3, "unit": "kg CO2-Eq" } }
              ],
              "electricity": [
                { "product": "electricity", "process": "grid", "labels": {},
                  "arguments": { "region": "FR" }, "amount": { "amount": 10.0, "unit": "kWh" } }
              ]
            }
        """), rounded(actual))
    }

    @Test
    fun run_withParameter_shouldPassIt() {
        val actual = success("virtual_machine", """{ "cpus": "4 u" }""")

        assertEquals(json("""{ "impacts": [ { "GWP": { "amount": 0.002, "unit": "kg CO2-Eq" } } ] }"""), rounded(actual))
    }

    @Test
    fun run_withoutParameter_shouldKeepTheDefault() {
        val actual = success("virtual_machine", "{}")

        assertEquals(json("""{ "impacts": [ { "GWP": { "amount": 0.001, "unit": "kg CO2-Eq" } } ] }"""), rounded(actual))
    }

    @Test
    fun run_withUnknownField_shouldFailWith400() {
        assertEquals(
            OperationResult.Failure(400, ErrorBody("unknown field 'cpu'")),
            run("virtual_machine", """{ "cpu": 4 }"""),
        )
    }

    @Test
    fun run_withWrongDimension_shouldFailWith400() {
        assertEquals(
            OperationResult.Failure(400, ErrorBody(
                "incompatible dimensions: expected none, found mass",
                listOf(ErrorFrame("argument 'cpus' = 2 kg")),
            )),
            run("virtual_machine", """{ "cpus": "2 kg" }"""),
        )
    }

    @Test
    fun run_withWrongDimensionInARow_shouldNameTheRowAndColumn() {
        assertEquals(
            OperationResult.Failure(400, ErrorBody(
                "incompatible dimensions: expected time, found mass",
                listOf(ErrorFrame("argument 'vms[1].hours' = 2 kg")),
            )),
            run("vm_fleet", """{ "vms": [ { "id": "a" }, { "id": "b", "hours": "2 kg" } ] }"""),
        )
    }

    @Test
    fun run_withUnknownColumn_shouldFailWith400() {
        assertEquals(
            OperationResult.Failure(400, ErrorBody("field 'vms[0]': unknown column 'cpu'")),
            run("vm_fleet", """{ "vms": [ { "cpu": 4 } ] }"""),
        )
    }

    @Test
    fun run_withInvalidQuantity_shouldFailWith400() {
        assertEquals(
            OperationResult.Failure(400, ErrorBody("field 'cpus': expected a quantity")),
            run("virtual_machine", """{ "cpus": true }"""),
        )
    }

    @Test
    fun run_withoutRowsNorConnector_shouldFailWith422() {
        val actual = assertIs<OperationResult.Failure>(run("vm_fleet", "{}"))

        assertEquals(422, actual.status)
    }

    @Test
    fun run_withNoRows_shouldReturnEmptyLists() {
        val actual = success("vm_fleet", """{ "vms": [] }""")

        assertEquals(json("""[]"""), actual["per_vm"])
        assertEquals(json("""[]"""), actual["electricity"])
    }

    private fun rounded(json: JsonObject): JsonObject = Json.parseToJsonElement(
        Regex("\"amount\":(-?[0-9.Ee-]+)").replace(json.toString()) {
            "\"amount\":${Math.round(it.groupValues[1].toDouble() * 1e9) / 1e9}"
        }
    ).jsonObject
}
