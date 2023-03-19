package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.testFramework.ParsingTestCase
import junit.framework.TestCase
import org.junit.Assert.*
import org.junit.Test
import org.hamcrest.CoreMatchers

class LcaBioExchangeDocumentationProviderTest : ParsingTestCase("", "lca", LcaParserDefinition()) {

    @Test
    fun testSubstance_ShouldRender_WithMinimumInfo(){
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
        TestCase.assertEquals("""
            <div class='definition'><pre>
            <span style="color:#ffc800;font-style:italic;">Substance </span><span style="color:#0000ff;font-weight:bold;">co2</span>
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
            
        """.trimIndent(),result)
    }

    @Test
    fun testSubstance_ShouldRender_WithAllInfos(){
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
        TestCase.assertEquals("""
        <div class='definition'><pre>
        <span style="color:#ffc800;font-style:italic;">Substance </span><span style="color:#0000ff;font-weight:bold;">propanol_air</span>
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
     
        """.trimIndent(),result)
    }

    override fun getTestDataPath(): String {
        return ""
    }
}