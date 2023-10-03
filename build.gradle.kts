fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("org.jetbrains.kotlin.jvm") apply false
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion")
    }
}
