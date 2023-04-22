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

    init {
//        val panel = JPanel()
//        panel.background = Color.green;
//        val myProgressTextLabel = JLabel("Progess....")
        var myCanceled = false
        val gridBag = GridBag().fillCellHorizontally()
        this.add(
            JLabel(MyBundle.message("lca.dialog.import.in_progress.label")),
            gridBag.nextLine().anchor(GridBagConstraints.WEST)
        )
//        panel.add(myProgressTextLabel, gridBag.nextLine().insetBottom(20))
//        myProgressTextLabel.background = Color.blue
//        myProgressTextLabel.isOpaque = true
        val c = GridBagConstraints()
//        c.gridx = 1
//        c.weightx = 1.0
//        c.weighty = 0.0
//        c.gridwidth = 1
//        c.gridheight = 1
//        c.fill = 2
//        c.anchor = GridBagConstraints.WEST
//        c.insets = JBUI.insets(0, 0, 0, 0)
//        panel.add(myProgressTextLabel, gridBag.nextLine().fillCellHorizontally().insetBottom(20))
//        panel.add(myProgressTextLabel)
//        myProgressBar.preferredSize = Dimension(JBUI.scale(600), myProgressBar.preferredSize.height)
//        myProgressBar.value = 20

        val c2 = gridBag.nextLine().fillCellHorizontally().anchor(GridBagConstraints.WEST).weightx(1.0)
        this.add(
            myProgressBar,
            c2
//            c
        )
        val cancelButton = JButton(IdeBundle.message("button.cancel.without.mnemonic"))
        cancelButton.isEnabled = true
        this.add(cancelButton, gridBag.nextLine())
        this.border = JBUI.Borders.empty(10, 0)
        cancelButton.addActionListener { _: ActionEvent ->
            cancelAction?.run()
//            settingsPanel!!.remove(panel)
//            settingsPanel!!.repaint()
        }
//        Thread() {
//            for (i in 1..5) {
//                myProgressBar.value = (100.0 * i / 5).roundToInt()
//                Thread.sleep(2000)
//            }
//        }.start()
    }

    override fun notifyProgress(percent: Int) {
        myProgressBar.value = percent
    }

    override fun notifyCurrentWork(current: String) {
        TODO("Not yet implemented")
    }

}