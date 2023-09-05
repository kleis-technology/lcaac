package ch.kleis.lcaplugin.ui.toolwindow.contribution_analysis

import ch.kleis.lcaplugin.actions.sankey.SankeyGraphBuilder
import ch.kleis.lcaplugin.core.graph.Graph
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.ui.toolwindow.LcaToolWindowContent
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.jcef.JBCefBrowser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.BorderLayout
import javax.swing.JMenuBar
import javax.swing.JPanel

class SankeyGraphWindow(
    processName: String,
    graph: Graph,
    indicatorList: List<MatrixColumnIndex<BasicNumber>>,
    private val graphBuilder: SankeyGraphBuilder,
) : LcaToolWindowContent {
    private val content: JPanel

    init {

        /*
            Browser
         */
        val browser = JBCefBrowser()
        browser.loadHTML(buildWebPage(graph))

        /*
            Menu bar
         */
        val comboBox = ComboBox<MatrixColumnIndex<BasicNumber>>()
        indicatorList.forEach(comboBox::addItem)
        comboBox.addActionListener {
            if (it.actionCommand == "comboBoxChanged") {
                @Suppress("UNCHECKED_CAST")
                val indicator = comboBox.selectedItem as MatrixColumnIndex<BasicNumber>
                val html = buildWebPage(graphBuilder.buildContributionGraph(indicator))
                browser.loadHTML(html)
            }
        }

        val menuBar = JMenuBar()
        menuBar.add(JBLabel("Contribution flows from $processName to"))
        menuBar.add(JBBox.createHorizontalGlue())
        menuBar.add(comboBox)
        menuBar.add(JBBox.createHorizontalGlue(), BorderLayout.LINE_END)

        /*
            Content
         */

        content = JPanel(BorderLayout())
        content.add(menuBar, BorderLayout.NORTH)
        content.add(browser.component, BorderLayout.CENTER)
    }

    override fun getContent(): JPanel {
        return content
    }

    private fun buildWebPage(graph: Graph): String {
        val graphData = Json.encodeToString(graph)
        return """
           <!DOCTYPE HTML>
           <html>
             <head>
                <meta charset="utf-8">
                <title>Sankey</title>
             </head>
             <body>
               <div id="error-container"></div>
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
