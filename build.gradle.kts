fun properties(key: String) = project.findProperty(key).toString()

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion")
    }
}
