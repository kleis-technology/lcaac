package com.github.albanseurat.lcaplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task

class TestAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val N = 10
        val delta = 1000L
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Test action") {
            override fun run(indicator: ProgressIndicator) {
                indicator.fraction = 0.0
                indicator.text = "Reading..."
                Thread.sleep(delta)
                IntRange(0, N - 1).forEach {
                    ProgressManager.checkCanceled()
                    indicator.fraction = it.toDouble() / (2 * N).toDouble()
                    indicator.text = "${it}/${2 * N}"
                    runReadAction {
                        Thread.sleep(delta)
                    }
                }

                indicator.fraction = 0.5
                indicator.text = "Writing..."
                Thread.sleep(delta)
                IntRange(N, 2 * N - 1).forEach {
                    ProgressManager.checkCanceled()
                    indicator.fraction = it.toDouble() / (2 * N).toDouble()
                    indicator.text = "${it}/${2 * N}"
                    WriteCommandAction.writeCommandAction(project).run<Throwable> {
                        Thread.sleep(delta)
                    }
                }
            }
        })
    }
}
