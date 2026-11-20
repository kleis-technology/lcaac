package ch.kleis.lcaac.server.http

import ch.kleis.lcaac.server.api.ApiDefinition
import ch.kleis.lcaac.server.run.ErrorBody
import ch.kleis.lcaac.server.run.OperationResult
import ch.kleis.lcaac.server.run.OperationRunner
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*

/**
 * The HTTP routes of an API: `GET /`, `GET /health`, `GET /openapi.json`, and for each operation,
 * `POST /{operation}` and `POST /{operation}/batch`, which runs at most [maxBatch] requests.
 */
fun Application.lcaacApi(
    definition: ApiDefinition,
    runner: OperationRunner,
    openApi: JsonObject,
    maxBatch: Int = DEFAULT_MAX_BATCH,
) {
    install(ContentNegotiation) {
        json(Json { encodeDefaults = false })
    }
    routing {
        get("/") {
            call.respond(buildJsonObject {
                put("name", definition.name)
                put("version", definition.version)
                put("description", definition.description)
                putJsonArray("operations") { definition.operations.keys.forEach { add(it) } }
            })
        }
        get("/health") {
            call.respond(buildJsonObject { put("status", "ok") })
        }
        get("/openapi.json") {
            call.respond(openApi)
        }
        post("/{operation}") {
            val name = call.parameters["operation"]!!
            val operation = definition.operations[name]
                ?: return@post call.respond(HttpStatusCode.NotFound, ErrorBody("unknown operation '$name'"))
            val request = try {
                val text = call.receiveText()
                if (text.isBlank()) JsonObject(emptyMap()) else Json.parseToJsonElement(text).jsonObject
            } catch (e: SerializationException) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("the request is not valid JSON"))
            } catch (e: IllegalArgumentException) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("the request is not a JSON object"))
            }
            respond(call, withContext(Dispatchers.Default) { runner.run(operation, request) })
        }
        post("/{operation}/batch") {
            val name = call.parameters["operation"]!!
            val operation = definition.operations[name]
                ?: return@post call.respond(HttpStatusCode.NotFound, ErrorBody("unknown operation '$name'"))
            val requests = try {
                val body = Json.parseToJsonElement(call.receiveText()).jsonObject
                if (body.keys != setOf("requests")) throw IllegalArgumentException()
                body["requests"]!!.jsonArray.map { it.jsonObject }
            } catch (e: SerializationException) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("the request is not valid JSON"))
            } catch (e: IllegalArgumentException) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorBody("expected an object with 'requests', a list of requests"),
                )
            }
            if (requests.size > maxBatch) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorBody("too many requests: ${requests.size}, at most $maxBatch in a batch"),
                )
            }
            respond(call, withContext(Dispatchers.Default) { runner.runBatch(operation, requests) })
        }
    }
}

const val DEFAULT_MAX_BATCH = 10_000

private suspend fun respond(call: ApplicationCall, result: OperationResult) = when (result) {
    is OperationResult.Success -> call.respond(result.body)
    is OperationResult.Failure -> call.respond(HttpStatusCode.fromValue(result.status), result.body)
}

/**
 * Serves an API until the process stops.
 */
fun serve(
    definition: ApiDefinition,
    runner: OperationRunner,
    openApi: JsonObject,
    host: String,
    port: Int,
    maxBatch: Int = DEFAULT_MAX_BATCH,
) {
    embeddedServer(Netty, host = host, port = port) {
        lcaacApi(definition, runner, openApi, maxBatch)
    }.start(wait = true)
}
