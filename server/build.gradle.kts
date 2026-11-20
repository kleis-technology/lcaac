fun properties(key: String) = project.findProperty(key).toString()

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")

    kotlin("plugin.serialization")
}

val javaVersion = properties("javaVersion")

kotlin {
    jvmToolchain(Integer.parseInt(javaVersion))
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(project(":grammar"))
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.charleskorn.kaml:kaml:0.59.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    val ktorVersion = "2.3.12"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")

    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
