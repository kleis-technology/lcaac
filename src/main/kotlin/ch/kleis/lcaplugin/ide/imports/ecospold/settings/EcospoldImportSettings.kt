package ch.kleis.lcaplugin.ide.imports.ecospold.settings

sealed interface EcospoldImportSettings {
    var rootPackage: String
    var libraryFile: String
    var rootFolder: String
    var importUnits: Boolean
}