package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.core.graph.Graph
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.containers.tail
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.BorderLayout
import javax.swing.*

class SankeyGraphResult(
    private val graphData: Graph,
    private val indicatorList: List<MatrixColumnIndex>,
) : LcaToolWindowContent {

    override fun getContent(): JPanel {
        val myPanel = JPanel(BorderLayout())

        val myMenuBar = JMenuBar()
        myMenuBar.add(JLabel("Choose an indicator: "))

        // First element of the list is the currently selected indicator.
        val myMenu = JMenu(indicatorList.first().getDisplayName())
        indicatorList.tail().forEach {
            myMenu.add(JMenuItem(it.getDisplayName()))
        }

        myMenuBar.add(myMenu)
        myPanel.add(myMenuBar, BorderLayout.NORTH)

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