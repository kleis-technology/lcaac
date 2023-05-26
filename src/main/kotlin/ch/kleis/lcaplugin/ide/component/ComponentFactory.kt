package ch.kleis.lcaplugin.ide.component

import ch.kleis.lcaplugin.MyBundle
import com.intellij.BundleBase
import com.intellij.ide.IdeBundle
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExtendableTextField
import java.awt.BorderLayout
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

class ComponentFactory {

    companion object {
        fun createLocationComponent(
            getter: () -> String,
            setter: (String) -> Unit
        ): LabeledComponent<TextFieldWithBrowseButton> {
            val myLocationField = TextFieldWithBrowseButton()
            val curProject = if (ProjectManager.getInstance().openProjects.isNotEmpty()) {
                ProjectManager.getInstance().openProjects[0].basePath ?: ""
            } else {
                ""
            }
            val root = getter.invoke()
            val myProjectDirectory = Path.of(root.ifEmpty { curProject })
            val projectLocation: String = myProjectDirectory.toString()
            myLocationField.text = projectLocation
            val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
            myLocationField.addBrowseFolderListener(
                "", "", null, descriptor
            )
            myLocationField.textField.addFocusListener(object : FocusAdapter() {
                override fun focusLost(e: FocusEvent?) {
                    setter.invoke(myLocationField.textField.text)
                }
            })

            return LabeledComponent.create(
                myLocationField,
                BundleBase.replaceMnemonicAmpersand(IdeBundle.message("directory.project.location.label")),
                BorderLayout.WEST
            )
        }

        fun createLibraryFileComponent(
            getter: () -> String,
            setter: (String) -> Unit
        ): LabeledComponent<TextFieldWithBrowseButton> {
            val myLocationField = TextFieldWithBrowseButton()
            val file = Path.of(getter.invoke())
            myLocationField.text = if (file.isRegularFile() && file.exists()) file.toString() else ""
            val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
            myLocationField.addBrowseFolderListener(
                MyBundle.message("lca.dialog.import.library.file.label"),
                MyBundle.message("lca.dialog.import.library.file.desc"), null, descriptor
            )
            myLocationField.textField.addFocusListener(object : FocusAdapter() {
                override fun focusLost(e: FocusEvent?) {
                    setter.invoke(myLocationField.textField.text)
                }
            })

            return LabeledComponent.create(
                myLocationField,
                BundleBase.replaceMnemonicAmpersand(MyBundle.message("lca.dialog.import.library.file.label")),
                BorderLayout.WEST
            )
        }

        fun createTextComponent(
            labelKey: String,
            getter: () -> String,
            setter: (String) -> Unit
        ): LabeledComponent<JBTextField> {
            val pack = object : ExtendableTextField(20) {
            }
            pack.text = getter.invoke()
            pack.addFocusListener(object : FocusAdapter() {
                override fun focusLost(e: FocusEvent?) {
                    setter.invoke(pack.text)
                }
            })

            return LabeledComponent.create(
                pack,
                BundleBase.replaceMnemonicAmpersand(MyBundle.message(labelKey)),
                BorderLayout.WEST
            )
        }

    }
}