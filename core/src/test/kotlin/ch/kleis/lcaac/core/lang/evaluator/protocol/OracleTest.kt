package ch.kleis.lcaac.core.lang.evaluator.protocol

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.expression.EProcess
import ch.kleis.lcaac.core.lang.expression.EProcessTemplate
import ch.kleis.lcaac.core.lang.expression.EProductSpec
import ch.kleis.lcaac.core.lang.expression.ETechnoExchange
import ch.kleis.lcaac.core.lang.expression.ProcessAnnotation.CACHED
import ch.kleis.lcaac.core.lang.fixture.ImpactBlockFixture
import ch.kleis.lcaac.core.lang.fixture.ProductFixture
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture
import ch.kleis.lcaac.core.lang.register.ProcessKey
import ch.kleis.lcaac.core.lang.register.ProcessTemplateRegister
import ch.kleis.lcaac.core.lang.resolver.BareProcessResolver
import ch.kleis.lcaac.core.lang.resolver.CachedProcessResolver
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class OracleTest {
    @Nested
    inner class AnswerProductRequest {
        private val spec = EProductSpec<BasicNumber>("carrot")
        private val request = ProductRequest(Address(0, 0), spec)
        private val mockProcess = mockk<EProcess<BasicNumber>>(relaxed = true)

        @Test
        fun `should use CachedProcessResolver when template has CACHED annotation`() {
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
                ),
                annotations = setOf(CACHED)
            )
            val symbolTable = SymbolTable(
                processTemplates = ProcessTemplateRegister.from(mapOf(
                    ProcessKey("eProcess") to template
                ))
            )
            val oracle = spyk(Oracle(symbolTable, BasicOperations, mockk()))

            mockkConstructor(CachedProcessResolver::class)
            every {
                anyConstructed<CachedProcessResolver<BasicNumber, String>>().resolve(template, spec)
            } returns mockProcess

            // when
            val result = oracle.answerRequest(request)

            // then
            assertTrue(result is ProductResponse<BasicNumber>)
            verify {
                anyConstructed<CachedProcessResolver<BasicNumber, String>>()
                    .resolve(template, spec)
            }
        }

        @Test
        fun `should use BareProcessResolver when template has no CACHED annotation`() {
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
                ),
                annotations = setOf()
            )
            val symbolTable = SymbolTable(
                processTemplates = ProcessTemplateRegister.from(mapOf(
                    ProcessKey("eProcess") to template
                ))
            )
            val oracle = spyk(Oracle(symbolTable, BasicOperations, mockk()))

            mockkConstructor(BareProcessResolver::class)
            every {
                anyConstructed<BareProcessResolver<BasicNumber, String>>().resolve(template, spec)
            } returns mockProcess

            // when
            val result = oracle.answerRequest(request)

            // then
            assertTrue(result is ProductResponse<BasicNumber>)
            verify {
                anyConstructed<BareProcessResolver<BasicNumber, String>>()
                    .resolve(template, spec)
            }
        }
    }
}
