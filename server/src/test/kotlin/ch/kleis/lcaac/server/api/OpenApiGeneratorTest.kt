package ch.kleis.lcaac.server.api

import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parseLcaText
import io.swagger.v3.parser.OpenAPIV3Parser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OpenApiGeneratorTest {
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
            labels {
                model = "standard"
            }
            params {
                id = "vm-001"
                vcpu = 2 u
                lifetime = 3 year
            }
            products {
                1 hour vm_hour
            }
            impacts {
                0.05 kg_CO2_Eq GWP
            }
        }

        process virtual_machine {
            labels {
                model = "detailed"
            }
            params {
                id = "vm-001"
                vcpu = 2 u
                lifetime = 3 year
            }
            products {
                1 hour vm_hour
            }
            impacts {
                0.06 kg_CO2_Eq GWP
            }
        }
    """.trimIndent()

    private val definition = ApiDefinitionLoader.load("""
        api:
          name: cloud-assess
          version: 2.1.0
          description: Impacts of cloud resources
          units:
            impacts: { GWP: kg_CO2_Eq }
            products: { vm_hour: hour }
          operations:
            vm_fleet:
              description: Impacts of a fleet of virtual machines
              process: vm_fleet
              data:
                vms: { datasource: vm_inventory }
              outputs:
                total: { select: [GWP], where: { product: fleet }, aggregate: one }
                per_vm: { select: [arguments, amount, GWP], where: { product: vm_hour } }
            virtual_machines:
              process: virtual_machine
              labels: { model: standard }
              pass_through: [meta]
              data:
                vms: { datasource: vm_inventory, columns: { name: id, cpus: vcpu }, pass_through: [meta] }
              for_each: { rows: vms, arguments: { id: id } }
              outputs:
                total: { select: [GWP], where: { product: vm_hour }, aggregate: one }
            virtual_machine:
              process: virtual_machine
              inputs:
                model: { label: model, description: The model of a virtual machine }
                lifetime: { parameter: lifetime }
              outputs:
                impacts: { select: [product, process, labels, GWP], where: { product: vm_hour } }
    """.trimIndent())!!.definition

    private val document: JsonObject = OpenApiGenerator(
        Loader(BasicOperations).load(sequenceOf(parseLcaText(model, "main.lca")), listOf(LoaderOption.WITH_PRELUDE))
    ).generate(definition)

    private fun schema(name: String) = document["components"]!!.jsonObject["schemas"]!!.jsonObject[name]

    private fun json(text: String) = Json.parseToJsonElement(text)

    @Test
    fun generate_shouldBeValidOpenApi() {
        val result = OpenAPIV3Parser().readContents(document.toString())

        assertEquals(emptyList(), result.messages)
        assertNotNull(result.openAPI)
        assertEquals(
            setOf("/vm_fleet", "/vm_fleet/batch", "/virtual_machines", "/virtual_machines/batch", "/virtual_machine", "/virtual_machine/batch"),
            result.openAPI.paths.keys,
        )
    }

    @Test
    fun generate_shouldDescribeTheRowsOfADataSource() {
        assertEquals(json("""
            {
              "type": "object",
              "additionalProperties": false,
              "properties": {
                "id": { "type": "string" },
                "vcpu": {
                  "allOf": [ { "${'$'}ref": "#/components/schemas/QuantityInput" } ],
                  "description": "Quantity of dimension none. Default unit u."
                },
                "hours": {
                  "allOf": [ { "${'$'}ref": "#/components/schemas/QuantityInput" } ],
                  "description": "Quantity of dimension time. Default unit hour."
                }
              }
            }
        """), schema("VmFleetVmsRow"))
    }

    @Test
    fun generate_shouldListTheValuesOfALabel() {
        assertEquals(json("""
            {
              "type": "object",
              "additionalProperties": false,
              "properties": {
                "model": { "type": "string", "enum": ["detailed", "standard"], "description": "The model of a virtual machine" },
                "lifetime": {
                  "allOf": [ { "${'$'}ref": "#/components/schemas/QuantityInput" } ],
                  "description": "Quantity of dimension time. Default unit year."
                }
              }
            }
        """), schema("VirtualMachineRequest"))
    }

    @Test
    fun generate_shouldDescribeTheSelectedColumns() {
        assertEquals(json("""
            {
              "type": "object",
              "required": ["request", "outputs"],
              "properties": {
                "request": {
                  "allOf": [ { "${'$'}ref": "#/components/schemas/VirtualMachineRequest" } ],
                  "description": "The request, as sent."
                },
                "outputs": {
                  "type": "object",
                  "required": ["impacts"],
                  "properties": {
                    "impacts": {
                      "type": "array",
                      "description": "One element per matching row of the trace.",
                      "items": {
                        "type": "object",
                        "required": ["product", "process", "labels", "GWP"],
                        "properties": {
                          "product": { "type": "string" },
                          "process": { "type": "string" },
                          "labels": { "${'$'}ref": "#/components/schemas/Strings" },
                          "GWP": {
                            "type": "object",
                            "required": ["amount", "unit"],
                            "properties": {
                              "amount": { "type": "number" },
                              "unit": { "type": "string", "enum": ["kg CO2-Eq"] }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
        """), schema("VirtualMachineResponse"))
    }

    @Test
    fun generate_whenAggregated_shouldDescribeOneElement() {
        assertEquals(json("""
            {
              "type": "object",
              "required": ["GWP"],
              "properties": {
                "GWP": {
                            "type": "object",
                            "required": ["amount", "unit"],
                            "properties": {
                              "amount": { "type": "number" },
                              "unit": { "type": "string", "enum": ["kg CO2-Eq"] }
                            }
                          }
              },
              "description": "The only matching row of the trace."
            }
        """), schema("VmFleetResponse")!!.jsonObject["properties"]!!.jsonObject["outputs"]!!
            .jsonObject["properties"]!!.jsonObject["total"])
    }

    @Test
    fun generate_whenForEach_shouldGiveOneResultPerRow() {
        val results = schema("VirtualMachinesResponse")!!.jsonObject["properties"]!!.jsonObject["results"]!!.jsonObject

        assertEquals(json("""{ "${'$'}ref": "#/components/schemas/VirtualMachinesVmsRow" }"""),
            results["items"]!!.jsonObject["properties"]!!.jsonObject["row"]!!.jsonObject["allOf"]!!.jsonArray.single())
        assertEquals(json("""["row", "outputs"]"""), results["items"]!!.jsonObject["required"])
    }

    @Test
    fun generate_withColumns_shouldDescribeTheRowsUnderTheNamesOfTheRequest() {
        assertEquals(
            setOf("name", "cpus", "meta"),
            schema("VirtualMachinesVmsRow")!!.jsonObject["properties"]!!.jsonObject.keys,
        )
        assertEquals(
            json("""{ "description": "Not read by the server: repeated in the response." }"""),
            schema("VirtualMachinesRequest")!!.jsonObject["properties"]!!.jsonObject["meta"],
        )
    }

    @Test
    fun generate_shouldNotRevealTheMapping() {
        val text = document.toString()

        listOf("vm_inventory", "x-lcaac", "\"where\"", "\"parameter\"", "\"datasource\"").forEach {
            assert(!text.contains(it)) { "$it in $text" }
        }
    }
}
