package ch.kleis.lcaplugin.imports.simapro

interface AsynchronousWatcher {

    fun notifyProgress(percent: Int)
    fun notifyCurrentWork(current: String)
}