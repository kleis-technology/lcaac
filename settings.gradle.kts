rootProject.name = "ch.kleis.lcaac"
include("core")
include("grammar")

pluginManagement {
    val kotlinPluginVersion = "1.9.0"
    val kspPluginVersion = "1.0.11"

    plugins {
        id("com.google.devtools.ksp") version "${kotlinPluginVersion}-${kspPluginVersion}"
        id("org.jetbrains.kotlin.jvm") version kotlinPluginVersion

        kotlin("plugin.serialization") version kotlinPluginVersion
    }
}
