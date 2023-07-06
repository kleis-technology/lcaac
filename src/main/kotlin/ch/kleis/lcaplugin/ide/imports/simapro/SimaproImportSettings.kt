package ch.kleis.lcaplugin.ide.imports.simapro

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.xmlb.XmlSerializerUtil

enum class SubstanceImportMode { SIMAPRO, EF30, EF31, NOTHING }

@State(name = "SimaproImportSettings", storages = [Storage("SimaproImportSettings.xml")], reportStatistic = false)
class SimaproImportSettings : PersistentStateComponent<SimaproImportSettings.State> {
    companion object {
        @JvmStatic
        val instance: SimaproImportSettings
            get() = ApplicationManager.getApplication().getService(SimaproImportSettings::class.java)
    }

    private val state: State = State()

    var rootPackage: String
        get() = state.ROOT_PACKAGE
        set(value) {
            state.ROOT_PACKAGE = value
        }

    var libraryFile: String
        get() = state.LIBRARY_FILE
        set(value) {
            state.LIBRARY_FILE = value
        }

    var rootFolder: String
        get() = state.ROOT_FOLDER
        set(value) {
            state.ROOT_FOLDER = value
        }

    var importUnits: Boolean
        get() = state.IMPORT_UNITS
        set(value) {
            state.IMPORT_UNITS = value
        }

    var importProcesses: Boolean
        get() = state.IMPORT_PROCESSES
        set(value) {
            state.IMPORT_PROCESSES = value
        }
    var importSubstancesMode: SubstanceImportMode
        get() = state.IMPORT_SUBSTANCES_MODE
        set(value) {
            state.IMPORT_SUBSTANCES_MODE = value
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
        var IMPORT_PROCESSES: Boolean = true

        @JvmField
        var IMPORT_SUBSTANCES_MODE: SubstanceImportMode = SubstanceImportMode.SIMAPRO
    }
}