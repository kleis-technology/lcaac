package ch.kleis.lcaac.cli.cmd

import com.github.ajalt.clikt.core.CliktCommand
import java.util.Properties

class VersionCommand : CliktCommand(name = "version", help = "Get version") {
    override fun run() {
        val propsFile = {}.javaClass.classLoader.getResourceAsStream("META-INF/lcaac.properties")
        val props = propsFile.use {
            Properties().apply { load(it) }
        }
        val author = props.getProperty("author")
        val description = props.getProperty("description")
        val version = props.getProperty("version")
        echo(description)
        echo("v${version}")
        echo(author)
    }
}
