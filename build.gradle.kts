fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.0" apply false
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion")
    }
}
