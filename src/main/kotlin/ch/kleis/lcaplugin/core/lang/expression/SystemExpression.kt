package ch.kleis.lcaplugin.core.lang.expression

sealed interface SystemExpression : LcaExpression
data class ESystem(
    val processes: List<LcaProcessExpression>
) : SystemExpression
