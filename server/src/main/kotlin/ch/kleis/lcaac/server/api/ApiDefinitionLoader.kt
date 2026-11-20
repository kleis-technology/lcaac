package ch.kleis.lcaac.server.api

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlException
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode

data class ApiDefinitionError(
    val path: String,
    val message: String,
    val location: String? = null,
) {
    override fun toString(): String = listOfNotNull(location, path.ifEmpty { null }, message).joinToString(": ")
}

class ApiDefinitionException(val errors: List<ApiDefinitionError>) : Exception(errors.joinToString("\n"))

/**
 * An API definition, with the locations of its fields in [sourceName], to locate errors.
 */
class LocatedApiDefinition(
    val definition: ApiDefinition,
    val sourceName: String,
    private val root: YamlNode,
) {
    /**
     * The location of the field at [path] in the definition (e.g. `operations.vm_fleet.process`), or of its
     * closest enclosing field when it is absent.
     */
    fun locationOf(path: List<String>): String {
        var node = root
        for (key in path) {
            val map = node as? YamlMap ?: break
            node = map.entries.entries.firstOrNull { it.key.content == key }?.value ?: break
        }
        return "$sourceName:${node.location.line}:${node.location.column}"
    }

    fun error(path: List<String>, message: String) =
        ApiDefinitionError((listOf(API_KEY) + path).joinToString("."), message, locationOf(path))
}

const val API_KEY = "api"

object ApiDefinitionLoader {
    // The rest of the configuration is read leniently, the API strictly: a misspelled field is an error.
    // Anchors and aliases share parts of the API, e.g. lists of indicators: the file is the user's own
    private val yaml = Yaml(configuration = YamlConfiguration(strictMode = true, allowAnchorsAndAliases = true))

    /**
     * The API declared under the key `api` of a configuration file (e.g. `lcaac.yaml`), or null if there is none.
     */
    fun load(configuration: String, sourceName: String = "lcaac.yaml"): LocatedApiDefinition? {
        try {
            val document = yaml.parseToYamlNode(configuration) as? YamlMap ?: return null
            val root = document.entries.entries.firstOrNull { it.key.content == API_KEY }?.value ?: return null
            val definition = yaml.decodeFromYamlNode(ApiDefinition.serializer(), root)
            return LocatedApiDefinition(definition, sourceName, root)
        } catch (e: YamlException) {
            throw ApiDefinitionException(listOf(
                ApiDefinitionError(e.path.toHumanReadableString(), e.message, "$sourceName:${e.line}:${e.column}")
            ))
        }
    }
}
