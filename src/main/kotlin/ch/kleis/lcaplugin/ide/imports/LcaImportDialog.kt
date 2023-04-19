package ch.kleis.lcaplugin.ide.imports

import ch.kleis.lcaplugin.MyBundle
import ch.kleis.lcaplugin.imports.ImportException
import ch.kleis.lcaplugin.imports.simapro.Importer
import com.intellij.BundleBase
import com.intellij.ide.IdeBundle
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Pair
import com.intellij.openapi.wm.impl.welcomeScreen.ActionGroupPanelWrapper
import com.intellij.openapi.wm.impl.welcomeScreen.FlatWelcomeFrame
import com.intellij.ui.ScrollingUtil
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.update.UiNotifyConnector
import java.awt.BorderLayout
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.ItemEvent
import java.nio.file.Path
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JPanel
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile


class LcaImportDialog(val settings: LcaImportSettings) : DialogWrapper(ProjectManager.getInstance().defaultProject) {

    private var myPair: Pair<JPanel, JBList<AnAction>>? = null
    private var libField: JComponent? = null
    private var packageField: JComponent? = null

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
        val pair = ActionGroupPanelWrapper.createActionGroupPanel(root, null, disposable)
        val component = pair.first
        addSettings(component)
        myPair = pair
        UiNotifyConnector.doWhenFirstShown(myPair!!.second) { ScrollingUtil.ensureSelectionExists(myPair!!.second) }
        ActionGroupPanelWrapper.installQuickSearch(pair.second)
        return component
    }

    private fun addSettings(component: JPanel) {
        val builder = FormBuilder()
        val locComp = createLocationComponent()
        builder.addLabeledComponent(locComp.label, locComp.component)
        val packCom = createPackageComponent()
        packageField = packCom.component
        builder.addLabeledComponent(packCom.label, packCom.component)
        val libComp = createLibraryFileComponent()
        libField = libComp.component.textField
        builder.addLabeledComponent(libComp.label, libComp.component)
        builder.addComponent(
            CheckBoxWithDescription(
                JBCheckBox(
                    BundleBase.replaceMnemonicAmpersand(MyBundle.message("lca.dialog.import.units.label")),
                    settings.importUnits
                ).apply {
                    addItemListener { e ->
                        settings.importUnits = e.stateChange == ItemEvent.SELECTED
                    }
                },
                MyBundle.message("lca.dialog.import.units.desc")
            )
        )
        builder.addComponent(
            CheckBoxWithDescription(
                JBCheckBox(
                    BundleBase.replaceMnemonicAmpersand(MyBundle.message("lca.dialog.import.processes.label")),
                    settings.importProcesses
                ).apply {
                    addItemListener { e ->
                        settings.importProcesses = e.stateChange == ItemEvent.SELECTED
                    }
                },
                MyBundle.message("lca.dialog.import.processes.desc")
            )
        )
        val substanceComp = createSubstanceCombo()
//        substanceField = substanceComp.component
        builder.addLabeledComponent(substanceComp.label, substanceComp.component)
        component.add(
            JPanel(VerticalFlowLayout())
                .apply {
                    add(builder.panel)
                })
    }

    private fun createSubstanceCombo(): LabeledComponent<JPanel> {
        val simaproMsg =
            BundleBase.replaceMnemonicAmpersand(MyBundle.message("lca.dialog.import.substances.desc.simapro"))
        val ef30Msg = BundleBase.replaceMnemonicAmpersand(MyBundle.message("lca.dialog.import.substances.desc.ef30"))
        val ef31Msg = BundleBase.replaceMnemonicAmpersand(MyBundle.message("lca.dialog.import.substances.desc.ef31"))
        val rSimapro = JBRadioButton(simaproMsg, settings.importSubstancesMode == SubstanceImportMode.SIMAPRO)
        val rEf30 = JBRadioButton(ef30Msg, settings.importSubstancesMode == SubstanceImportMode.EF30)
        val rEf31 = JBRadioButton(ef31Msg, settings.importSubstancesMode == SubstanceImportMode.EF31)
        val group = ButtonGroup()
        group.add(rSimapro)
        group.add(rEf30)
        group.add(rEf31)
        rSimapro.addActionListener {
            settings.importSubstancesMode = SubstanceImportMode.SIMAPRO
        }
        rEf30.addActionListener {
            settings.importSubstancesMode = SubstanceImportMode.EF30
        }
        rEf31.addActionListener {
            settings.importSubstancesMode = SubstanceImportMode.EF31
        }
        val panel = JPanel(VerticalFlowLayout())
            .apply { add(rSimapro) }
            .apply { add(rEf30) }
            .apply { add(rEf31) }
        return LabeledComponent.create(
            panel,
            BundleBase.replaceMnemonicAmpersand(MyBundle.message("lca.dialog.import.substances.label")),
            BorderLayout.WEST
        )
    }

    private fun createPackageComponent(): LabeledComponent<JBTextField> {
        val pack = object : ExtendableTextField(20) {
        }
        pack.text = settings.rootPackage
        pack.addFocusListener(object : FocusAdapter() {
            override fun focusLost(e: FocusEvent?) {
                settings.rootPackage = pack.text
            }
        })

        return LabeledComponent.create(
            pack,
            BundleBase.replaceMnemonicAmpersand(MyBundle.message("lca.dialog.import.package.label")),
            BorderLayout.WEST
        )
    }


    private fun createLocationComponent(): LabeledComponent<TextFieldWithBrowseButton> {
        val myLocationField = TextFieldWithBrowseButton()
        val curProject = if (ProjectManager.getInstance().openProjects.isNotEmpty()) {
            ProjectManager.getInstance().openProjects[0].basePath ?: ""
        } else {
            ""
        }
        val root = settings.rootFolder
        val myProjectDirectory = Path.of(root.ifEmpty { curProject })
        val projectLocation: String = myProjectDirectory.toString()
        myLocationField.text = projectLocation
        val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
        myLocationField.addBrowseFolderListener(
            MyBundle.message("lca.dialog.import.root.folder.label"),
            MyBundle.message("lca.dialog.import.root.folder.desc"), null, descriptor
        )
        myLocationField.textField.addFocusListener(object : FocusAdapter() {
            override fun focusLost(e: FocusEvent?) {
                settings.rootFolder = myLocationField.textField.text
            }
        })

        return LabeledComponent.create(
            myLocationField,
            BundleBase.replaceMnemonicAmpersand(IdeBundle.message("directory.project.location.label")),
            BorderLayout.WEST
        )
    }


    private fun createLibraryFileComponent(): LabeledComponent<TextFieldWithBrowseButton> {
        val myLocationField = TextFieldWithBrowseButton()
        val file = Path.of(settings.libraryFile)
        myLocationField.text = if (file.isRegularFile() && file.exists()) file.toString() else ""
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
        myLocationField.addBrowseFolderListener(
            MyBundle.message("lca.dialog.import.library.file.label"),
            MyBundle.message("lca.dialog.import.library.file.desc"), null, descriptor
        )
        myLocationField.textField.addFocusListener(object : FocusAdapter() {
            override fun focusLost(e: FocusEvent?) {
                settings.libraryFile = myLocationField.textField.text
            }
        })

        return LabeledComponent.create(
            myLocationField,
            BundleBase.replaceMnemonicAmpersand(MyBundle.message("lca.dialog.import.library.file.label")),
            BorderLayout.WEST
        )
    }


    override fun getPreferredFocusedComponent(): JComponent {
        return FlatWelcomeFrame.getPreferredFocusedComponent(myPair!!)
    }

    override fun doOKAction() {
        if (okAction.isEnabled) {
            try {
                Importer(settings).import()
                close(OK_EXIT_CODE)
            } catch (e: ImportException) {
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("LcaNotificationError")
                    .createNotification("Unable to import your library: ${e.message}", NotificationType.ERROR)
                    .notify(ProjectManager.getInstance().openProjects.firstOrNull())
            }
        }
    }

    public override fun doValidate(): ValidationInfo? {
        val libPath = Path.of(settings.libraryFile)
        if (!libPath.exists() || !libPath.isRegularFile()) {
            return ValidationInfo(MyBundle.message("lca.dialog.import.library.file.error"), libField)
        }
        if (!Regex("[a-zA-Z0-9]*").matches(settings.rootPackage)
            || Regex("^[0-9]").matches(settings.rootPackage)
        ) {
            return ValidationInfo(MyBundle.message("lca.dialog.import.package.error"), packageField)
        }
        return null
    }


    override fun getStyle(): DialogStyle {
        return DialogStyle.COMPACT
    }

    private fun createRootStep(): LcaImportStep {
        return LcaImportStep()
    }

    override fun getHelpId(): String {
        return "concepts.project"
    }
}

