package ch.kleis.lcaac.server.api

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parseLcaText
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.assertThrows

class ApiCheckerTest {
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
            labels {
                mix = "average"
            }
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

        process grid {
            labels {
                mix = "marginal"
            }
            params {
                region = "FR"
            }
            products {
                1 kWh electricity
            }
            impacts {
                0.08 kg_CO2_Eq GWP
            }
        }
    """.trimIndent()

    private val symbolTable: SymbolTable<BasicNumber> =
        Loader(BasicOperations).load(sequenceOf(parseLcaText(model, "main.lca")), listOf(LoaderOption.WITH_PRELUDE))

    private fun check(api: String): List<String> {
        val located = ApiDefinitionLoader.load(api.trimIndent())!!
        return ApiChecker(symbolTable).check(located).map { it.toString() }
    }

    @Test
    fun check_whenValid_shouldFindNoError() {
        val api = """
            api:
              name: cloud-assess
              version: 2.1.0
              operations:
                vm_fleet:
                  description: Impacts of a fleet of virtual machines
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
                  process: grid
                  inputs:
                    mix: { label: mix }
                    region: { parameter: region }
                  outputs:
                    total:
                      select: [GWP]
                      where: { product: electricity, arguments: { region: FR } }
        """

        assertEquals(emptyList(), check(api))
    }

    @Test
    fun check_whenUnknownNames_shouldLocateEachOne() {
        val api = """
            api:
              name: cloud-assess
              version: 2.1.0
              operations:
                vm_fleet:
                  process: vm_fleet
                  inputs:
                    cpus: { parameter: cpu_count }
                  data:
                    vms: { datasource: vm_list }
                  outputs:
                    total:
                      select: [GWP, CO2]
                      where: { product: fleets, process: vm_fleet, arguments: { vcpu: "2 u" } }
        """

        assertEquals(
            listOf(
                "lcaac.yaml:8:28: api.operations.vm_fleet.inputs.cpus.parameter: unknown parameter 'cpu_count' of process 'vm_fleet'",
                "lcaac.yaml:10:28: api.operations.vm_fleet.data.vms.datasource: unknown data source 'vm_list'",
                "lcaac.yaml:14:29: api.operations.vm_fleet.outputs.total.where.product: unknown product 'fleets'",
                "lcaac.yaml:14:67: api.operations.vm_fleet.outputs.total.where.arguments: unknown string parameter 'vcpu'",
                "lcaac.yaml:13:19: api.operations.vm_fleet.outputs.total.select: unknown column 'CO2'",
            ),
            check(api),
        )
    }

    @Test
    fun check_whenUnknownProcess_shouldStop() {
        val api = """
            api:
              name: a
              version: "1"
              operations:
                fleet:
                  process: fleet
                  outputs:
                    total: { select: [GWP], where: { product: fleet } }
        """

        assertEquals(listOf("lcaac.yaml:6:16: api.operations.fleet.process: unknown process 'fleet'"), check(api))
    }

    @Test
    fun check_whenLabelNeitherFixedNorGiven_shouldFail() {
        val api = """
            api:
              name: a
              version: "1"
              operations:
                electricity:
                  process: grid
                  outputs:
                    total: { select: [GWP], where: { product: electricity } }
        """

        assertEquals(
            listOf("lcaac.yaml:6:7: api.operations.electricity.labels: no process 'grid' has exactly the labels {}"),
            check(api),
        )
    }

    @Test
    fun check_whenAnUnlabeledProcessHasLabeledVariants_shouldSelectIt() {
        val st = Loader(BasicOperations).load(sequenceOf(parseLcaText("""
            process vm {
                products {
                    1 hour vm
                }
                inputs {
                    1 hour vm from vm() match (phase = "use")
                }
            }
            process vm {
                labels {
                    phase = "use"
                }
                products {
                    1 hour vm
                }
            }
        """.trimIndent(), "main.lca")), listOf(LoaderOption.WITH_PRELUDE))
        val api = """
            api:
              name: a
              version: "1"
              operations:
                vm:
                  process: vm
                  outputs:
                    total: { select: [amount], where: { product: vm } }
        """

        assertEquals(emptyList(), ApiChecker(st).check(ApiDefinitionLoader.load(api.trimIndent())!!).map { it.toString() })
    }

    @Test
    fun check_whenFixedLabelsSelectNothing_shouldFail() {
        val api = """
            api:
              name: a
              version: "1"
              operations:
                electricity:
                  process: grid
                  labels: { mix: residual }
                  outputs:
                    total: { select: [GWP], where: { product: electricity } }
        """

        assertEquals(
            listOf("lcaac.yaml:7:15: api.operations.electricity.labels: no process 'grid' has exactly the labels {mix=residual}"),
            check(api),
        )
    }

    @Test
    fun check_whenInvalidInputsAndOutputs_shouldFail() {
        val api = """
            api:
              name: a
              version: "1"
              operations:
                electricity:
                  process: grid
                  labels: { mix: average }
                  inputs:
                    both: { parameter: region, label: mix }
                    mix: { label: mix }
                  outputs:
                    total: { select: [GWP, GWP], where: {} }
                    none: { select: [], where: { product: electricity } }
        """

        assertEquals(
            listOf(
                "lcaac.yaml:9:15: api.operations.electricity.inputs.both: an input maps to exactly one of 'parameter' or 'label'",
                "lcaac.yaml:10:23: api.operations.electricity.inputs.mix.label: label 'mix' is already fixed",
                "lcaac.yaml:12:45: api.operations.electricity.outputs.total.where: 'where' needs at least one condition",
                "lcaac.yaml:12:26: api.operations.electricity.outputs.total.select: column 'GWP' is selected twice",
                "lcaac.yaml:13:25: api.operations.electricity.outputs.none.select: 'select' needs at least one column",
            ),
            check(api),
        )
    }

    @Test
    fun labelValues_shouldListTheValuesOfTheProcesses() {
        val operation = Operation(process = "grid", inputs = mapOf("mix" to Input(label = "mix")))

        assertEquals(listOf("average", "marginal"), ApiChecker(symbolTable).labelValues(operation, "mix"))
    }

    @Test
    fun load_withoutApi_shouldBeNull() {
        assertEquals(null, ApiDefinitionLoader.load("name: model\nconnectors: []\n"))
    }

    @Test
    fun load_shouldIgnoreTheRestOfTheConfiguration() {
        val config = """
            name: model
            unknown: { anything: 1 }
            api:
              name: a
              version: "1"
        """

        assertEquals(ApiDefinition(name = "a", version = "1"), ApiDefinitionLoader.load(config.trimIndent())!!.definition)
    }

    @Test
    fun load_whenUnknownField_shouldLocateIt() {
        val api = """
            api:
              name: a
              version: "1"
              operations:
                electricity:
                  process: grid
                  output: {}
        """

        val actual = assertThrows<ApiDefinitionException> { ApiDefinitionLoader.load(api.trimIndent()) }
        assertEquals(1, actual.errors.size)
        assertEquals("lcaac.yaml:7:7", actual.errors.single().location)
    }
}
