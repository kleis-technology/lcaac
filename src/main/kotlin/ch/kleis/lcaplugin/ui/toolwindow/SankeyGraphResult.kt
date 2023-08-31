package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.actions.sankey.SankeyGraphBuilder
import ch.kleis.lcaplugin.core.graph.Graph
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.util.maximumWidth
import com.intellij.ui.util.preferredWidth
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JMenuBar
import javax.swing.JPanel

class SankeyGraphResult(
    private val processName: String,
    private val graphData: Graph,
    private val indicatorList: List<MatrixColumnIndex<BasicNumber>>,
    private val graphBuilder: SankeyGraphBuilder,
) : LcaToolWindowContent {

    override fun getContent(): JPanel {
        val myPanel = JPanel(BorderLayout())
        val myBrowser = JBCefBrowser()

        if (indicatorList.size > 1) {
            val myMenuBar = JMenuBar()

            myMenuBar.add(JBLabel("Choose an indicator: "))
            myMenuBar.add(buildIndicatorChoiceMenu(indicatorList, myBrowser))
            myPanel.add(myMenuBar, BorderLayout.NORTH)
        }

        myBrowser.loadHTML(buildWebPage(Json.encodeToString(graphData), indicatorList.first().getDisplayName()))
        myPanel.add(myBrowser.component, BorderLayout.CENTER)
        return myPanel
    }

    private fun buildIndicatorChoiceMenu(
        indicatorList: List<MatrixColumnIndex<BasicNumber>>,
        browser: JBCefBrowser,
    ): JComponent {
        val myCombo = ComboBox<MatrixColumnIndex<BasicNumber>>()

        indicatorList.forEach(myCombo::addItem)

        myCombo.maximumWidth = myCombo.preferredWidth

        myCombo.addActionListener {
            if (it.actionCommand == "comboBoxChanged") {
                val newIndicator = myCombo.selectedItem as MatrixColumnIndex<BasicNumber>
                browser.loadHTML(
                    buildWebPage(Json.encodeToString(
                        graphBuilder.buildContributionGraph(newIndicator)),
                        newIndicator.getDisplayName()))
            }
        }

        return myCombo
    }

    private fun buildWebPage(graphData: String, indicatorName: String): String {
        return """
           <!DOCTYPE HTML>
           <html>
             <head>
                <meta charset="utf-8">
                <title>Sankey</title>
             </head>
             <body>
               <h1>Contribution flows from <i>$processName</i> to <i>$indicatorName</i></h1>
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
