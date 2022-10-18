package com.github.albanseurat.lcaplugin.actions

import com.github.albanseurat.lcaplugin.LcaLanguage
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import org.openlca.simapro.csv.CsvHeader
import org.openlca.simapro.csv.SimaProCsv
import java.util.zip.GZIPInputStream

class ImportScsvFileBackgroundTask(project: Project, private val scsvFile: VirtualFile, private val containerFile: PsiElement): Task.Backgroundable(project, "Importing SCSV...")
{
    override fun run(indicator: ProgressIndicator) {
        val inputStream = GZIPInputStream(scsvFile.inputStream)
        val reader = SimaProCsv.readerOf(inputStream, SimaProCsv.defaultCharset())

        val header = CsvHeader.readFrom(reader)
        val datasets = SimaProCsv.read(header, reader)

        indicator.fraction = 0.0

        val it = datasets.processes()[0]

        val content = """
            dataset "${it.name()}" {
                    meta {
                        - category: "${it.category().name}"
                        - allocationRules: "${it.allocationRules()}"
                        - collectionMethod: "${it.collectionMethod()}"
                        - comment: "${it.comment()}"
                        - dataTreatment: "${it.dataTreatment()}"
                        - date: "${it.date()}"
                        - generator: "${it.generator()}"
                        - identifier: "${it.identifier()}"
                        - infrastructure: "${it.infrastructure()}"
                        - platformId: "${it.platformId()}"
                        - processType: ${it.processType()}
                        - record: "${it.record()}"
                        - status: "${it.status()}"
                        - systemDescription: "${it.systemDescription()}"
                        - verification: "${it.verification()}"
                    }
            }
        """.trimIndent()

        val ds = PsiFileFactory.getInstance(project).createFileFromText(LcaLanguage.INSTANCE, content)
        WriteCommandAction.writeCommandAction(project).run<RuntimeException> {
            containerFile.add(ds)
        }

        indicator.fraction = 1.0
    }
}
