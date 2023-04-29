package ch.kleis.lcaplugin.ide.imports

import ch.kleis.lcaplugin.MyBundle
import ch.kleis.lcaplugin.ide.component.ProgressBar
import ch.kleis.lcaplugin.ide.imports.progressbar.AsynchronousImportWorker
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.DialogWrapperPeer
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Pair
import com.intellij.openapi.wm.impl.welcomeScreen.ActionGroupPanelWrapper
import com.intellij.openapi.wm.impl.welcomeScreen.FlatWelcomeFrame
import com.intellij.ui.ScrollingUtil
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.update.UiNotifyConnector
import javax.swing.JComponent
import javax.swing.JPanel


class LcaImportDialog<P>(private val panel: P) :
    DialogWrapper(ProjectManager.getInstance().defaultProject) where P : ImportHandler, P : JPanel {

    private var panelAndActions: Pair<JPanel, JBList<AnAction>>? = null

    private var worker: AsynchronousImportWorker? = null

    init {
        super.init()
        val peer: DialogWrapperPeer = peer
        val pane = peer.rootPane
        if (pane != null) {
            val size = JBUI.size(FlatWelcomeFrame.MAX_DEFAULT_WIDTH, FlatWelcomeFrame.DEFAULT_HEIGHT)
            pane.minimumSize = size
            pane.preferredSize = size
        }
    }

    override fun createCenterPanel(): JComponent? {
        title = MyBundle.message("lca.dialog.import.title")
        val action = object : AnAction() {
            override fun actionPerformed(e: AnActionEvent) {// Nothing
            }
        }
        val root = DefaultActionGroup(action)

        Disposer.register(disposable) { root.removeAll() }
        val groupActions = ActionGroupPanelWrapper.createActionGroupPanel(root, null, disposable)
        val component = groupActions.first
        component.add(panel)
        panelAndActions = groupActions
        UiNotifyConnector.doWhenFirstShown(panelAndActions!!.second) {
            ScrollingUtil.ensureSelectionExists(
                panelAndActions!!.second
            )
        }
        ActionGroupPanelWrapper.installQuickSearch(groupActions.second)
        return component
    }


    override fun getPreferredFocusedComponent(): JComponent {
        return FlatWelcomeFrame.getPreferredFocusedComponent(panelAndActions!!)
    }

    private fun importOnSuccess() {
        close(OK_EXIT_CODE)
    }

    private fun importOnError(progressBar: ProgressBar) {
        panel.remove(progressBar)
        panel.repaint()
        getButton(myOKAction)?.isEnabled = true
        getButton(myCancelAction)?.isEnabled = true
    }

    override fun doOKAction() {
        if (okAction.isEnabled) {

            val progressBar = ProgressBar()
            val importer = panel.importer()
            val worker =
                AsynchronousImportWorker(importer, this::importOnSuccess, { importOnError(progressBar) }, progressBar)
            getButton(myOKAction)?.isEnabled = false
            getButton(myCancelAction)?.isEnabled = false
            panel.add(progressBar)
            pack()
            worker.start()
        }
    }

    override fun doCancelAction() {
        super.doCancelAction()
        worker?.active = false
    }

    public override fun doValidate(): ValidationInfo? {
        return panel.doValidate()
    }


    override fun getStyle(): DialogStyle {
        return DialogStyle.COMPACT
    }


    override fun createSouthPanel(): JComponent {
        val result = super.createSouthPanel()
        helpAction.isEnabled = true
        return result
    }

    override fun getHelpId(): String {
        return "not null"
    }

    override fun doHelpAction() {
        if (myHelpAction.isEnabled) {
            val helpUrl = MyBundle.message("lca.dialog.import.help.url")
            BrowserUtil.browse(helpUrl)
        }
    }
}

