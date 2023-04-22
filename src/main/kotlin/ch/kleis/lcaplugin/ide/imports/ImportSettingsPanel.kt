package ch.kleis.lcaplugin.ide.imports

import ch.kleis.lcaplugin.MyBundle
import com.intellij.BundleBase
import com.intellij.ide.IdeBundle
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.CheckBoxWithDescription
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.util.ui.FormBuilder
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

class ImportSettingsPanel(private val settings: LcaImportSettings) : JPanel(VerticalFlowLayout()) {

    val libField: JComponent
    val packageField: JComponent

    init {
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
        builder.addLabeledComponent(substanceComp.label, substanceComp.component)
        this.add(builder.panel)
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

}