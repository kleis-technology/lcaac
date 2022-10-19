package com.github.albanseurat.lcaplugin.actions

import com.github.albanseurat.lcaplugin.LcaLanguage
import com.github.albanseurat.lcaplugin.services.ScsvProcessBlockStream
import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.ide.IdeBundle
import com.intellij.ide.actions.ElementCreator
import com.intellij.internal.statistic.collectors.fus.fileTypes.FileTypeUsageCounterCollector
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserDialog
import com.intellij.openapi.fileChooser.ex.FileChooserDialogImpl
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import java.io.File
import java.util.zip.GZIPInputStream

class ImportScsvFileAction : AnAction(), DumbAware {

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

                WriteCommandAction.writeCommandAction(project).run<RuntimeException> {
                    val scsvFileMap: HashMap<String, ArrayList<PsiFile>> = HashMap()

                    ScsvProcessBlockStream {
                        val contentResources = if (it.resources().size > 0) {
                            val prefix = "resources {\n"
                            val inner = it.resources().map { r ->
                                "  - \"${r.name()}\" ${r.amount()} ${r.unit()}\n"
                            }.reduce { a, s -> a + s }
                            val suffix = "}\n"
                            prefix + inner + suffix
                        } else {
                            ""
                        }
                        val contentProducts = if (it.products().size > 0) {
                            val prefix = "products {\n"
                            val inner = it.products().map { p ->
                                "  - \"${p.name()}\" ${p.amount()} ${p.unit()}\n"
                            }.reduce { a, s -> a + s }
                            val suffix = "}\n"
                            prefix + inner + suffix
                        } else {
                            ""
                        }
                        val contentMeta = """
                            meta {
                                - category: "${it.category().name}"
                                - identifier: "${it.identifier()}"
                                - processType: "${it.processType()}"
                            }
                        """.trimIndent().plus("\n")
                        val content = """dataset "${it.name()}" {
                                                $contentProducts
                                                $contentResources
                                                $contentMeta
                                                }
                                                
                                                
                                    """.trimIndent().plus("\n\n")
                        val ds = PsiFileFactory.getInstance(project).createFileFromText(LcaLanguage.INSTANCE, content)
                        val key = it.category().name.lowercase()
                        if (key !in scsvFileMap.keys) {
                            scsvFileMap[key] = ArrayList()
                        }
                        scsvFileMap[key]?.add(ds)

                    }.read(GZIPInputStream(scsvFile.inputStream))

                    indicator.fraction = 0.5
                    indicator.text = "Writing files..."

                    val elementCreator = MyElementCreator(project, dir, "error")

                    scsvFileMap.keys.forEach { key ->
                        val elements = elementCreator.tryCreate("$key.lca")
                        val containerFile = elements[0].containingFile
                        scsvFileMap[key]?.forEach { ds ->
                            containerFile.node.addChildren(ds.firstChild.node, ds.lastChild.node, null)
                            containerFile.node.addLeaf(TokenType.NEW_LINE_INDENT, "", null)
                        }
                        ReformatCodeProcessor(containerFile, true).run()
                    }
                }

                indicator.fraction = 1.0
                indicator.text = "Done"
            }
        })
    }

    private class MyElementCreator(
        val project: Project,
        val directory: PsiDirectory,
        errorTitle: String
    ) : ElementCreator(project, errorTitle) {
        override fun create(newName: String): Array<PsiElement> {
            val file = WriteAction.compute<PsiFile, Exception> { directory.createFile(newName) }
            FileTypeUsageCounterCollector.triggerCreate(project, file.virtualFile)
            return arrayOf(file)
        }

        override fun getActionName(newName: String): String {
            return IdeBundle.message(
                "progress.creating.file",
                directory.virtualFile.presentableUrl,
                File.separator,
                newName
            )
        }
    }
}
