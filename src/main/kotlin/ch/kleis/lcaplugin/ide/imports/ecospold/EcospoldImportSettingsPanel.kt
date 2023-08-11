package ch.kleis.lcaplugin.ide.imports.ecospold

import ch.kleis.lcaplugin.MyBundle
import ch.kleis.lcaplugin.ide.imports.ImportHandler
import ch.kleis.lcaplugin.ide.imports.LcaImportDialog
import ch.kleis.lcaplugin.ide.imports.ecospold.settings.EcospoldImportSettings
import ch.kleis.lcaplugin.ide.imports.ecospold.settings.LCIASettings
import ch.kleis.lcaplugin.ide.imports.ecospold.settings.LCISettings
import ch.kleis.lcaplugin.imports.Importer
import ch.kleis.lcaplugin.imports.ecospold.EcospoldImporter
import com.intellij.BundleBase
import com.intellij.ide.IdeBundle
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.*
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.ItemEvent
import java.nio.file.Path
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile


class EcospoldImportSettingsPanel(
    private val settings: EcospoldImportSettings,
) : JPanel(VerticalFlowLayout()), ImportHandler {

    private val libField: JComponent
    private val packageField: JComponent
    private val methodNameModel = DefaultComboBoxModel<String>()
    private val warning = JBLabel()

    // LCIA specific field
    private val methodNameField: JComponent?

    // LCI specific field
    private val mappingFileField: JComponent?

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

        warning.foreground = JBColor.ORANGE
        val warningLabelled = LabeledComponent.create(warning, "", BorderLayout.WEST)
        builder.addLabeledComponent(warningLabelled.label, warningLabelled.component)

        when (settings) {
            is LCIASettings -> {
                mappingFileField = null

                val methodLabelled = createMethodComponent(settings)
                methodNameField = methodLabelled.component
                builder.addLabeledComponent(methodLabelled.label, methodLabelled.component)
            }

            is LCISettings -> {
                methodNameField = null

                val mappingFile = createMappingFileComponent(settings)
                mappingFileField = mappingFile.component
                builder.addLabeledComponent(mappingFile.label, mappingFile.component)

                val importBuiltin = createImportBuiltinLibraryComponent(settings)
                builder.addLabeledComponent(importBuiltin.label, importBuiltin.component)
            }
        }


        builder.addComponent(CheckBoxWithDescription(JBCheckBox(BundleBase.replaceMnemonicAmpersand(MyBundle.message("lca.dialog.import.units.label")), settings.importUnits).apply {
            addItemListener { e ->
                settings.importUnits = e.stateChange == ItemEvent.SELECTED
            }
        }, MyBundle.message("lca.dialog.import.units.desc")))
        this.add(builder.panel)
    }

    private fun createMethodComponent(settings: LCIASettings): LabeledComponent<JComponent> {
        val comp = ComboBox(methodNameModel, 300)
        comp.addActionListener {
            if (it.actionCommand == "comboBoxChanged") {
                settings.methodName = methodNameModel.selectedItem?.toString() ?: ""
            }
        }
        methodNameModel.selectedItem = settings.methodName
        return LabeledComponent.create(comp, MyBundle.message("lca.dialog.import.ecospold.method"), BorderLayout.WEST)
    }

    private fun updateMethodModelFromLib() {
        val file = Path.of(settings.libraryFile)
        if (file.exists() && file.isRegularFile()) {
            val names = EcospoldImporter.getMethodNames(file.toString())
            methodNameModel.removeAllElements()
            methodNameModel.addAll(names)
            methodNameModel.selectedItem = ""
        } else {
            methodNameModel.removeAllElements()
            methodNameModel.addAll(listOf(""))
            methodNameModel.selectedItem = ""

        }
    }

    private fun createPackageComponent(): LabeledComponent<JBTextField> {
        val pack = object : ExtendableTextField(20) {}
        pack.text = settings.rootPackage
        pack.addFocusListener(object : FocusAdapter() {
            override fun focusLost(e: FocusEvent?) {
                settings.rootPackage = pack.text
            }
        })

        return LabeledComponent.create(pack, BundleBase.replaceMnemonicAmpersand(MyBundle.message("lca.dialog.import.package.label")), BorderLayout.WEST)
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
        myLocationField.addBrowseFolderListener(MyBundle.message("lca.dialog.import.root.folder.label"), MyBundle.message("lca.dialog.import.root.folder.desc"), null, descriptor)
        myLocationField.textField.addFocusListener(object : FocusAdapter() {
            override fun focusLost(e: FocusEvent?) {
                settings.rootFolder = myLocationField.textField.text
            }
        })

        return LabeledComponent.create(myLocationField, BundleBase.replaceMnemonicAmpersand(IdeBundle.message("directory.project.location.label")), BorderLayout.WEST)
    }


    private fun createLibraryFileComponent(): LabeledComponent<TextFieldWithBrowseButton> {
        val myLocationField = TextFieldWithBrowseButton()
        val file = Path.of(settings.libraryFile)
        myLocationField.text = if (file.isRegularFile() && file.exists()) file.toString() else ""
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
        myLocationField.addBrowseFolderListener(MyBundle.message("lca.dialog.import.library.file.label"), MyBundle.message("lca.dialog.import.library.file.desc"), null, descriptor)
        fun checkLibName() {
            val name = myLocationField.textField.text.lowercase()
            if (name.takeLast(2) != "7z") {
                warning.text = MyBundle.message("lca.dialog.import.ecospold.7zwarning")
            } else {
                when (settings) {
                    is LCIASettings ->
                        if (name.contains("lcia")) {
                            warning.text = ""
                        } else {
                            warning.text = MyBundle.message("lca.dialog.import.ecospold.lcia.warning")
                        }

                    is LCISettings ->
                        if (name.contains("lci")) {
                            warning.text = ""
                        } else {
                            warning.text = MyBundle.message("lca.dialog.import.ecospold.lci.warning")
                        }
                }
            }
        }
        myLocationField.textField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                Logger.getInstance(this::class.java).info(e.toString())
                settings.libraryFile = myLocationField.textField.text
                checkLibName()
                updateMethodModelFromLib()
            }
        })
        checkLibName()
        updateMethodModelFromLib()

        return LabeledComponent.create(myLocationField, BundleBase.replaceMnemonicAmpersand(MyBundle.message("lca.dialog.import.library.file.label")), BorderLayout.WEST)
    }

    private fun createMappingFileComponent(settings: LCISettings): LabeledComponent<TextFieldWithBrowseButton> {
        val myMappingFileField = TextFieldWithBrowseButton()
        val file = Path.of(settings.mappingFile)
        myMappingFileField.text = if (file.exists() && file.isRegularFile()) file.toString() else ""
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()

        myMappingFileField.addBrowseFolderListener(MyBundle.message("lca.dialog.import.ecospold.lci.mappingFile.label"), MyBundle.message("lca.dialog.import.ecospold.lci.mappingFile.desc"), null, descriptor)

        myMappingFileField.textField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                Logger.getInstance(this::class.java).info(e.toString())
                settings.mappingFile = myMappingFileField.textField.text
            }
        })

        @Suppress("DialogTitleCapitalization") return LabeledComponent.create(
            myMappingFileField,
            MyBundle.message("lca.dialog.import.ecospold.lci.mappingFile.label"),
            BorderLayout.WEST,
        )
    }

    private fun createImportBuiltinLibraryComponent(settings: LCISettings): LabeledComponent<ComboBox<LCISettings.Companion.BuiltinLibrary?>> {
        val myComboBox = ComboBox<LCISettings.Companion.BuiltinLibrary?>()
        myComboBox.addItem(null)
        LCISettings.Companion.BuiltinLibrary.values().forEach(myComboBox::addItem)
        myComboBox.addActionListener {
            if (it.actionCommand == "comboBoxChanged") {
                settings.importBuiltinLibrary = myComboBox.selectedItem as LCISettings.Companion.BuiltinLibrary?
            }
        }

        return LabeledComponent.create(
            myComboBox,
            MyBundle.message("lca.dialog.import.ecospold.lci.builtinLibrary.label"),
            BorderLayout.WEST,
        )
    }

    override fun importer(): Importer {
        return EcospoldImporter(settings)
    }

    override fun doValidate(): ValidationInfo? {
        val genericValidations = listOf({ LcaImportDialog.validateRegularFile(settings.libraryFile, libField) }, { LcaImportDialog.validatePackageIsValid(settings.rootPackage, packageField) })

        @Suppress("MoveLambdaOutsideParentheses") val specificValidations = when (settings) {
            is LCIASettings -> listOf(
                { LcaImportDialog.validateNonEmpty(settings.methodName, methodNameField!!) },
            )

            is LCISettings -> if (settings.mappingFile.isNotEmpty()) {
                listOf({ LcaImportDialog.validateRegularFile(settings.mappingFile, mappingFileField!!) })
            } else listOf()
        }

        return (genericValidations + specificValidations).firstNotNullOfOrNull { it.invoke() }
    }

}