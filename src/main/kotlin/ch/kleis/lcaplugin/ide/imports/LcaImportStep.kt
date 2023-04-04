package ch.kleis.lcaplugin.ide.imports

import com.intellij.openapi.actionSystem.DefaultActionGroup

class LcaImportStep : DefaultActionGroup(LcaImportPerformedAction()) {
//    , DumbAware, ActionsWithPanelProvider {


//    fun LcaImportStep() {
//        super(LcaImportPerformedAction())
//    }

//    protected class Customization : AbstractNewProjectStep.Customization<LcaImportSettings>() {
//        override fun createCallback(): AbstractCallback<LcaImportSettings> {
//            return LcaGenerateProjectCallback()
//        }
//
//        override fun createEmptyProjectGenerator(): DirectoryProjectGenerator<LcaImportSettings> {
//            return LcaBaseProjectGenerator()
//        }
//
//        override fun createProjectSpecificSettingsStep(
//            projectGenerator: DirectoryProjectGenerator<LcaImportSettings>,
//            callback: AbstractCallback<LcaImportSettings>
//        ): ProjectSettingsStepBase<LcaImportSettings> {
//            val npwGenerator: T = ObjectUtils.tryCast(
//                projectGenerator,
//                NewProjectWizardDirectoryGeneratorAdapter::class.java
//            )
//            return if (npwGenerator != null) {
//                NewProjectWizardProjectSettingsStep<LcaImportSettings>(npwGenerator)
//            } else {
//                LcaProjectSpecificSettingsStep(projectGenerator, callback)
//            }
//        }
//
//        override fun getActions(
//            generators: List<DirectoryProjectGenerator<*>?>,
//            callback: AbstractCallback<LcaImportSettings>
//        ): Array<AnAction> {
////            var generators = generators
////            generators = ArrayList(generators)
////            generators.sort(Comparator.comparing { obj: DirectoryProjectGenerator<*> -> obj.name })
////            generators.sort(Comparator.comparingInt<DirectoryProjectGenerator<*>> { value: DirectoryProjectGenerator<*>? ->
////                if (value is LcaAbstractProjectGenerator) {
////                    return@comparingInt -1
////                }
////                0
////            })
//            return EMPTY_ARRAY
//        }
//    }
}
