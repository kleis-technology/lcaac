package com.github.albanseurat.lcaplugin.language.ide.insight

import com.github.albanseurat.lcaplugin.LcaFileType
import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.util.ThrowableRunnable


class CreateDatasetAction(private val dataset: String, private val unitText: String?) : BaseIntentionAction() {

    override fun getText(): String {
        return "Create datataset '$dataset'"
    }

    override fun getFamilyName(): String {
        return "Create property"
    }

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        return true
    }


    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        WriteCommandAction.writeCommandAction(project).run(ThrowableRunnable<RuntimeException> {
            val newDataset = PsiFileFactory.getInstance(project)
                .createFileFromText(
                    "_Dummy_.${LcaFileType.INSTANCE.defaultExtension}",
                    LcaFileType.INSTANCE,
                    "\n\ndataset $dataset {\n products {\n - $dataset 1 $unitText\n}\n}\n"
                )
            file.node.addChildren(newDataset.firstChild.node, newDataset.lastChild.node, null);
            ReformatCodeProcessor(file, true).run()
            (newDataset.lastChild.navigationElement as Navigatable).navigate(true)
        })

    }
}