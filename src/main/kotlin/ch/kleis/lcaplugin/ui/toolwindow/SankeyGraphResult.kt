package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.actions.sankey.SankeyGraphBuilder
import ch.kleis.lcaplugin.core.graph.Graph
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBMenu
import com.intellij.ui.jcef.JBCefBrowser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.BorderLayout
import javax.swing.JMenuBar
import javax.swing.JPanel

class SankeyGraphResult(
    private val graphData: Graph,
    private val indicatorList: List<MatrixColumnIndex>,
    private val graphBuilder: SankeyGraphBuilder,
) : LcaToolWindowContent {

    override fun getContent(): JPanel {
        val myPanel = JPanel(BorderLayout())
        val myBrowser = JBCefBrowser()

        if (indicatorList.size > 1) {
            val myMenuBar = JMenuBar()

            myMenuBar.add(JBLabel("Choose an indicator: "))

            // First element of the list is the currently selected indicator.
            val myMenu: JBMenu = buildMenu(
                indicatorList.first(),
                indicatorList,
                myMenuBar,
                myBrowser
            )

            myMenuBar.add(myMenu)
            myPanel.add(myMenuBar, BorderLayout.NORTH)
        }

        myBrowser.loadHTML(buildWebPage(Json.encodeToString(graphData)))
        myPanel.add(myBrowser.component, BorderLayout.CENTER)
        return myPanel
    }

    private fun buildMenu(
        currentIndicator: MatrixColumnIndex,
        indicatorList: List<MatrixColumnIndex>,
        parent: JMenuBar,
        browser: JBCefBrowser,
    ): JBMenu {
        val myMenu = JBMenu()
        myMenu.text = currentIndicator.getDisplayName()

        indicatorList.filterNot { it == currentIndicator }.forEach { indicator ->

            val menuItem = JBMenuItem(indicator.getDisplayName())
            menuItem.addActionListener {
                // update contents
                browser.loadHTML(
                    buildWebPage(Json.encodeToString(graphBuilder.buildContributionGraph(indicator)))
                )
                // update menu
                val newMenu = buildMenu(indicator, indicatorList, parent, browser)
                parent.removeAll()
                parent.add(JBLabel("Choose an indicator: "))
                parent.add(newMenu)
            }

            myMenu.add(menuItem)
        }
        return myMenu
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