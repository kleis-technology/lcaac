package ch.kleis.lcaplugin.imports

interface AsynchronousWatcher {

    fun notifyProgress(percent: Int)
    fun notifyCurrentWork(current: String)
}