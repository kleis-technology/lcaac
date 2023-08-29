package ch.kleis.lcaplugin.ui.toolwindow.contribution_analysis

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.xmlb.XmlSerializerUtil

enum class SubstanceImportMode { SIMAPRO, EF30, EF31, NOTHING }

@State(name = "SaveSettings", storages = [Storage("lcaSave.xml")], reportStatistic = false)
class SaveSettings : PersistentStateComponent<SaveSettings.State> {
    companion object {
        @JvmStatic
        val instance: SaveSettings
            get() = ApplicationManager.getApplication().getService(SaveSettings::class.java)
    }

    private val state: State = State()

    var saveFolder: String
        get() = state.SAVE_FOLDER
        set(value) {
            state.SAVE_FOLDER = value
        }

    var fileName: String
        get() = state.FILE_NAME
        set(value) {
            state.FILE_NAME = value
        }

    override fun getState(): State = state

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    @Suppress("PropertyName")
    class State {
        @JvmField
        var SAVE_FOLDER: String = "${ProjectManager.getInstance().openProjects[0].basePath ?: ""}/out"

        @JvmField
        var FILE_NAME: String = "computation_result.csv"


    }
}
