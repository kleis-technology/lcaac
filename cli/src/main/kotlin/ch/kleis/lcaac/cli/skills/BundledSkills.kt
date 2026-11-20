package ch.kleis.lcaac.cli.skills

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlScalar
import java.io.File

/**
 * An agent skill (https://agentskills.io/specification): a directory [name] with a `SKILL.md` file, whose
 * frontmatter gives its [name] and [description], and other [files], relative to the directory.
 */
data class Skill(val name: String, val description: String, val files: List<String>)

/**
 * The agent skills bundled with the CLI, read from its resources (`skills/`, with an index of their files).
 */
class BundledSkills(private val loader: ClassLoader = BundledSkills::class.java.classLoader) {
    private val paths: List<String> by lazy {
        resource("index.txt").lines().filter { it.isNotBlank() }
    }

    val skills: List<Skill> by lazy {
        paths.groupBy { it.substringBefore('/') }.map { (name, files) ->
            val frontmatter = frontmatter(read("$name/$SKILL_FILE"))
            Skill(
                frontmatter["name"] ?: name,
                frontmatter["description"] ?: "",
                files.map { it.substringAfter('/') }.sorted(),
            )
        }.sortedBy { it.name }
    }

    /** The content of [file] of the skill [skill]. */
    fun read(skill: String, file: String): String = read("$skill/$file")

    private fun read(path: String): String = resource(path)

    private fun resource(path: String): String =
        loader.getResourceAsStream("skills/$path")?.use { it.readBytes().toString(Charsets.UTF_8) }
            ?: throw IllegalStateException("missing resource skills/$path")

    /**
     * Installs the skills into [directory], each in a directory of its name, with `lcaac-version` set to [version].
     * Replaces the skills installed by lcaac before (upgrades), but not a directory of the same name that is not one.
     */
    fun install(directory: File, version: String): List<Skill> {
        skills.forEach { skill ->
            val target = File(directory, skill.name)
            if (target.exists() && !isInstalledByLcaac(target)) {
                throw SkillInstallException(
                    "${target.path} exists and was not installed by lcaac: remove it, or install elsewhere with --dir"
                )
            }
        }
        skills.forEach { skill ->
            val target = File(directory, skill.name)
            target.deleteRecursively()
            skill.files.forEach { file ->
                val content = read(skill.name, file).let { if (file == SKILL_FILE) withVersion(it, version) else it }
                File(target, file).apply { parentFile.mkdirs() }.writeText(content)
            }
        }
        return skills
    }

    companion object {
        const val SKILL_FILE = "SKILL.md"
        private const val VERSION_KEY = "lcaac-version"
        private val versionLine = Regex("(?m)^(\\s*$VERSION_KEY:\\s*).*$")

        /** The top-level string fields of the YAML frontmatter of a `SKILL.md` file. */
        fun frontmatter(content: String): Map<String, String> {
            val match = Regex("\\A---\\R(.*?)\\R---(\\R|\\z)", RegexOption.DOT_MATCHES_ALL).find(content)
                ?: return emptyMap()
            val map = Yaml.default.parseToYamlNode(match.groupValues[1]) as? YamlMap ?: return emptyMap()
            return map.entries.entries.mapNotNull { (key, value) ->
                (value as? YamlScalar)?.let { key.content to it.content }
            }.toMap()
        }

        /** The body of a `SKILL.md` file, after its frontmatter. */
        fun body(content: String): String =
            content.replaceFirst(Regex("\\A---\\R.*?\\R---(\\R|\\z)", RegexOption.DOT_MATCHES_ALL), "")

        private fun withVersion(content: String, version: String): String =
            versionLine.replaceFirst(content, "$1\"$version\"")

        private fun isInstalledByLcaac(directory: File): Boolean =
            File(directory, SKILL_FILE).takeIf { it.isFile }?.readText()
                ?.let { content -> versionLine.containsMatchIn(content.substringBefore("\n---", "")) } ?: false
    }
}

class SkillInstallException(message: String) : Exception(message)
