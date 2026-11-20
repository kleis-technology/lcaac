package ch.kleis.lcaac.server.run

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.datasource.DataSourceOperationsWithConfig
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.expression.ERecord
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

/**
 * Inputs mapped to a global parameter, on the pattern of Cloud Assess: the period of the assessment is the
 * global `timewindow`, which is also the amount of the reference product.
 */
class GlobalTest {
    private val model = """
        params {
            timewindow = 1 hour
        }

        datasource grid_mix {
            location = "grid_mix.csv"
            schema {
                gwp = 0.05 kg_CO2_Eq
            }
        }

        process virtual_machine {
            params {
                vcpu = 2 u
            }
            variables {
                hours = timewindow / 1 hour
            }
            products {
                timewindow vm
            }
            inputs {
                hours * vcpu * 1 kWh electricity from grid()
            }
        }

        @cached
        process grid {
            variables {
                mix = lookup grid_mix
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
          name: a
          version: "1"
          units:
            impacts: { GWP: kg_CO2_Eq }
          operations:
            virtual_machine:
              process: virtual_machine
              inputs:
                period: { global: timewindow }
                vcpu: { parameter: vcpu }
              outputs:
                total: { select: [GWP], where: { process: virtual_machine }, aggregate: one }
    """.trimIndent())!!.definition

    /** A connector that counts the reads of the grid mix, without cache. */
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
            return ERecord(mapOf("gwp" to EQuantityScale(BasicNumber(0.05), Prelude.unitMap<BasicNumber>()["kg_CO2_Eq"]!!)))
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
        api.units,
    )

    private val operation = api.operations["virtual_machine"]!!

    private fun requests(vararg texts: String): List<JsonObject> = texts.map { Json.parseToJsonElement(it).jsonObject }

    private fun gwp(body: JsonObject): Double =
        body["outputs"]!!.jsonObject["total"]!!.jsonObject["GWP"]!!.jsonObject["amount"].toString().toDouble()

    @Test
    fun run_withGlobal_shouldAssessForItsValue() {
        val hour = gwp(assertIs<OperationResult.Success>(runner.run(operation, JsonObject(emptyMap()))).body)
        val month = gwp(assertIs<OperationResult.Success>(runner.run(operation, requests("""{ "period": "720 hour" }""").single())).body)

        assertEquals(0.1, hour, 1e-9)
        assertEquals(720 * 0.1, month, 1e-6)
    }

    @Test
    fun run_withGlobalOfTheWrongDimension_shouldFailWith400() {
        assertEquals(
            OperationResult.Failure(400, ErrorBody(
                "incompatible dimensions: expected time, found mass",
                listOf(ErrorFrame("argument 'period' = 2 kg")),
            )),
            runner.run(operation, requests("""{ "period": "2 kg" }""").single()),
        )
    }

    @Test
    fun runBatch_shouldShareTheCachedProcessesPerValueOfTheGlobals() {
        val actual = runner.runBatch(operation, requests(
            """{ "period": "720 hour", "vcpu": 1 }""",
            """{ "period": "720 hour", "vcpu": 2 }""",
            """{ "period": "24 hour" }""",
            """{ "vcpu": 4 }""",
        ))

        assertEquals(4, assertIs<OperationResult.Success>(actual).body["results"]!!.jsonArray.size)
        assertEquals(3, connector.reads)
    }
}
