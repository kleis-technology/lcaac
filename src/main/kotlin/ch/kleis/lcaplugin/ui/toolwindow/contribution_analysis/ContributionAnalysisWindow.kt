package ch.kleis.lcaplugin.ui.toolwindow.contribution_analysis

import ch.kleis.lcaplugin.core.assessment.ContributionAnalysis
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.ui.toolwindow.FloatingPointRepresentation
import ch.kleis.lcaplugin.ui.toolwindow.LcaToolWindowContent
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.components.JBBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBEmptyBorder
import org.jdesktop.swingx.plaf.basic.core.BasicTransferable
import java.awt.BorderLayout
import java.awt.datatransfer.Transferable
import javax.swing.*
import javax.swing.plaf.UIResource
import javax.swing.table.DefaultTableCellRenderer

/*
    https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/tool_window
 */

class ContributionAnalysisWindow(
    analysis: ContributionAnalysis,
    observablePortComparator: Comparator<MatrixColumnIndex<BasicNumber>>,
    val project: Project,
    val name: String,
) :
    LcaToolWindowContent {
    private val content: JPanel

    init {
        /*
            Table pane
         */
        val tableModel = ContributionTableModel(analysis, observablePortComparator)

        val table = JBTable(tableModel)
        table.transferHandler = WithHeaderTransferableHandler()

        val cellRenderer = DefaultTableCellRenderer()
        cellRenderer.horizontalAlignment = JLabel.RIGHT
        table.setDefaultRenderer(FloatingPointRepresentation::class.java, cellRenderer)

        val tablePane = JBScrollPane(table)
        tablePane.border = JBEmptyBorder(0)

        /*
            Menu bar
         */
        val button = JButton(AllIcons.Actions.MenuSaveall)
        button.addActionListener {
            val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return@addActionListener
            val content = ContentFactory.getInstance()
                .createContent(
                    ContributionAnalysisHugeWindow(
                        analysis,
                        observablePortComparator,
                        "lca.dialog.export.info",
                        project
                    ).getContent(),
                    name,
                    false
                )
            toolWindow.contentManager.addContent(content)
            toolWindow.contentManager.setSelectedContent(content)
            toolWindow.show()
        }

        val menuBar = JMenuBar()
        menuBar.add(JBLabel("Contribution analysis"), BorderLayout.LINE_START)
        menuBar.add(JBBox.createHorizontalGlue())
        menuBar.add(button, BorderLayout.LINE_END)

        /*
            Content
         */
        content = JPanel(BorderLayout())
        content.add(menuBar, BorderLayout.PAGE_START)
        content.add(tablePane, BorderLayout.CENTER)
        content.updateUI()
    }

    override fun getContent(): JPanel {
        return content
    }

    class WithHeaderTransferableHandler : TransferHandler(), UIResource {

        /**
         * Create a Transferable to use as the source for a data transfer.
         *
         * @param c  The component holding the data to be transferred.  This
         * argument is provided to enable sharing of TransferHandlers by
         * multiple components.
         * @return  The representation of the data to be transferred.
         */
        public override fun createTransferable(c: JComponent?): Transferable? {
            if (c is JTable) {
                val rows: IntArray?
                val cols: IntArray?
                if (!c.rowSelectionAllowed && !c.columnSelectionAllowed) {
                    return null
                }
                if (!c.rowSelectionAllowed) {
                    val rowCount = c.rowCount
                    rows = IntArray(rowCount)
                    for (counter in 0 until rowCount) {
                        rows[counter] = counter
                    }
                } else {
                    rows = c.selectedRows
                }
                if (!c.columnSelectionAllowed) {
                    val colCount = c.columnCount
                    cols = IntArray(colCount)
                    for (counter in 0 until colCount) {
                        cols[counter] = counter
                    }
                } else {
                    cols = c.selectedColumns
                }
                if (rows == null || cols == null || rows.isEmpty() || cols.isEmpty()) {
                    return null
                }
                val plainStr = StringBuilder()
                val htmlStr = StringBuilder()
                htmlStr.append("<html>\n<body>\n<table>\n")
                htmlStr.append("<tr>\n")
                for (col in cols.indices) {
                    val obj = c.getColumnName(cols[col])
                    val `val` = obj?.toString() ?: ""
                    plainStr.append(`val`).append('\t')
                    htmlStr.append("  <th>").append(`val`).append("</th>\n")
                }
                plainStr.append("\n")
                htmlStr.append("</tr>\n")
                for (row in rows.indices) {
                    htmlStr.append("<tr>\n")
                    for (col in cols.indices) {
                        val obj = c.getValueAt(rows[row], cols[col])
                        val `val` = obj?.toString() ?: ""
                        plainStr.append(`val`).append('\t')
                        htmlStr.append("  <td>").append(`val`).append("</td>\n")
                    }
                    // we want a newline at the end of each line and not a tab
                    plainStr.deleteCharAt(plainStr.length - 1).append('\n')
                    htmlStr.append("</tr>\n")
                }

                // remove the last newline
                plainStr.deleteCharAt(plainStr.length - 1)
                htmlStr.append("</table>\n</body>\n</html>")
                return BasicTransferable(plainStr.toString(), htmlStr.toString())
            }
            return null
        }

        override fun getSourceActions(c: JComponent?): Int {
            return COPY
        }
    }
}
