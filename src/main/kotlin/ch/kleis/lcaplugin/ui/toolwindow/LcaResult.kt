package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.compute.matrix.ObservableFactorMatrix
import com.intellij.ui.table.BaseTableView
import javax.swing.JPanel

/*
    https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/tool_window
 */

class LcaResult(result: ObservableFactorMatrix) {
    private val content: JPanel = JPanel()

    init {
        val tableModel = ObservableFactorTableModel(result)
        val table = BaseTableView(tableModel)
        content.add(table)
    }

    fun getContent(): JPanel {
        return content
    }
}
