package ch.kleis.lcaplugin.imports

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException

/**
 * This class is in charge to convert formula from simapro to LCA As Code
 * In simapro variable are only without unit (u for LCA As Code), and LCA as code only support operation on quantity
 * ie ( number with a unit).
 *
 * The unit is not pass to this evaluator but will be directly add to the end of the result.
 * If we return something like "1.2" it will be added to create a resource exchange like "1.2 kg co2"
 *
 * We have to deal with 3 situations
 *  - only number : we return the number, and it will be ok like "1.2 kg co2"
 *  - a formula without variables: it could be full simplify by groovy and return a number: "2 * 3" will produce
 *  at the end "6 kg co2"
 *  - a formula with variables: we can't use groovy anymore, we add a 'u' to all numbers and put parenthesis around them
 *  to multiply by 1. The one number will be put in front of the unit to create a quantity.
 *  For exemple: "0.1486*LUC_crop_specific" will be return as "(0.1486 u * LUC_crop_specific) * 1"
 *  At the end the full resource line will be "(0.1486 u * LUC_crop_specific) * 1 kg co2" that is a valid exchange
 */
class FormulaConverter {
    companion object {
        private val onlyScientific = Regex("^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$")
        private val scientificCapture = Regex("([-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)")
        private val operatorsCapture = Regex("([()+\\-*/^=<>!&|])")
        private val multipleSpacesCapture = Regex("(\\ +)")
        private val exponentCapture = Regex("\\ *\\^\\ *([0-9]+\\.?[0-9]*)\\ *u")
        private val power10WithSpaceCapture = Regex("([0-9]+)E\\ *([+\\-]*)\\ *")
        private val engine: ScriptEngine

        init {
            val mgr = ScriptEngineManager()
            engine = mgr.getEngineByName("Groovy")
        }


        fun tryToCompute(amountFormula: String): Pair<String, Boolean> {

            val compute = try {
                if (onlyScientific.matches(amountFormula)) {
                    amountFormula
                } else {
                    engine.eval(amountFormula).toString()
                }
            } catch (e: ScriptException) {
                val withUnit = amountFormula.replace(scientificCapture, "$1 u ")
                val withSpaces = withUnit.replace(operatorsCapture, " $1 ")
                val withoutMultipleSpace = withSpaces.replace(multipleSpacesCapture, " ").trim()
                val withoutUnitInExponent = withoutMultipleSpace.replace(exponentCapture, "^$1")
                val withoutSpacesAroundPowerTen = withoutUnitInExponent.replace(power10WithSpaceCapture, "$1E$2")
                "( $withoutSpacesAroundPowerTen ) * 1"
            }
            return Pair(compute, compute != amountFormula)
        }
    }

}