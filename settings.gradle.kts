rootProject.name = "ch.kleis.lcaac"
include("core")
include("plugin")
include("grammar")

pluginManagement {
    val grammarKitPluginVersion = "2021.2.2"
    val intellijPluginVersion = "1.15.0"
    val jetbrainsChangelogPluginVersion = "2.0.0"
    val kotlinPluginVersion = "1.9.0"
    val kspPluginVersion = "1.0.11"
    val qodanaPluginVersion = "0.1.13"

    plugins {
        id("com.google.devtools.ksp") version "${kotlinPluginVersion}-${kspPluginVersion}"
        id("org.jetbrains.changelog") version jetbrainsChangelogPluginVersion
        id("org.jetbrains.grammarkit") version grammarKitPluginVersion
        id("org.jetbrains.intellij") version intellijPluginVersion
        id("org.jetbrains.kotlin.jvm") version kotlinPluginVersion
        id("org.jetbrains.qodana") version qodanaPluginVersion

        kotlin("plugin.serialization") version kotlinPluginVersion
    }
}