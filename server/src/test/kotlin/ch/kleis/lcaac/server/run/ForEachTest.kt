package ch.kleis.lcaac.server.run

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.datasource.DataSourceOperationsWithConfig
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorBuilder
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parseLcaText
import ch.kleis.lcaac.server.api.ApiDefinitionLoader
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Operations evaluated once per row, on the pattern of Cloud Assess: each virtual machine looks up its row by id,
 * and shares a cached process with the others.
 */
class ForEachTest {
    private val model = """
        datasource vms {
            location = "vms.csv"
            schema {
                id = "vm-1"
                vcpu = 2 u
                region = "FR"
            }
        }

        datasource grid_mix {
            location = "grid_mix.csv"
            schema {
                region = "FR"
                gwp = 0.05 kg_CO2_Eq
            }
        }

        process virtual_machine {
            params {
                id = "vm-1"
                factor = 1 u
            }
            variables {
                vm = lookup vms match id = id
            }
            products {
                1 hour vm_hour
            }
            inputs {
                factor * vm.vcpu * 1 kWh electricity from grid()
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
          name: a
          version: "1"
          units:
            impacts: { GWP: kg_CO2_Eq }
          operations:
            virtual_machines:
              process: virtual_machine
              inputs:
                factor: { parameter: factor }
              data:
                vms: { datasource: vms }
              for_each:
                rows: vms
                arguments: { id: id }
              outputs:
                total: { select: [GWP], where: { process: virtual_machine }, aggregate: one }
            renamed:
              process: virtual_machine
              pass_through: [meta]
              data:
                machines: { datasource: vms, columns: { name: id, cpus: vcpu }, pass_through: [meta, tags] }
              for_each:
                rows: machines
                arguments: { id: id }
              outputs:
                total: { select: [GWP], where: { process: virtual_machine }, aggregate: one }
    """.trimIndent())!!.definition

    @TempDir
    lateinit var directory: File

    /** Counts the reads of the grid mixes, read by a cached process. */
    private class CountingConnector(private val connector: DataSourceConnector<BasicNumber>) :
        DataSourceConnector<BasicNumber> by connector {
        var reads = 0

        override fun getFirst(
            caller: DataSourceOperationsWithConfig<BasicNumber>,
            config: DataSourceConfig,
            source: DataSourceValue<BasicNumber>,
        ) = connector.getFirst(caller, config, source).also { if (source.config.location == "grid_mix.csv") reads += 1 }
    }

    private lateinit var connector: CountingConnector
    private lateinit var runner: OperationRunner

    @BeforeTest
    fun setUp() {
        File(directory, "vms.csv").writeText("id,vcpu,region\nvm-1,2,FR\nvm-2,4,FR\n")
        File(directory, "grid_mix.csv").writeText("region,gwp\nFR,0.05\n")
        val config = LcaacConfig(connectors = listOf(ConnectorConfig("csv", mapOf("directory" to directory.path))))
        val symbolTable = Loader(BasicOperations).load(
            sequenceOf(parseLcaText(model, "main.lca")), listOf(LoaderOption.WITH_PRELUDE),
        )
        val csv = ConnectorFactory(directory.path, config, BasicOperations, symbolTable, listOf(CsvConnectorBuilder()))
            .buildConnectors().getValue("csv")
        connector = CountingConnector(csv)
        runner = OperationRunner(symbolTable, config, mapOf("csv" to connector), api.units)
    }

    private fun run(request: String, operation: String = "virtual_machines") =
        runner.run(api.operations[operation]!!, Json.parseToJsonElement(request).jsonObject)

    private fun json(text: String) = Json.parseToJsonElement(text)

    private fun rounded(json: JsonObject): JsonObject = Json.parseToJsonElement(
        Regex("\"amount\":(-?[0-9.Ee-]+)").replace(json.toString()) {
            "\"amount\":${Math.round(it.groupValues[1].toDouble() * 1e9) / 1e9}"
        }
    ).jsonObject

    @Test
    fun run_shouldGiveOneResultPerRow_withItsRow() {
        val actual = assertIs<OperationResult.Success>(run("""
            { "factor": 2, "vms": [ { "id": "a", "vcpu": 1 }, { "id": "b", "vcpu": "3 u" } ] }
        """)).body

        assertEquals(json("""
            {
              "request": { "factor": 2 },
              "results": [
                { "row": { "id": "a", "vcpu": 1 }, "outputs": { "total": { "GWP": { "amount": 0.1, "unit": "kg CO2-Eq" } } } },
                { "row": { "id": "b", "vcpu": "3 u" }, "outputs": { "total": { "GWP": { "amount": 0.3, "unit": "kg CO2-Eq" } } } }
              ]
            }
        """), rounded(actual))
    }

    @Test
    fun run_shouldShareTheCachedProcessesBetweenTheRows() {
        run("""{ "vms": [ { "id": "a" }, { "id": "b" }, { "id": "c" } ] }""")

        assertEquals(1, connector.reads)
    }

    @Test
    fun run_withoutRows_shouldUseTheRowsOfTheModel() {
        val actual = assertIs<OperationResult.Success>(run("{}")).body

        assertEquals(json("""
            {
              "request": {},
              "results": [
                { "row": { "id": "vm-1", "vcpu": { "amount": 2.0, "unit": "u" }, "region": "FR" },
                  "outputs": { "total": { "GWP": { "amount": 0.1, "unit": "kg CO2-Eq" } } } },
                { "row": { "id": "vm-2", "vcpu": { "amount": 4.0, "unit": "u" }, "region": "FR" },
                  "outputs": { "total": { "GWP": { "amount": 0.2, "unit": "kg CO2-Eq" } } } }
              ]
            }
        """), rounded(actual))
    }

    @Test
    fun run_withNoRow_shouldGiveNoResult() {
        val actual = assertIs<OperationResult.Success>(run("""{ "vms": [] }""")).body

        assertEquals(json("""{ "request": {}, "results": [] }"""), actual)
    }

    @Test
    fun run_whenARowFails_shouldFailAndNameIt() {
        val actual = assertIs<OperationResult.Failure>(run("""{ "vms": [ { "id": "a" }, { "id": "b", "vcpu": "2 kg" } ] }"""))

        assertEquals(400, actual.status)
        assertEquals("incompatible dimensions: expected none, found mass", actual.body.error)
    }

    @Test
    fun run_withColumns_shouldReadTheRowsUnderTheNamesOfTheRequest() {
        val actual = assertIs<OperationResult.Success>(run("""{ "machines": [ { "name": "a", "cpus": 3 } ] }""", "renamed")).body

        assertEquals(json("""
            {
              "request": {},
              "results": [
                { "row": { "name": "a", "cpus": 3 }, "outputs": { "total": { "GWP": { "amount": 0.15, "unit": "kg CO2-Eq" } } } }
              ]
            }
        """), rounded(actual))
    }

    @Test
    fun run_withColumns_shouldRefuseTheOtherColumns() {
        assertEquals(
            OperationResult.Failure(400, ErrorBody("field 'machines[0]': unknown column 'vcpu'")),
            run("""{ "machines": [ { "name": "a", "vcpu": 3 } ] }""", "renamed"),
        )
        assertEquals(
            OperationResult.Failure(400, ErrorBody("field 'machines[0]': unknown column 'region'")),
            run("""{ "machines": [ { "name": "a", "region": "DE" } ] }""", "renamed"),
        )
    }

    @Test
    fun run_withColumns_shouldNameTheFieldsOfTheRequestInErrors() {
        val actual = assertIs<OperationResult.Failure>(run("""{ "machines": [ { "name": "a", "cpus": "3 kg" } ] }""", "renamed"))

        assertEquals(listOf(ErrorFrame("argument 'machines[0].cpus' = 3 kg")), actual.body.frames)
    }

    @Test
    fun run_withColumnsWithoutRows_shouldGiveTheRowsOfTheModelUnderTheNamesOfTheRequest() {
        val actual = assertIs<OperationResult.Success>(run("{}", "renamed")).body

        assertEquals(
            json("""[ { "name": "vm-1", "cpus": { "amount": 2.0, "unit": "u" } }, { "name": "vm-2", "cpus": { "amount": 4.0, "unit": "u" } } ]"""),
            JsonArray(actual["results"]!!.jsonArray.map { it.jsonObject["row"]!! }),
        )
    }

    @Test
    fun run_withPassThrough_shouldRepeatTheFieldsWithoutReadingThem() {
        val actual = assertIs<OperationResult.Success>(run("""
            {
              "meta": { "request": "r-1" },
              "machines": [ { "name": "a", "cpus": 3, "meta": { "env": "production" }, "tags": ["x", 1] } ]
            }
        """, "renamed")).body

        assertEquals(json("""
            {
              "request": { "meta": { "request": "r-1" } },
              "results": [
                { "row": { "name": "a", "cpus": 3, "meta": { "env": "production" }, "tags": ["x", 1] },
                  "outputs": { "total": { "GWP": { "amount": 0.15, "unit": "kg CO2-Eq" } } } }
              ]
            }
        """), rounded(actual))
    }

    @Test
    fun run_withUndeclaredField_shouldFail() {
        assertEquals(
            OperationResult.Failure(400, ErrorBody("field 'machines[0]': unknown column 'metta'")),
            run("""{ "machines": [ { "name": "a", "metta": {} } ] }""", "renamed"),
        )
        assertEquals(
            OperationResult.Failure(400, ErrorBody("unknown field 'tags'")),
            run("""{ "tags": [] }""", "renamed"),
        )
    }

    @Test
    fun runBatch_shouldGiveTheResultsOfEachRequest() {
        val batch = listOf("""{ "vms": [ { "id": "a" } ] }""", """{ "factor": 3, "vms": [ { "id": "a" }, { "id": "b" } ] }""")
            .map { Json.parseToJsonElement(it).jsonObject }

        val actual = assertIs<OperationResult.Success>(runner.runBatch(api.operations["virtual_machines"]!!, batch)).body

        val singles = batch.map { assertIs<OperationResult.Success>(runner.run(api.operations["virtual_machines"]!!, it)).body }
        assertEquals(json("""{ "results": ${singles} }"""), actual)
    }
}
