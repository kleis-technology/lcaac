package ch.kleis.lcaplugin.ide.imports

import ch.kleis.lcaplugin.MyBundle
import ch.kleis.lcaplugin.ide.component.ProgressBar
import ch.kleis.lcaplugin.ide.imports.progressbar.AsynchronousImportWorker
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
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
import java.nio.file.Path
import javax.swing.JComponent
import javax.swing.JPanel
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile


class LcaImportDialog(private val settings: LcaImportSettings) :
    DialogWrapper(ProjectManager.getInstance().defaultProject) {

    private var panelAndActions: Pair<JPanel, JBList<AnAction>>? = null
    private var settingsPanel: ImportSettingsPanel? = null
    private var worker: AsynchronousImportWorker? = null

    constructor() : this(LcaImportSettings.instance)

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

        val root: DefaultActionGroup = createRootStep()
        Disposer.register(disposable) { root.removeAll() }
        val groupActions = ActionGroupPanelWrapper.createActionGroupPanel(root, null, disposable)
        val component = groupActions.first
        settingsPanel = ImportSettingsPanel(settings)
        component.add(settingsPanel)
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
        settingsPanel?.remove(progressBar)
        settingsPanel?.repaint()
        getButton(myOKAction)?.isEnabled = true
        getButton(myCancelAction)?.isEnabled = true
    }

    override fun doOKAction() {
        if (okAction.isEnabled) {
            val progressBar = ProgressBar()
            val worker =
                AsynchronousImportWorker(settings, progressBar, { importOnSuccess() }, { importOnError(progressBar) })
            getButton(myOKAction)?.isEnabled = false
            getButton(myCancelAction)?.isEnabled = false
            settingsPanel?.add(progressBar)
            pack()
            worker.start()
        }
    }

    override fun doCancelAction() {
        super.doCancelAction()
        worker?.active = false
    }

    public override fun doValidate(): ValidationInfo? {
        val libPath = Path.of(settings.libraryFile)
        if (!libPath.exists() || !libPath.isRegularFile()) {
            return ValidationInfo(MyBundle.message("lca.dialog.import.library.file.error"), settingsPanel!!.libField)
        }
        if (!Regex("[a-zA-Z0-9]*").matches(settings.rootPackage)
            || Regex("^[0-9]").matches(settings.rootPackage)
        ) {
            return ValidationInfo(MyBundle.message("lca.dialog.import.package.error"), settingsPanel!!.packageField)
        }
        return null
    }


    override fun getStyle(): DialogStyle {
        return DialogStyle.COMPACT
    }

    private fun createRootStep(): LcaImportStep {
        return LcaImportStep()
    }

    override fun createSouthPanel(): JComponent {
        val result = super.createSouthPanel()
        helpAction.isEnabled = true
        return result
    }

    //
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

