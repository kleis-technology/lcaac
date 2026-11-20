package ch.kleis.lcaac.server.http

import ch.kleis.lcaac.server.api.ApiDefinition
import ch.kleis.lcaac.server.run.ErrorBody
import ch.kleis.lcaac.server.run.OperationResult
import ch.kleis.lcaac.server.run.OperationRunner
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
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
 * The HTTP routes of an API: `GET /`, `GET /health`, `GET /openapi.json`, and `POST /{operation}` for each operation.
 */
fun Application.lcaacApi(definition: ApiDefinition, runner: OperationRunner, openApi: JsonObject) {
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
            when (val result = withContext(Dispatchers.Default) { runner.run(operation, request) }) {
                is OperationResult.Success -> call.respond(result.body)
                is OperationResult.Failure -> call.respond(HttpStatusCode.fromValue(result.status), result.body)
            }
        }
    }
}

/**
 * Serves an API until the process stops.
 */
fun serve(definition: ApiDefinition, runner: OperationRunner, openApi: JsonObject, host: String, port: Int) {
    embeddedServer(CIO, host = host, port = port) {
        lcaacApi(definition, runner, openApi)
    }.start(wait = true)
}
