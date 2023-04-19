package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.core.graph.Graph
import com.intellij.ui.jcef.JBCefBrowser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.BorderLayout
import javax.swing.JPanel

class LcaGraphChildProcessesResult(private val graphData: Graph): LcaToolWindowContent {
    override fun getContent(): JPanel {
        val myPanel = JPanel(BorderLayout())
        val myBrowser = JBCefBrowser()
        myBrowser.loadHTML(buildWebPage(Json.encodeToString(graphData)))
        myPanel.add(myBrowser.component, BorderLayout.CENTER)
        return myPanel
    }

    private fun buildWebPage(graphData: String): String {
        return """
            <!DOCTYPE HTML>
            <html>
            <body>
            Graph data:<br/>
            $graphData
            </body>
            </html>
        """.trimIndent()
    }
}