package ch.kleis.lcaplugin.imports.util

interface AsynchronousWatcher {

    fun notifyProgress(percent: Int)
    fun notifyCurrentWork(current: String)
}