package ch.kleis.lcaac.server.http

import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parseLcaText
import ch.kleis.lcaac.server.api.ApiDefinitionLoader
import ch.kleis.lcaac.server.api.OpenApiGenerator
import ch.kleis.lcaac.server.run.OperationRunner
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiServerTest {
    private val model = """
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
        process grid {
            products {
                1 kWh electricity
            }
            impacts {
                2 kg_CO2_Eq GWP
            }
        }
        process broken {
            products {
                1 p broken
            }
            impacts {
                1 kg + 1 kWh GWP
            }
        }
    """.trimIndent()

    private val definition = ApiDefinitionLoader.load("""
        api:
          name: cloud-assess
          version: 2.1.0
          operations:
            virtual_machine:
              process: virtual_machine
              inputs:
                cpus: { parameter: vcpu }
              outputs:
                impacts: { select: [GWP], where: { process: virtual_machine } }
            broken:
              process: broken
              outputs:
                impacts: { select: [GWP], where: { process: broken } }
    """.trimIndent())!!.definition

    private val symbolTable = Loader(BasicOperations).load(sequenceOf(parseLcaText(model, "main.lca")), listOf(LoaderOption.WITH_PRELUDE))

    private val runner = OperationRunner(symbolTable, LcaacConfig(), emptyMap())

    private fun test(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        application { lcaacApi(definition, runner, OpenApiGenerator(symbolTable).generate(definition), maxBatch = 3) }
        block()
    }

    private fun json(text: String) = Json.parseToJsonElement(text)

    @Test
    fun getRoot_shouldDescribeTheApi() = test {
        val response = client.get("/")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            json("""{"name":"cloud-assess","version":"2.1.0","description":"","operations":["virtual_machine","broken"]}"""),
            json(response.bodyAsText()),
        )
    }

    @Test
    fun getHealth_shouldBeOk() = test {
        assertEquals(HttpStatusCode.OK, client.get("/health").status)
    }

    @Test
    fun getOpenApi_shouldServeTheDocument() = test {
        val response = client.get("/openapi.json")

        assertEquals(HttpStatusCode.OK, response.status)
        assert(response.bodyAsText().contains("\"operationId\":\"virtual_machine\"")) { response.bodyAsText() }
    }

    @Test
    fun post_shouldRunTheOperation() = test {
        val response = client.post("/virtual_machine") {
            contentType(ContentType.Application.Json)
            setBody("""{"cpus": 4}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(json("""{"impacts":[{"GWP":{"amount":0.08,"unit":"kg CO2-Eq"}}]}"""), json(response.bodyAsText()))
    }

    @Test
    fun post_withoutBody_shouldUseTheDefaults() = test {
        val response = client.post("/virtual_machine")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(json("""{"impacts":[{"GWP":{"amount":0.04,"unit":"kg CO2-Eq"}}]}"""), json(response.bodyAsText()))
    }

    @Test
    fun postBatch_shouldRunEachRequest() = test {
        val response = client.post("/virtual_machine/batch") {
            contentType(ContentType.Application.Json)
            setBody("""{"requests": [{"cpus": 4}, {}]}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            json("""{"results":[
                {"impacts":[{"GWP":{"amount":0.08,"unit":"kg CO2-Eq"}}]},
                {"impacts":[{"GWP":{"amount":0.04,"unit":"kg CO2-Eq"}}]}
            ]}"""),
            json(response.bodyAsText()),
        )
    }

    @Test
    fun postBatch_tooLarge_shouldBe400() = test {
        val response = client.post("/virtual_machine/batch") { setBody("""{"requests": [{}, {}, {}, {}]}""") }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(json("""{"error":"too many requests: 4, at most 3 in a batch"}"""), json(response.bodyAsText()))
    }

    @Test
    fun postBatch_withoutRequests_shouldBe400() = test {
        val response = client.post("/virtual_machine/batch") { setBody("""[{}]""") }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(
            json("""{"error":"expected an object with 'requests', a list of requests"}"""),
            json(response.bodyAsText()),
        )
    }

    @Test
    fun postBatch_whenARequestFails_shouldBe422() = test {
        val response = client.post("/broken/batch") { setBody("""{"requests": [{}]}""") }

        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assert(response.bodyAsText().contains("request 'requests[0]'")) { response.bodyAsText() }
    }

    @Test
    fun post_unknownOperation_shouldBe404() = test {
        val response = client.post("/vm")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(json("""{"error":"unknown operation 'vm'"}"""), json(response.bodyAsText()))
    }

    @Test
    fun post_invalidJson_shouldBe400() = test {
        val response = client.post("/virtual_machine") { setBody("""{"cpus": """) }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun post_whenTheEvaluationFails_shouldBe422WithLocations() = test {
        val response = client.post("/broken")

        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        val body = response.bodyAsText()
        assert(body.contains("incompatible dimensions")) { body }
        assert(body.contains("main.lca:")) { body }
    }
}
