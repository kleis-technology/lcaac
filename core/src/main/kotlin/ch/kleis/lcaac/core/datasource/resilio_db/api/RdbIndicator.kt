package ch.kleis.lcaac.core.datasource.resilio_db.api

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.prelude.Prelude

@Suppress("EnumEntryName")
enum class RdbIndicator(
    val rdbField: String,
) {
    ADPe("ADPe"),
    ADPf("ADPf"),
    AP("AP"),
    CTUe("CTUe"),
    CTUh_c("CTUh-c"),
    CTUh_nc("CTUh-nc"),
    Epf("Epf"),
    Epm("Epm"),
    Ept("Ept"),
    GWP("GWP"),
    GWPb("GWPb"),
    GWPf("GWPf"),
    GWPlu("GWPlu"),
    IR("IR"),
    LU("LU"),
    ODP("ODP"),
    PM("PM"),
    POCP("POCP"),
    WU("WU"),
    TPE("TPE");

    companion object {
        fun fromRdbField(rdbField: String): RdbIndicator? {
            return entries.firstOrNull {
                it.rdbField == rdbField
            }
        }
    }

    fun <Q> getUnit(): DataExpression<Q> = when(this) {
        ADPe -> Prelude.unitMap<Q>()["kg_Sb_Eq"]
        ADPf -> Prelude.unitMap<Q>()["MJ_net_calorific_value"]
        AP -> Prelude.unitMap<Q>()["mol_H_p_Eq"]
        CTUe -> Prelude.unitMap<Q>()["CTUe"]
        CTUh_c -> Prelude.unitMap<Q>()["CTUh"]
        CTUh_nc -> Prelude.unitMap<Q>()["CTUh"]
        Epf -> Prelude.unitMap<Q>()["kg_P_Eq"]
        Epm -> Prelude.unitMap<Q>()["kg_N_Eq"]
        Ept -> Prelude.unitMap<Q>()["mol_N_Eq"]
        GWP -> Prelude.unitMap<Q>()["kg_CO2_Eq"]
        GWPb -> Prelude.unitMap<Q>()["kg_CO2_Eq"]
        GWPf -> Prelude.unitMap<Q>()["kg_CO2_Eq"]
        GWPlu -> Prelude.unitMap<Q>()["kg_CO2_Eq"]
        IR -> Prelude.unitMap<Q>()["kBq_U235_Eq"]
        LU -> Prelude.unitMap<Q>()["u"]
        ODP -> Prelude.unitMap<Q>()["kg_CFC_11_Eq"]
        PM -> Prelude.unitMap<Q>()["disease_incidence"]
        POCP -> Prelude.unitMap<Q>()["kg_NMVOC_Eq"]
        WU -> Prelude.unitMap<Q>()["m3_world_eq_deprived"]
        TPE -> Prelude.unitMap<Q>()["MJ_net_calorific_value"]
    } ?: throw IllegalStateException()

}
