package ch.kleis.lcaplugin.ide.template

class LcaTemplateFixture {
    companion object {
        val fileWithLiveTemplateInRoot = """
process p {
    products {
        1 kg carrot
    }
    inputs {
        1 l water from water_production match (geo = "FR")
    }
}

proc
""".trimIndent()

        val fileWithLiveTemplateInProcess = """
process p {
    proc
    products {
        1 kg carrot
    }
    inputs {
        1 l water from water_production match (geo = "FR")
    }
}


""".trimIndent()
    }
}