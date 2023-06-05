package ch.kleis.lcaplugin.ide.imports.simapro

import ch.kleis.lcaplugin.MyBundle
import ch.kleis.lcaplugin.ide.component.ComponentFactory
import ch.kleis.lcaplugin.ide.component.ComponentFactory.Companion.createTextComponent
import ch.kleis.lcaplugin.ide.imports.ImportHandler
import ch.kleis.lcaplugin.ide.imports.LcaImportDialog
import ch.kleis.lcaplugin.imports.Importer
import ch.kleis.lcaplugin.imports.simapro.SimaproImporter
import com.intellij.BundleBase
import com.intellij.openapi.ui.CheckBoxWithDescription
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBRadioButton
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import java.awt.event.ItemEvent
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JPanel

class SimaproImportSettingsPanel(private val settings: SimaproImportSettings) : JPanel(VerticalFlowLayout()),
    ImportHandler {

    private val libField: JComponent
    private val packageField: JComponent

    init {
        val builder = FormBuilder()
        val locComp = ComponentFactory.createLocationComponent(
            { settings.rootFolder },
            { s: String -> settings.rootFolder = s })
        builder.addLabeledComponent(locComp.label, locComp.component)
        val packCom = createTextComponent(
            "lca.dialog.import.package.label",
            { settings.rootPackage },
            { s: String -> settings.rootPackage = s }
        )
        packageField = packCom.component
        builder.addLabeledComponent(packCom.label, packCom.component)
        val libComp = ComponentFactory.createLibraryFileComponent(
            { settings.libraryFile },
            { s: String -> settings.libraryFile = s })
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
        val nothingMsg =
            BundleBase.replaceMnemonicAmpersand(MyBundle.message("lca.dialog.import.substances.desc.nothing"))
        val ef30Msg = BundleBase.replaceMnemonicAmpersand(MyBundle.message("lca.dialog.import.substances.desc.ef30"))
        val ef31Msg = BundleBase.replaceMnemonicAmpersand(MyBundle.message("lca.dialog.import.substances.desc.ef31"))
        val rSimapro = JBRadioButton(simaproMsg, settings.importSubstancesMode == SubstanceImportMode.SIMAPRO)
        val rNothing = JBRadioButton(nothingMsg, settings.importSubstancesMode == SubstanceImportMode.NOTHING)
        val rEf30 = JBRadioButton(ef30Msg, settings.importSubstancesMode == SubstanceImportMode.EF30)
        val rEf31 = JBRadioButton(ef31Msg, settings.importSubstancesMode == SubstanceImportMode.EF31)
        val group = ButtonGroup()
        group.add(rNothing)
        group.add(rSimapro)
        group.add(rEf30)
        group.add(rEf31)
        rNothing.addActionListener {
            settings.importSubstancesMode = SubstanceImportMode.NOTHING
        }
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
            .apply { add(rNothing) }
            .apply { add(rSimapro) }
            .apply { add(rEf30) }
            .apply { add(rEf31) }
        return LabeledComponent.create(
            panel,
            BundleBase.replaceMnemonicAmpersand(MyBundle.message("lca.dialog.import.substances.label")),
            BorderLayout.WEST
        )
    }


    override fun importer(): Importer {
        return SimaproImporter(settings)
    }

    override fun doValidate(): ValidationInfo? {
        return listOf(
            { LcaImportDialog.validateRegularFile(settings.libraryFile, libField) },
            { LcaImportDialog.validatePackageIsValid(settings.rootPackage, packageField) })
            .firstNotNullOfOrNull { it.invoke() }
    }

}