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
     * The location of the field at [path] (e.g. `operations.vm_fleet.process`), or of its closest
     * enclosing field when it is absent.
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
        ApiDefinitionError(path.joinToString("."), message, locationOf(path))
}

object ApiDefinitionLoader {
    private val yaml = Yaml(configuration = YamlConfiguration(strictMode = true))

    fun load(content: String, sourceName: String = "api.yaml"): LocatedApiDefinition {
        try {
            val root = yaml.parseToYamlNode(content)
            val definition = yaml.decodeFromYamlNode(ApiDefinition.serializer(), root)
            return LocatedApiDefinition(definition, sourceName, root)
        } catch (e: YamlException) {
            throw ApiDefinitionException(listOf(
                ApiDefinitionError(e.path.toHumanReadableString(), e.message, "$sourceName:${e.line}:${e.column}")
            ))
        }
    }
}
