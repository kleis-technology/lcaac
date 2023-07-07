package ch.kleis.lcaplugin.imports.ecospold.lcia

import ch.kleis.lcaplugin.imports.ecospold.EcoSpold2ProcessMapper
import ch.kleis.lcaplugin.imports.ecospold.model.ActivityDataset
import ch.kleis.lcaplugin.imports.model.ExchangeBlock
import ch.kleis.lcaplugin.imports.model.ImportedBioExchange

class LciaProcessMapper(process: ActivityDataset) : EcoSpold2ProcessMapper(process) {
    override fun mapEmission() {
        val bio = ImportedBioExchange(listOf(), "1.0", "u", pUid, "")
        result.emissionBlocks = mutableListOf(ExchangeBlock("Virtual Substance for Impact Factors", mutableListOf(bio)))
    }

}