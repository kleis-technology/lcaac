package ch.kleis.lcaplugin.ide.imports

import com.intellij.openapi.ui.DialogWrapper
import org.jetbrains.annotations.Nullable
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel


class SampleDialogWrapper : DialogWrapper(true) {
    init {
        title = "Test DialogWrapper"
        init()
    }

    @Nullable
    override fun createCenterPanel(): JComponent? {
        val dialogPanel = JPanel(BorderLayout())
        val label = JLabel("testing")
        label.preferredSize = Dimension(100, 100)
        dialogPanel.add(label, BorderLayout.CENTER)
        return dialogPanel
    }
}