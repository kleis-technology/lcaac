package ch.kleis.lcaac.server.run

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.datasource.DataSourceOperationsWithConfig
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.prelude.Prelude
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parseLcaText
import ch.kleis.lcaac.server.api.ApiDefinitionLoader
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BatchTest {
    private val model = """
        datasource grid_mix {
            location = "grid_mix.csv"
            schema {
                region = "FR"
                gwp = 0.05 kg_CO2_Eq
            }
        }

        process virtual_machine {
            params {
                vcpu = 2 u
            }
            products {
                1 hour vm_hour
            }
            inputs {
                vcpu * 0.01 kWh electricity
            }
        }

        process vm_fleet {
            products {
                1 p fleet
            }
            inputs {
                for_each vm from vms {
                    vm.hours vm_hour from virtual_machine(vcpu = vm.vcpu)
                }
            }
        }

        datasource vms {
            location = "vms.csv"
            schema {
                vcpu = 2 u
                hours = 1 hour
            }
        }

        @cached
        process grid {
            variables {
                mix = lookup grid_mix match region = "FR"
            }
            products {
                1 kWh electricity
            }
            impacts {
                mix.gwp GWP
            }
        }
    """.trimIndent()

    private val api = ApiDefinitionLoader.load("""
        api:
          name: cloud-assess
          version: 1.0.0
          operations:
            virtual_machine:
              process: virtual_machine
              inputs:
                vcpu: { parameter: vcpu }
              outputs:
                impacts: { select: [GWP], where: { process: virtual_machine } }
            vm_fleet:
              process: vm_fleet
              data:
                vms: { datasource: vms }
              outputs:
                total: { select: [GWP], where: { process: vm_fleet } }
    """.trimIndent())!!.definition

    /** A connector that counts the reads of the grid mixes, without cache. */
    private class CountingConnector : DataSourceConnector<BasicNumber> {
        var reads = 0

        override fun getName() = "csv"
        override fun getConfig() = ConnectorConfig("csv", emptyMap())

        override fun getFirst(
            caller: DataSourceOperationsWithConfig<BasicNumber>,
            config: DataSourceConfig,
            source: DataSourceValue<BasicNumber>,
        ): ERecord<BasicNumber> {
            reads += 1
            return ERecord(mapOf(
                "region" to EStringLiteral("FR"),
                "gwp" to EQuantityScale(BasicNumber(0.05), Prelude.unitMap<BasicNumber>()["kg_CO2_Eq"]!!),
            ))
        }

        override fun getAll(
            caller: DataSourceOperationsWithConfig<BasicNumber>,
            config: DataSourceConfig,
            source: DataSourceValue<BasicNumber>,
        ) = sequenceOf(getFirst(caller, config, source))
    }

    private val connector = CountingConnector()

    private val runner = OperationRunner(
        Loader(BasicOperations).load(sequenceOf(parseLcaText(model, "main.lca")), listOf(LoaderOption.WITH_PRELUDE)),
        LcaacConfig(),
        mapOf("csv" to connector),
    )

    private fun requests(vararg texts: String): List<JsonObject> = texts.map { Json.parseToJsonElement(it).jsonObject }

    @Test
    fun runBatch_shouldGiveTheResultsOfEachRequestInOrder() {
        val operation = api.operations["virtual_machine"]!!
        val batch = requests("""{"vcpu": 4}""", "{}", """{"vcpu": 8}""")

        val actual = assertIs<OperationResult.Success>(runner.runBatch(operation, batch)).body

        val singles = batch.map { assertIs<OperationResult.Success>(runner.run(operation, it)).body }
        assertEquals(singles, actual["results"]!!.jsonArray.map { it.jsonObject })
    }

    @Test
    fun runBatch_shouldShareTheCachedProcesses() {
        val operation = api.operations["virtual_machine"]!!

        runner.runBatch(operation, requests("""{"vcpu": 4}""", """{"vcpu": 6}""", """{"vcpu": 8}"""))

        assertEquals(1, connector.reads)
    }

    @Test
    fun run_shouldNotShareTheCachedProcessesBetweenCalls() {
        val operation = api.operations["virtual_machine"]!!

        runner.run(operation, requests("""{"vcpu": 4}""").single())
        runner.run(operation, requests("""{"vcpu": 6}""").single())

        assertEquals(2, connector.reads)
    }

    @Test
    fun runBatch_withRows_shouldGiveTheResultsOfEachRequest() {
        val operation = api.operations["vm_fleet"]!!
        val batch = requests(
            """{"vms": [{"vcpu": 4, "hours": 10}]}""",
            """{"vms": [{"vcpu": 4, "hours": 10}, {"vcpu": 2, "hours": 10}]}""",
        )

        val actual = assertIs<OperationResult.Success>(runner.runBatch(operation, batch)).body

        val singles = batch.map { assertIs<OperationResult.Success>(runner.run(operation, it)).body }
        assertEquals(singles, actual["results"]!!.jsonArray.map { it.jsonObject })
    }

    @Test
    fun runBatch_whenARequestFails_shouldFailAndNameIt() {
        val operation = api.operations["virtual_machine"]!!

        val actual = runner.runBatch(operation, requests("""{"vcpu": 4}""", """{"vcpu": "2 kg"}"""))

        assertEquals(
            OperationResult.Failure(400, ErrorBody(
                "incompatible dimensions: expected none, found mass",
                listOf(ErrorFrame("argument 'vcpu' = 2 kg"), ErrorFrame("request 'requests[1]'")),
            )),
            actual,
        )
    }

    @Test
    fun runBatch_whenEmpty_shouldGiveNoResults() {
        val actual = runner.runBatch(api.operations["virtual_machine"]!!, emptyList())

        assertEquals(OperationResult.Success(Json.parseToJsonElement("""{"results": []}""").jsonObject), actual)
    }
}
