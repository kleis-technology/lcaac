package ch.kleis.lcaplugin.ide.imports.ecospold.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.xmlb.XmlSerializerUtil


@State(name = "EcospoldImportLCIASettings", storages = [Storage("EcospoldImportLCIASettings.xml")], reportStatistic = false)
class LCIASettings : EcospoldImportSettings, PersistentStateComponent<LCIASettings.State> {
    companion object {
        @JvmStatic
        val instance: LCIASettings
            get() = ApplicationManager.getApplication().getService(LCIASettings::class.java)
    }

    private val state: State = State()

    override var rootPackage: String
        get() = state.ROOT_PACKAGE
        set(value) {
            state.ROOT_PACKAGE = value
        }

    override var libraryFile: String
        get() = state.LIBRARY_FILE
        set(value) {
            state.LIBRARY_FILE = value
        }

    override var rootFolder: String
        get() = state.ROOT_FOLDER
        set(value) {
            state.ROOT_FOLDER = value
        }

    override var importUnits: Boolean
        get() = state.IMPORT_UNITS
        set(value) {
            state.IMPORT_UNITS = value
        }

    var methodName: String
        get() = state.METHOD_NAME
        set(value) {
            state.METHOD_NAME = value
        }

    override fun getState(): State = state

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    @Suppress("PropertyName")
    class State {
        @JvmField
        var ROOT_PACKAGE: String = ""

        @JvmField
        var LIBRARY_FILE: String = ""

        @JvmField
        var ROOT_FOLDER: String = ProjectManager.getInstance().openProjects.firstOrNull()?.basePath ?: ""

        @JvmField
        var IMPORT_UNITS: Boolean = true

        @JvmField
        var METHOD_NAME: String = ""
    }
}