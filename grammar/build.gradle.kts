import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    `java-library`
    id("maven-publish")
    id("antlr")
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
    kotlin("plugin.serialization") version "1.8.10"
}

group = properties("lcaacGroup")
version = properties("lcaacVersion")

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    testImplementation(project(":core"))

    antlr("org.antlr:antlr4:4.7.2") { // use ANTLR version 4
        exclude("com.ibm.icu", "icu4j")
    }

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

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

sourceSets {
    main {
        java {
            srcDirs("src/main/gen")
        }
        antlr {
            srcDirs("src/main/antlr")
        }
    }
}

tasks {
    generateGrammarSource {
        outputDirectory = file("src/main/gen/ch/kleis/lcaac/grammar/parser")
        arguments = arguments.plus( // https://github.com/antlr/antlr4/blob/master/doc/tool-options.md
            listOf(
                "-package", "ch.kleis.lcaac.grammar.parser",
            )
        )
    }

    compileJava {
        dependsOn("generateGrammarSource")
    }
    compileKotlin {
        dependsOn("generateGrammarSource")
    }
    compileTestKotlin {
        dependsOn("generateTestGrammarSource")
    }
}

publishing {
    publications {
        create<MavenPublication>("grammar") {
            from(components["java"])
        }
    }
}
