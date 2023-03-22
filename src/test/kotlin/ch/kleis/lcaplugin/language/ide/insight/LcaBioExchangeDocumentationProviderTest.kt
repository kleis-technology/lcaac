package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.psi.LcaProcess
import ch.kleis.lcaplugin.psi.LcaQuantityFactor
import ch.kleis.lcaplugin.psi.LcaQuantityTerm
import com.intellij.testFramework.ParsingTestCase
import com.jetbrains.rd.util.first
import junit.framework.TestCase
import org.junit.Assert.*
import org.junit.Test
import org.hamcrest.CoreMatchers

class LcaBioExchangeDocumentationProviderTest : ParsingTestCase("", "lca", LcaParserDefinition()) {

    @Test
    fun testSubstance_ShouldRender_WithMinimumInfo() {
        // Given
        val file = parseFile(
            "abc", """
            substance co2 {
                name = "co2"
                compartment = "compartment"
                reference_unit = kg
            }
        """.trimIndent()
        ) as LcaFile
        val substance = file.getSubstances().first()
        val sut = LcaBioExchangeDocumentationProvider()

        // When
        val result = sut.generateDoc(substance, substance)

        // Then
        TestCase.assertEquals(
            """
            <div class='definition'><pre>
            <span style="color:#ffc800;font-style:italic;">Substance</span> <span style="color:#0000ff;font-weight:bold;">co2</span>
            </pre></div>
            <div class='content'>
            <table class='sections'>
            <tr>
            <td valign='top' class='section'>Compartment</td>
            <td valign='top'>compartment</td>
            </tr>
            <tr>
            <td valign='top' class='section'>Reference Unit</td>
            <td valign='top'>kg</td>
            </tr>
            </table>
            </div>
            
        """.trimIndent(), result
        )
    }

    @Test
    fun testSubstance_ShouldRender_WithAllInfos() {
        // Given
        val file = parseFile(
            "abc", """
            substance propanol_air {
                name = "propanol"
                compartment = "air"
                sub_compartment = "high altitude"
                reference_unit = kg
                meta{
                    author = "Alain Colas"
                    description = "Propan-1-ol..."
                }
            }
        """.trimIndent()
        ) as LcaFile
        val substance = file.getSubstances().first()
        val sut = LcaBioExchangeDocumentationProvider()

        // When
        val result = sut.generateDoc(substance, substance)

        // Then
        TestCase.assertEquals(
            """
        <div class='definition'><pre>
        <span style="color:#ffc800;font-style:italic;">Substance</span> <span style="color:#0000ff;font-weight:bold;">propanol_air</span>
        </pre></div>
        <div class='content'>
        <span style="">Propan-1-ol...</span></div>
        <div class='content'>
        <table class='sections'>
        <tr>
        <td valign='top' class='section'>Author</td>
        <td valign='top'>Alain Colas</td>
        </tr>
        </table>
        </div>
        <div class='content'>
        <table class='sections'>
        <tr>
        <td valign='top' class='section'>Compartment</td>
        <td valign='top'>air</td>
        </tr>
        <tr>
        <td valign='top' class='section'>Sub-Compartment</td>
        <td valign='top'>high altitude</td>
        </tr>
        <tr>
        <td valign='top' class='section'>Reference Unit</td>
        <td valign='top'>kg</td>
        </tr>
        </table>
        </div>
     
        """.trimIndent(), result
        )
    }

    // TODO Implement when Unit Reference will be implemented
//    @Test
//    fun testUnit_ShouldRender() {
//        // Given
//        val file = parseFile(
//            "abc", """
//            process b {
//                params {
//                    yield = 100 g
//                }
//            }
//        """.trimIndent()
//        ) as LcaFile
//        val quantityExpression = file.getProcesses().first().getParameters().first().value.getTerm() as LcaQuantityTerm
//        val gUnit = (quantityExpression.children.first() as LcaQuantityFactor).quantityPrimitive.getUnit()
//        val sut = LcaBioExchangeDocumentationProvider()
//
//        // When
//        val result = sut.generateDoc(gUnit, gUnit)
//
//        // Then
//        TestCase.assertEquals(
//            """
//
//
//        """.trimIndent(), result
//        )
//    }

    @Test
    fun testProduct_ShouldRenderWithoutProcess() {
        // Given
        val file = parseFile(
            "abc", """
            process b {
                params {
                    p1 = 1 kg
                    p2 = p1 + p1
                }            
                products {
                    1 kg carrot
                }
            }
        """.trimIndent()
        ) as LcaFile
        val carrotInputExchange = file.getProcesses().first().getProducts().first()
        val sut = LcaBioExchangeDocumentationProvider()

        // When
        val result = sut.generateDoc(carrotInputExchange, carrotInputExchange)

        // Then
        TestCase.assertEquals(
            """
            <div class='definition'><pre>
            <span style="color:#ffc800;font-style:italic;">Product</span> <span style="color:#0000ff;font-weight:bold;">carrot</span><span style="color:#ffc800;font-style:italic;"> from </span><span style="color:#0000ff;font-weight:bold;">b</span>
            </pre></div>
            <div class='content'>
            <span style="color:#808080;font-style:italic;">Process Parameters:</span><table class='sections'>
            <tr>
            <td valign='top' class='section'>p1 = </td>
            <td valign='top'>1 kg</td>
            </tr>
            <tr>
            <td valign='top' class='section'>p2 = </td>
            <td valign='top'>p1 + p1</td>
            </tr>
            </table>
            </div>
            <div class='definition'><pre>
            </pre></div>
            
        """.trimIndent(), result
        )
    }
    @Test
    fun testProcess_ShouldRenderWithoutProcess() {
        // Given
        val file = parseFile(
            "abc", """
            process b {
                params {
                    p1 = 1 kg
                    p2 = p1 + p1
                }            
                products {
                    1 kg carrot
                }
            }
        """.trimIndent()
        ) as LcaFile
        val process = file.getProcesses().first() as LcaProcess
        val sut = LcaBioExchangeDocumentationProvider()

        // When
        val result = sut.generateDoc(process, process)

        // Then
        TestCase.assertEquals(
            """
        <div class='definition'><pre>
        <span style="color:#ffc800;font-style:italic;">Process</span> <span style="color:#0000ff;font-weight:bold;">b</span>
        </pre></div>
        <div class='content'>
        <span style="color:#808080;font-style:italic;">Process Parameters:</span><table class='sections'>
        <tr>
        <td valign='top' class='section'>p1 = </td>
        <td valign='top'>1 kg</td>
        </tr>
        <tr>
        <td valign='top' class='section'>p2 = </td>
        <td valign='top'>p1 + p1</td>
        </tr>
        </table>
        </div>
        <div class='definition'><pre>
        </pre></div>

        """.trimIndent(), result
        )
    }

  @Test
    fun testProcess_ShouldRenderWithProcessAndMeta() {
        // Given
        val file = parseFile(
            "abc", """
            process b {
                meta {
                    author = "Alain Colas"
                    description = "Propan-1-ol..."
                }
                params {
                    p1 = 1 kg
                    p2 = p1 + p1
                }            
                products {
                    1 kg carrot
                }
            }
        """.trimIndent()
        ) as LcaFile
        val process = file.getProcesses().first() as LcaProcess
        val sut = LcaBioExchangeDocumentationProvider()

        // When
        val result = sut.generateDoc(process, process)

        // Then
        TestCase.assertEquals(
            """
        <div class='definition'><pre>
        <span style="color:#ffc800;font-style:italic;">Process</span> <span style="color:#0000ff;font-weight:bold;">b</span>
        </pre></div>
        <div class='content'>
        <span style="">Propan-1-ol...</span></div>
        <div class='content'>
        <table class='sections'>
        <tr>
        <td valign='top' class='section'>Author</td>
        <td valign='top'>Alain Colas</td>
        </tr>
        </table>
        </div>
        <div class='content'>
        <span style="color:#808080;font-style:italic;">Process Parameters:</span><table class='sections'>
        <tr>
        <td valign='top' class='section'>p1 = </td>
        <td valign='top'>1 kg</td>
        </tr>
        <tr>
        <td valign='top' class='section'>p2 = </td>
        <td valign='top'>p1 + p1</td>
        </tr>
        </table>
        </div>
        <div class='definition'><pre>
        </pre></div>

        """.trimIndent(), result
        )
    }


    override fun getTestDataPath(): String {
        return ""
    }
}