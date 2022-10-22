package com.github.albanseurat.lcaplugin.actions

import com.github.albanseurat.lcaplugin.LcaLanguage
import com.github.albanseurat.lcaplugin.project.creators.DatasetDirectoryCreator
import com.github.albanseurat.lcaplugin.project.creators.DatasetFileCreator
import com.github.albanseurat.lcaplugin.services.ScsvProcessBlockFormatter
import com.github.albanseurat.lcaplugin.services.ScsvProcessBlockStream
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserDialog
import com.intellij.openapi.fileChooser.ex.FileChooserDialogImpl
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.psi.*
import java.util.zip.GZIPInputStream

class ImportScsvFileAction : AnAction() {
    val formatter = ScsvProcessBlockFormatter()

    override fun actionPerformed(e: AnActionEvent) {
        val view = e.getData(LangDataKeys.IDE_VIEW) ?: return
        val project = e.project ?: return
        val dir = view.orChooseDirectory ?: return

        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("gz")
        val fileChooserDialog: FileChooserDialog = FileChooserDialogImpl(descriptor, project)
        val scsvFile = fileChooserDialog.choose(project)[0]

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Importing SCSV file...") {
            override fun run(indicator: ProgressIndicator) {
                indicator.fraction = 0.0
                indicator.text = "Reading ..."

                val scsvFileMap: HashMap<String, ArrayList<PsiFile>> = HashMap()

                var processBlockCount = 0
                ScsvProcessBlockStream {
                    ProgressManager.checkCanceled()
                    val content = formatter.format(it)
                    val ds = runReadAction {
                        PsiFileFactory.getInstance(project).createFileFromText(LcaLanguage.INSTANCE, content)
                    }
                    val key = it.category().name.lowercase()
                    if (key !in scsvFileMap.keys) {
                        scsvFileMap[key] = ArrayList()
                    }
                    scsvFileMap[key]?.add(ds)

                    processBlockCount += 1
                    indicator.text = "Read $processBlockCount process blocks"
                }.read(GZIPInputStream(scsvFile.inputStream))


                indicator.fraction = 0.0
                indicator.text = "Writing files..."

                val subDirectoryCreator = DatasetDirectoryCreator(project, dir, "error")

                var datasetsCount = 0
                scsvFileMap.keys.forEach { key ->
                    val subdir = WriteCommandAction.writeCommandAction(project).compute<Array<PsiElement>, Throwable> {
                        subDirectoryCreator.tryCreate(key)
                    }[0] as PsiDirectory
                    val fileCreator = DatasetFileCreator(project, subdir, "error")
                    scsvFileMap[key]?.forEach { ds ->
                        ProgressManager.checkCanceled()
                        val elements = WriteCommandAction.writeCommandAction(project)
                            .compute<Array<PsiElement>, Throwable> {
                                fileCreator.tryCreate("${datasetsCount}.lca")
                            }
                        val containerFile = elements[0].containingFile
                        WriteCommandAction.writeCommandAction(containerFile, ds).run<Throwable> {
                            containerFile.node.addChild(ds.node.firstChildNode)
                            containerFile.node.addLeaf(TokenType.NEW_LINE_INDENT, "\n\n", null)
                        }
                        datasetsCount += 1
                        indicator.fraction = datasetsCount.toDouble() / processBlockCount.toDouble()
                        indicator.text = "Wrote $datasetsCount/$processBlockCount datasets"
                    }
                }

                indicator.fraction = 1.0
                indicator.text = "Done"
            }
        })
    }
}
