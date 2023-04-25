package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.core.graph.Graph
import com.intellij.ui.jcef.JBCefBrowser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.BorderLayout
import javax.swing.JPanel

class LcaGraphChildProcessesResult(private val graphData: Graph) : LcaToolWindowContent {
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
               <script src="https://d3js.org/d3.v7.min.js"></script>
               <style>
                 .shape {
                   fill: white;
                   stroke-width: 1.5px;
                 }
                 .productShape {
                   stroke: blue;
                 }
                 .substanceShape {
                   stroke: green;
                 }
                 .processShape {
                   stroke: black;
                 }
                 .link {
                   stroke: black;
                   stroke-width: 1.5px;
                 }
                 .fixed {
                   fill: red;
                 }
               </style>
             </head>
             <body>
               <script>
                 const width = 1500;
                 const height = (width * 9) / 16;

                 // Put JSON here
                 const graph = $graphData;
 
                 ${this.javaClass.classLoader.getResource("ch/kleis/lcaplugin/lcaGraph.js")?.readText()}
 
                main(width, height, graph);
               </script>
             </body>
           </html>
        """.trimIndent()
    }
}