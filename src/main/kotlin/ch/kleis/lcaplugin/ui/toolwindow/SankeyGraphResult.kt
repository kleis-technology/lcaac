package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.actions.sankey.SankeyGraphBuilder
import ch.kleis.lcaplugin.core.graph.Graph
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
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
            myMenuBar.add(buildIndicatorChoiceMenu(indicatorList, myBrowser))
            myPanel.add(myMenuBar, BorderLayout.NORTH)
        }

        myBrowser.loadHTML(buildWebPage(Json.encodeToString(graphData), indicatorList.first().referenceUnit().toString()))
        myPanel.add(myBrowser.component, BorderLayout.CENTER)
        return myPanel
    }

    private fun buildIndicatorChoiceMenu(
        indicatorList: List<MatrixColumnIndex>,
        browser: JBCefBrowser,
    ): JComponent {
        val myCombo = ComboBox<MatrixColumnIndex>()

        indicatorList.forEach(myCombo::addItem)

        myCombo.maximumWidth = myCombo.preferredWidth

        myCombo.addActionListener {
            if (it.actionCommand == "comboBoxChanged") {
                browser.loadHTML(buildWebPage(Json.encodeToString(graphData), indicatorList.first().referenceUnit().toString()))
            }
        }

        return myCombo
    }

    private fun buildWebPage(graphData: String, indicatorUnit: String): String {
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
             const unit = "$indicatorUnit";
             const data = $graphData;
                     
             ${this.javaClass.classLoader.getResource("ch/kleis/lcaplugin/lcaGraph.js")?.readText()}
             </script>
           </html>
        """.trimIndent()
    }
}