package ch.kleis.lcaplugin.ide.imports.ecospold

import ch.kleis.lcaplugin.MyBundle
import ch.kleis.lcaplugin.ide.imports.ImportHandler
import ch.kleis.lcaplugin.ide.imports.LcaImportDialog
import ch.kleis.lcaplugin.imports.Importer
import ch.kleis.lcaplugin.imports.ecospold.lcai.EcospoldImporter
import com.intellij.BundleBase
import com.intellij.ide.IdeBundle
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.*
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.ItemEvent
import java.nio.file.Path
import javax.swing.JComponent
import javax.swing.JPanel
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

class EcospoldImportSettingsPanel(private val settings: EcospoldImportSettings) : JPanel(VerticalFlowLayout()),
    ImportHandler {

    private val libField: JComponent
    private val packageField: JComponent

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
        this.add(builder.panel)
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

    override fun importer(): Importer {
        return EcospoldImporter(settings)
    }

    override fun doValidate(): ValidationInfo? {
        return listOf(
            { -> LcaImportDialog.validateRegularFile(settings.libraryFile, libField) },
            { -> LcaImportDialog.validatePackageIsValid(settings.rootPackage, packageField) })
            .firstNotNullOfOrNull { it.invoke() }
    }

}