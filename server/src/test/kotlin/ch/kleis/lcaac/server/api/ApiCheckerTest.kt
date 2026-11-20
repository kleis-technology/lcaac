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
        val located = ApiDefinitionLoader.load(api.trimIndent())
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
                "api.yaml:8:26: operations.vm_fleet.inputs.cpus.parameter: unknown parameter 'cpu_count' of process 'vm_fleet'",
                "api.yaml:10:26: operations.vm_fleet.data.vms.datasource: unknown data source 'vm_list'",
                "api.yaml:14:27: operations.vm_fleet.outputs.total.where.product: unknown product 'fleets'",
                "api.yaml:14:65: operations.vm_fleet.outputs.total.where.arguments: unknown string parameter 'vcpu'",
                "api.yaml:13:17: operations.vm_fleet.outputs.total.select: unknown column 'CO2'",
            ),
            check(api),
        )
    }

    @Test
    fun check_whenUnknownProcess_shouldStop() {
        val api = """
            api: { name: a, version: "1" }
            operations:
              fleet:
                process: fleet
                outputs:
                  total: { select: [GWP], where: { product: fleet } }
        """

        assertEquals(listOf("api.yaml:4:14: operations.fleet.process: unknown process 'fleet'"), check(api))
    }

    @Test
    fun check_whenLabelNeitherFixedNorGiven_shouldFail() {
        val api = """
            api: { name: a, version: "1" }
            operations:
              electricity:
                process: grid
                outputs:
                  total: { select: [GWP], where: { product: electricity } }
        """

        assertEquals(
            listOf(
                "api.yaml:4:5: operations.electricity.labels: process 'grid' with labels {mix=average} has labels [mix] that are neither fixed nor given by an input",
                "api.yaml:4:5: operations.electricity.labels: process 'grid' with labels {mix=marginal} has labels [mix] that are neither fixed nor given by an input",
            ),
            check(api),
        )
    }

    @Test
    fun check_whenFixedLabelsSelectNothing_shouldFail() {
        val api = """
            api: { name: a, version: "1" }
            operations:
              electricity:
                process: grid
                labels: { mix: residual }
                outputs:
                  total: { select: [GWP], where: { product: electricity } }
        """

        assertEquals(
            listOf("api.yaml:5:13: operations.electricity.labels: no process 'grid' has the labels {mix=residual}"),
            check(api),
        )
    }

    @Test
    fun check_whenInvalidInputsAndOutputs_shouldFail() {
        val api = """
            api: { name: a, version: "1" }
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
                "api.yaml:7:13: operations.electricity.inputs.both: an input maps to exactly one of 'parameter' or 'label'",
                "api.yaml:8:21: operations.electricity.inputs.mix.label: label 'mix' is already fixed",
                "api.yaml:10:43: operations.electricity.outputs.total.where: 'where' needs at least one condition",
                "api.yaml:10:24: operations.electricity.outputs.total.select: column 'GWP' is selected twice",
                "api.yaml:11:23: operations.electricity.outputs.none.select: 'select' needs at least one column",
            ),
            check(api),
        )
    }

    @Test
    fun labelValues_shouldListTheValuesOfTheProcesses() {
        val operation = Operation(process = "grid")

        assertEquals(listOf("average", "marginal"), ApiChecker(symbolTable).labelValues(operation, "mix"))
    }

    @Test
    fun load_whenUnknownField_shouldLocateIt() {
        val api = """
            api: { name: a, version: "1" }
            operations:
              electricity:
                process: grid
                output: {}
        """

        val actual = assertThrows<ApiDefinitionException> { ApiDefinitionLoader.load(api.trimIndent()) }
        assertEquals(1, actual.errors.size)
        assertEquals("api.yaml:5:5", actual.errors.single().location)
    }
}
