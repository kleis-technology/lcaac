package ch.kleis.lcaplugin.ui.toolwindow

import javax.swing.JPanel

/**
 * An interface that all classes wishing to display content in the "LCA Output" toolWindow must respect.
 */
interface LcaToolWindowContent {
    /**
     * Return a JPanel to be displayed in the toolWindow.
     */
    fun getContent(): JPanel
}