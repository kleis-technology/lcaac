package ch.kleis.lcaplugin.actions.csv

import ch.kleis.lcaplugin.core.lang.evaluator.ToValue
import ch.kleis.lcaplugin.core.lang.value.FromProcessRefValue
import ch.kleis.lcaplugin.core.lang.value.IndicatorValue
import ch.kleis.lcaplugin.core.lang.value.ProductValue
import ch.kleis.lcaplugin.core.lang.value.QuantityValue
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.core.math.basic.BasicOperations
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class CsvProcessorTest : BasePlatformTestCase() {
    private val ops = BasicOperations
    private val umap = with(ToValue(BasicOperations)) {
        Prelude.unitMap<BasicNumber>().map { it.value.toUnitValue() }
            .associateBy { it.symbol.toString() }
    }

    @Test
    fun test_multiProducts() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    products {
                        1 kg carrot allocate 60 percent
                        1 kg fanes allocate 40 percent
                    }
                    impacts {
                        1 kg cc
                        2 l water_use
                    }
                }
            """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)
        val symbolTable = parser.load()
        val processor = CsvProcessor(symbolTable)
        val cc = IndicatorValue(
            "cc", umap["kg"]!!,
        )
        val waterUse = IndicatorValue(
            "water_use", umap["l"]!!,
        )
        val request = CsvRequest(
            "p",
            emptyMap(),
            mapOf("scenario" to 0),
            listOf("s0"),
        )

        // when
        val actual = processor.process(request)

        // then
        assertEquals(2, actual.size)
        with(ops) {
            assertEquals(
                QuantityValue(pure(0.6), umap["kg"]!!),
                actual[0].impacts[cc]!!,
            )
            assertEquals(
                QuantityValue(pure(1.2), umap["l"]!!),
                actual[0].impacts[waterUse]!!,
            )
            assertEquals(
                QuantityValue(pure(0.4), umap["kg"]!!),
                actual[1].impacts[cc]!!,
            )
            assertEquals(
                QuantityValue(pure(0.8), umap["l"]!!),
                actual[1].impacts[waterUse]!!,
            )
        }
    }

    @Test
    fun test_csvProcessor() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val vf = myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    params {
                        a = 0 kg
                        b = 0 kg
                        c = 1 kg
                    }
                    products {
                        1 kg out
                    }
                    inputs {
                        a + b + c in
                    }
                }
            """.trimIndent()
        )
        val kg = umap["kg"]!!
        val file = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val parser = LcaLangAbstractParser(sequenceOf(file), ops)
        val symbolTable = parser.load()
        val csvProcessor = CsvProcessor(symbolTable)
        val request = CsvRequest(
            "p",
            emptyMap(),
            mapOf("geo" to 0, "id" to 1, "a" to 2, "b" to 2),
            listOf("UK", "s00", "1.0", "1.0"),
        )

        // when
        val actual = csvProcessor.process(request)[0]

        // then
        assertEquals(request, actual.request)
        val out = ProductValue(
            "out", kg,
            FromProcessRefValue(
                name = "p",
                arguments = mapOf(
                    "a" to QuantityValue(ops.pure(1.0), kg),
                    "b" to QuantityValue(ops.pure(1.0), kg),
                    "c" to QuantityValue(ops.pure(1.0), kg),
                )
            )
        )
        assertEquals(
            out, actual.output
        )
        val key = ProductValue(
            "in", kg,
        )
        assertEquals(
            QuantityValue(ops.pure(3.0), kg), actual.impacts[key]
        )
    }
}
