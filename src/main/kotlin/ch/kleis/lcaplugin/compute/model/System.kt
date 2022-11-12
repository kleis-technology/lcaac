package ch.kleis.lcaplugin.compute.model

class System(private val processes: List<Process>) {
    private val processRegistry = HashMap<String, Process>()

    init {
        processes.forEach {
            processRegistry[it.name] = it
        }
    }

    fun getProcess(name: String): Process {
        return processRegistry[name] ?: throw NoSuchElementException(name)
    }
}
