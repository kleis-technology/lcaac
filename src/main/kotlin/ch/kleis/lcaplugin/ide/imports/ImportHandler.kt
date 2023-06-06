package ch.kleis.lcaplugin.ide.imports

import ch.kleis.lcaplugin.imports.Importer
import com.intellij.openapi.ui.ValidationInfo

interface ImportHandler {

    fun importer(): Importer
    fun doValidate(): ValidationInfo?
}