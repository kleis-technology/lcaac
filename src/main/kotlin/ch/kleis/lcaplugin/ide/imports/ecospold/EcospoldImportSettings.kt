package ch.kleis.lcaplugin.ide.imports.ecospold

import ch.kleis.lcaplugin.imports.ecospold.EcospoldLibraryType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.xmlb.XmlSerializerUtil


@State(name = "EcospoldImportSettings", storages = [Storage("EcospoldImportSettings.xml")], reportStatistic = false)
class EcospoldImportSettings : PersistentStateComponent<EcospoldImportSettings.State> {
    companion object {
        @JvmStatic
        val instance: EcospoldImportSettings
            get() = ApplicationManager.getApplication().getService(EcospoldImportSettings::class.java)
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

    var libraryType: EcospoldLibraryType
        get() = state.LIBRARY_TYPE
        set(value) {
            state.LIBRARY_TYPE = value
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
        var LIBRARY_TYPE: EcospoldLibraryType = EcospoldLibraryType.LCI

        @JvmField
        var ROOT_FOLDER: String = ProjectManager.getInstance().openProjects.firstOrNull()?.basePath ?: ""

        @JvmField
        var IMPORT_UNITS: Boolean = true

        @JvmField
        var METHOD_NAME: String = ""

    }
}