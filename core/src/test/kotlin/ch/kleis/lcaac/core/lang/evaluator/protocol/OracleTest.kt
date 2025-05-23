package ch.kleis.lcaac.core.lang.evaluator.protocol

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.expression.EProcess
import ch.kleis.lcaac.core.lang.expression.EProcessTemplate
import ch.kleis.lcaac.core.lang.expression.EProductSpec
import ch.kleis.lcaac.core.lang.expression.ETechnoExchange
import ch.kleis.lcaac.core.lang.fixture.ImpactBlockFixture
import ch.kleis.lcaac.core.lang.fixture.ProductFixture
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture
import ch.kleis.lcaac.core.lang.register.ProcessKey
import ch.kleis.lcaac.core.lang.register.ProcessTemplateRegister
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test

class OracleTest {

    @Test
    fun cachedOracle() {
        // given
        val template = EProcessTemplate(
            body = EProcess(
                name = "eProcess",
                products = listOf(
                    ETechnoExchange(QuantityFixture.oneKilogram, ProductFixture.carrot)
                ),
                impacts = listOf(
                    ImpactBlockFixture.oneClimateChange
                ),
            )
        )
        val symbolTable = SymbolTable(
            processTemplates = ProcessTemplateRegister.from(mapOf(
                ProcessKey("eProcess") to template
            ))
        )
        val inner = spyk(BareOracle(symbolTable, BasicOperations, mockk()))
        val oracle = CachedOracle(inner = inner)
        val requests = setOf(
            ProductRequest(
                address = Address(0, 0),
                value = EProductSpec<BasicNumber>(
                    "carrot",
                )
            )
        )
        // when
        oracle.answer(requests)
        oracle.answer(requests)

        // then
        verify(exactly = 1) {
            inner.answerRequest(any())
        }
    }
}
