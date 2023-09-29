fun properties(key: String) = project.findProperty(key).toString()

plugins {
    `java-library`
    `maven-publish`

    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.jvm")

    kotlin("plugin.serialization")
}

val groupId = properties("lcaacGroup")
val artifactId = "core"
val artifactVersion = properties("lcaacVersion")
val javaVersion = properties("javaVersion")

kotlin {
    jvmToolchain(Integer.parseInt(javaVersion))
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.ejml:ejml-simple:0.43")
    implementation("org.jetbrains.kotlinx:multik-core:0.2.2")
    implementation("org.jetbrains.kotlinx:multik-default:0.2.2")


    val arrowVersion = "1.1.5"
    val kotlinxSerializationJSONVersion = "1.5.1"

    implementation(platform("io.arrow-kt:arrow-stack:$arrowVersion"))
    implementation("io.arrow-kt:arrow-core")
    implementation("io.arrow-kt:arrow-optics")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJSONVersion")
    ksp("io.arrow-kt:arrow-optics-ksp-plugin:$arrowVersion")

    testImplementation("io.mockk:mockk:1.13.4")
    implementation(kotlin("stdlib-jdk8"))

    val log4jVersion = "2.20.0"
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/kleis-technology/lca-plugin")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("core") {
            groupId = groupId
            artifactId = artifactId
            version = artifactVersion
            from(components["java"])
        }
    }
}
