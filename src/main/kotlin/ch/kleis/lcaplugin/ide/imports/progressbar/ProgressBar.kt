package ch.kleis.lcaplugin.ide.imports.progressbar

import ch.kleis.lcaplugin.MyBundle
import ch.kleis.lcaplugin.imports.simapro.AsynchronousWatcher
import com.intellij.ide.IdeBundle
import com.intellij.util.ui.GridBag
import com.intellij.util.ui.JBUI
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar

class ProgressBar : JPanel(GridBagLayout()), AsynchronousWatcher {
    var cancelAction: Runnable? = null
    private val myProgressBar = JProgressBar(0, 100)
    private val label = JLabel(MyBundle.message("lca.dialog.import.in_progress.label.init"))

    init {
        var myCanceled = false
        val gridBag = GridBag().fillCellHorizontally()
        this.add(
            label,
            gridBag.nextLine().anchor(GridBagConstraints.WEST)
        )

        this.add(
            myProgressBar,
            gridBag.nextLine().fillCellHorizontally().anchor(GridBagConstraints.WEST).weightx(1.0)
        )
        val cancelButton = JButton(IdeBundle.message("button.cancel.without.mnemonic"))
        cancelButton.isEnabled = true
        this.add(cancelButton, gridBag.nextLine())
        this.border = JBUI.Borders.empty(10, 0)
        cancelButton.addActionListener { _: ActionEvent ->
            cancelAction?.run()
        }
    }

    override fun notifyProgress(percent: Int) {
        myProgressBar.value = percent
    }

    override fun notifyCurrentWork(current: String) {
        label.text = MyBundle.getMessage("lca.dialog.import.in_progress.label.progress", current)
    }

}