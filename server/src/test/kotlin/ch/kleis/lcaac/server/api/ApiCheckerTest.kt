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

    /** The units of the model, appended to [api] unless it declares its own: the locations stay the same. */
    private val units = """
        |  units:
        |    impacts: { GWP: kg_CO2_Eq }
        |    products: { fleet: p, vm_hour: hour, electricity: kWh }
    """.trimMargin()

    private fun check(api: String): List<String> {
        val text = api.trimIndent().let { if ("  units:" in it) it else it + "\n" + units }
        val located = ApiDefinitionLoader.load(text)!!
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
              units:
                products: { vm: hour }
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
    fun check_whenConditionsAndAggregates_shouldAcceptThem() {
        val api = """
            api:
              name: a
              version: "1"
              operations:
                vm_fleet:
                  process: vm_fleet
                  outputs:
                    total:
                      select: [labels, GWP]
                      where: { product: fleet, labels: { mix: null } }
                      aggregate: one
                    electricity:
                      select: [amount, GWP]
                      where: { product: electricity, labels: { mix: [average, marginal] }, arguments: { region: null } }
                      aggregate: sum
        """

        assertEquals(emptyList(), check(api))
    }

    @Test
    fun check_whenInvalidConditionsAndAggregates_shouldFail() {
        val api = """
            api:
              name: a
              version: "1"
              operations:
                vm_fleet:
                  process: vm_fleet
                  outputs:
                    total:
                      select: [process, amount, GWP]
                      where: { labels: { mix: [] } }
                      aggregate: sum
        """

        assertEquals(
            listOf(
                "lcaac.yaml:10:35: api.operations.vm_fleet.outputs.total.where.labels.mix: a condition needs at least one value, or null for an absent value",
                "lcaac.yaml:9:19: api.operations.vm_fleet.outputs.total.select: column 'process' cannot be summed",
                "lcaac.yaml:9:19: api.operations.vm_fleet.outputs.total.select: column 'amount' is the amount of one product: set 'where.product'",
            ),
            check(api),
        )
    }

    @Test
    fun check_whenUnitsAreMissingOrUnknown_shouldFail() {
        val api = """
            api:
              name: a
              version: "1"
              units:
                impacts: { CO2: kg, GWP: kg_CO2 }
                products: { vm_hour: hour }
              operations:
                vm_fleet:
                  process: vm_fleet
                  outputs:
                    per_vm: { select: [amount, GWP], where: { product: vm_hour } }
                    electricity: { select: [amount], where: { product: electricity } }
                    all: { select: [amount], where: { process: grid } }
        """

        assertEquals(
            listOf(
                "lcaac.yaml:5:21: api.units.impacts.CO2: unknown indicator or substance 'CO2'",
                "lcaac.yaml:5:30: api.units.impacts.GWP: unknown unit 'kg_CO2'",
                "lcaac.yaml:12:32: api.operations.vm_fleet.outputs.electricity.select: no unit for the amounts of 'electricity': declare it under 'api.units.products'",
                "lcaac.yaml:13:24: api.operations.vm_fleet.outputs.all.select: column 'amount' is the amount of one product: set 'where.product'",
            ),
            check(api),
        )
    }

    @Test
    fun check_whenForEach_shouldAcceptIt() {
        val api = """
            api:
              name: a
              version: "1"
              operations:
                virtual_machines:
                  process: virtual_machine
                  data:
                    vms: { datasource: vm_inventory }
                  for_each: { rows: vms, arguments: { id: id, vcpu: vcpu } }
                  outputs:
                    total: { select: [GWP], where: { process: virtual_machine }, aggregate: one }
        """

        assertEquals(emptyList(), check(api))
    }

    @Test
    fun check_whenInvalidForEach_shouldFail() {
        val api = """
            api:
              name: a
              version: "1"
              operations:
                virtual_machines:
                  process: virtual_machine
                  inputs:
                    cpus: { parameter: vcpu }
                  data:
                    vms: { datasource: vm_inventory }
                  for_each: { rows: vms, arguments: { id: vcpu, vcpu: vcpu, region: id, name: label } }
                  outputs:
                    total: { select: [GWP], where: { process: virtual_machine }, aggregate: one }
                others:
                  process: virtual_machine
                  for_each: { rows: vms }
                  outputs:
                    total: { select: [GWP], where: { process: virtual_machine }, aggregate: one }
        """

        assertEquals(
            listOf(
                "lcaac.yaml:11:47: api.operations.virtual_machines.for_each.arguments.id: column 'vcpu' and parameter 'id' are not of the same type",
                "lcaac.yaml:11:59: api.operations.virtual_machines.for_each.arguments.vcpu: parameter 'vcpu' is already an input",
                "lcaac.yaml:11:73: api.operations.virtual_machines.for_each.arguments.region: unknown parameter 'region' of process 'virtual_machine'",
                "lcaac.yaml:11:83: api.operations.virtual_machines.for_each.arguments.name: unknown parameter 'name' of process 'virtual_machine'",
                "lcaac.yaml:11:83: api.operations.virtual_machines.for_each.arguments.name: unknown column 'label' of 'vms'",
                "lcaac.yaml:16:25: api.operations.others.for_each.rows: 'vms' is not a field of 'data'",
                "lcaac.yaml:16:17: api.operations.others.for_each.arguments: 'for_each' needs at least one argument, from a column of the rows",
            ),
            check(api),
        )
    }

    @Test
    fun load_whenUnknownAggregate_shouldFail() {
        val api = """
            api:
              name: a
              version: "1"
              operations:
                vm_fleet:
                  process: vm_fleet
                  outputs:
                    total: { select: [GWP], where: { product: fleet }, aggregate: max }
        """

        assertThrows<ApiDefinitionException> { check(api) }
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
