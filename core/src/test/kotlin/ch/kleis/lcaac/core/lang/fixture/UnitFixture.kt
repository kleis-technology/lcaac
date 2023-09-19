package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.expression.EUnitLiteral
import ch.kleis.lcaac.core.math.basic.BasicNumber

object DimensionFixture {
    val mass = Dimension.of("mass")
    val length = Dimension.of("length")
    val time = Dimension.of("time")
    val volume = length.pow(3.0)
}

object UnitFixture {
    val unit = EUnitLiteral<BasicNumber>(UnitSymbol.of("unit"), 1.0, Dimension.None)
    val kg = EUnitLiteral<BasicNumber>(UnitSymbol.of("kg"), 1.0, DimensionFixture.mass)
    val g = EUnitLiteral<BasicNumber>(UnitSymbol.of("g"), 1.0e-3, DimensionFixture.mass)
    val m = EUnitLiteral<BasicNumber>(UnitSymbol.of("m"), 1.0, DimensionFixture.length)
    val km = EUnitLiteral<BasicNumber>(UnitSymbol.of("km"), 1000.0, DimensionFixture.length)
    val person = EUnitLiteral<BasicNumber>(UnitSymbol.of("person"), 1.0, Dimension.None)
    val l = EUnitLiteral<BasicNumber>(UnitSymbol.of("l"), 1.0e-3, DimensionFixture.volume)
    val s = EUnitLiteral<BasicNumber>(UnitSymbol.of("s"), 1.0, DimensionFixture.time)
    val hour = EUnitLiteral<BasicNumber>(UnitSymbol.of("hour"), 3600.0, DimensionFixture.time)
    val percent = EUnitLiteral<BasicNumber>(UnitSymbol.of("percent"), 1.0e-2, Dimension.None)

    val basicUnits = """
        package builtin_units
        
        unit kg {
            symbol = "kg"
            dimension = "mass"
        }
        unit g {
            symbol = "g"
            alias_for = 0.001 kg
        }
        unit ton {
            symbol = "ton"
            alias_for = 1000.0 kg
        }
        unit km {
            symbol = "km"
            alias_for = 1000.0 m
        }
        unit m {
            symbol = "m"
            dimension = "length"
        }
        unit m2 {
            symbol = "m2"
            alias_for = m^2
        }
        unit m3 {
            symbol = "m3"
            alias_for = m^3
        }
        unit l {
            symbol = "l"
            alias_for = 0.001 m3
        }
        unit u {
            symbol = "u"
            dimension = "none"
        }
        unit piece {
            symbol = "piece"
            alias_for = 1.0 u
        }
        unit percent {
            symbol = "percent"
            alias_for = 0.01 u
        }
    """.trimIndent()
}
