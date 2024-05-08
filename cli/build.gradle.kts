fun properties(key: String) = project.findProperty(key).toString()

plugins {
    application
    `maven-publish`
    id("org.jetbrains.kotlin.jvm")
}

val groupId = properties("lcaacGroup")
val artifactId = "cli"
val artifactVersion = properties("lcaacVersion")
val javaVersion = properties("javaVersion")

application {
    applicationName = "lcaac"
    mainClass.set("ch.kleis.lcaac.cli.MainKt")
}

kotlin {
    jvmToolchain(Integer.parseInt(javaVersion))
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    testImplementation(project(":core"))

    implementation(project(":grammar"))
    testImplementation(project(":grammar"))

    testImplementation("io.mockk:mockk:1.13.4")
    implementation(kotlin("stdlib-jdk8"))

    val log4jVersion = "2.20.0"
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")

    testImplementation(kotlin("test"))

    implementation("com.github.ajalt.clikt:clikt:4.2.2")
    implementation("org.apache.commons:commons-csv:1.10.0")

    implementation("com.charleskorn.kaml:kaml:0.59.0")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/kleis-technology/lcaac")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("cli") {
            groupId = groupId
            artifactId = artifactId
            version = artifactVersion
            from(components["java"])
        }
    }
}
