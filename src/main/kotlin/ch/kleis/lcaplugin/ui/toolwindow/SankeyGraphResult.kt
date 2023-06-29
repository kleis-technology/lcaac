package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.core.graph.Graph
import com.intellij.ui.jcef.JBCefBrowser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.BorderLayout
import javax.swing.JPanel

class SankeyGraphResult(private val graphData: Graph) : LcaToolWindowContent {
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
             <head>
                <meta charset="utf-8">
                <title>Sankey</title>
             </head>
             <body>
               <div id="container"></div>
             </body>
             <script type="module">
             const data = $graphData;
                     
             ${this.javaClass.classLoader.getResource("ch/kleis/lcaplugin/lcaGraph.js")?.readText()}
             </script>
           </html>
        """.trimIndent()
    }
}