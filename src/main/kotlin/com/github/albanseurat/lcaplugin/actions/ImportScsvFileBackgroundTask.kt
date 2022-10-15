package com.github.albanseurat.lcaplugin.actions

import com.github.albanseurat.lcaplugin.LcaLanguage
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.PsiFileFactoryImpl
import org.openlca.simapro.csv.CsvHeader
import org.openlca.simapro.csv.SimaProCsv
import java.util.zip.GZIPInputStream

class ImportScsvFileBackgroundTask(project: Project, val file: VirtualFile): Task.Backgroundable(project, "Importing SCSV...")
{
    override fun run(indicator: ProgressIndicator) {
        val inputStream = GZIPInputStream(file.inputStream)
        val reader = SimaProCsv.readerOf(inputStream, SimaProCsv.defaultCharset())

        val header = CsvHeader.readFrom(reader)
        val datasets = SimaProCsv.read(header, reader)

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

        val psiFileFactory: PsiFileFactory = PsiFileFactoryImpl(project)
        psiFileFactory.createFileFromText("import.lca", LcaLanguage.INSTANCE, content)
    }
}
