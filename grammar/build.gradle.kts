fun properties(key: String) = project.findProperty(key).toString()

plugins {
    antlr
    `java-library`
    `maven-publish`

    id("org.jetbrains.kotlin.jvm")

    kotlin("plugin.serialization")
}

val group = properties("lcaacGroup")
val version = properties("lcaacVersion")
val javaVersion = properties("javaVersion")

kotlin {
    jvmToolchain(Integer.parseInt(javaVersion))
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
