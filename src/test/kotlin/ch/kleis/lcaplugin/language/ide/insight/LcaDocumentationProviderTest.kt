package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.psi.LcaProcess
import ch.kleis.lcaplugin.psi.LcaQuantityRef
import ch.kleis.lcaplugin.psi.LcaScaleQuantityExpression
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.rd.util.first
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LcaDocumentationProviderTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return ""
    }

    @Test
    fun testSubstance_ShouldRender_WithMinimumInfo() {
        // Given
        val virtualFile = myFixture.createFile(
            "testSubstance_ShouldRender_WithMinimumInfo.lca", """
            substance co2 {
                name = "co2"
                type = Resource
                compartment = "compartment"
                reference_unit = kg
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(virtualFile) as LcaFile
        val substance = file.getSubstances().first()
        val sut = LcaDocumentationProvider()

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
            <td valign='top' class='section'>Name</td>
            <td valign='top'>co2</td>
            </tr>
            <tr>
            <td valign='top' class='section'>Type</td>
            <td valign='top'>Resource</td>
            </tr>
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
        val virtualFile = myFixture.createFile(
            "testSubstance_ShouldRender_WithAllInfos.lca", """
            substance propanol_air {
                name = "propanol"
                type = Resource
                compartment = "air"
                sub_compartment = "high altitude"
                reference_unit = kg
                meta{
                    "author" = "Alain Colas"
                    "description" = "Propan-1-ol...
            with a return"
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(virtualFile) as LcaFile
        val substance = file.getSubstances().first()
        val sut = LcaDocumentationProvider()

        // When
        val result = sut.generateDoc(substance, substance)

        // Then
        TestCase.assertEquals(
            """
        <div class='definition'><pre>
        <span style="color:#ffc800;font-style:italic;">Substance</span> <span style="color:#0000ff;font-weight:bold;">propanol_air</span>
        </pre></div>
        <div class='content'>
        <table class='sections'>
        <tr>
        <td valign='top' class='section'>Name</td>
        <td valign='top'>propanol</td>
        </tr>
        <tr>
        <td valign='top' class='section'>Type</td>
        <td valign='top'>Resource</td>
        </tr>
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
        <div class='content'>
        <span style="">Propan-1-ol...<br>with a return</span></div>
        <div class='content'>
        <table class='sections'>
        <tr>
        <td valign='top' class='section'>Author</td>
        <td valign='top'>Alain Colas</td>
        </tr>
        </table>
        </div>
        
        """.trimIndent(), result
        )
    }

    @Test
    fun testQuantityRef_whenUnit_ShouldRender() {
        // Given
        val virtualFile = myFixture.createFile(
            "testQuantityRef_whenUnit_ShouldRender.lca", """
            process b {
                params {
                    yield = 100 g
                }
            }
            
            unit g {
                symbol = "g"
                dimension = "mass"
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(virtualFile) as LcaFile
        val assignment = file
            .getProcesses().first()
            .getParameters().first()
            .value as LcaScaleQuantityExpression
        val ref = assignment.quantityExpression!! as LcaQuantityRef
        val sut = LcaDocumentationProvider()

        // When
        val result = sut.generateDoc(ref, ref)

        // Then
        TestCase.assertEquals(
            """
        <div class='definition'><pre>
        <span style="color:#ffc800;font-style:italic;">Unit</span> <span style="color:#0000ff;font-weight:bold;">g</span>
        </pre></div>
        <div class='content'>
        <table class='sections'>
        <tr>
        <td valign='top' class='section'>Symbol</td>
        <td valign='top'>g</td>
        </tr>
        <tr>
        <td valign='top' class='section'>Dimension</td>
        <td valign='top'>mass</td>
        </tr>
        </table>
        </div>
        
        """.trimIndent(), result
        )
    }

    @Test
    fun testQuantityRef_whenUnitAlias_ShouldRender() {
        // Given
        val virtualFile = myFixture.createFile(
            "testQuantityRef_whenUnitAlias_ShouldRender.lca", """
            process b {
                params {
                    yield = 100 g
                }
            }
            
            unit g {
                symbol = "g"
                alias_for = 1e-3 kg
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(virtualFile) as LcaFile
        val assignment = file
            .getProcesses().first()
            .getParameters().first()
            .value as LcaScaleQuantityExpression
        val ref = assignment.quantityExpression!! as LcaQuantityRef
        val sut = LcaDocumentationProvider()

        // When
        val result = sut.generateDoc(ref, ref)

        // Then
        TestCase.assertEquals(
            """
        <div class='definition'><pre>
        <span style="color:#ffc800;font-style:italic;">Unit</span> <span style="color:#0000ff;font-weight:bold;">g</span>
        </pre></div>
        <div class='content'>
        <table class='sections'>
        <tr>
        <td valign='top' class='section'>Symbol</td>
        <td valign='top'>g</td>
        </tr>
        <tr>
        <td valign='top' class='section'>Alias for</td>
        <td valign='top'>1e-3 kg</td>
        </tr>
        </table>
        </div>
        
        """.trimIndent(), result
        )
    }

    @Test
    fun testQuantityRef_whenGlobalAssignment_ShouldRender() {
        // Given
        val virtualFile = myFixture.createFile(
            "testQuantityRef_whenGlobalAssignment_ShouldRender.lca", """
            variables {
                x = 1 kg
            }
            
            process b {
                params {
                    yield = 100 x
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(virtualFile) as LcaFile
        val assignment: LcaScaleQuantityExpression = file
            .getProcesses().first()
            .getParameters().first()
            .value as LcaScaleQuantityExpression
        val ref = assignment.quantityExpression!! as LcaQuantityRef
        val sut = LcaDocumentationProvider()

        // When
        val result = sut.generateDoc(ref, ref)

        // Then
        TestCase.assertEquals(
            """
        <div class='definition'><pre>
        <span style="color:#ffc800;font-style:italic;">Global quantity</span> <span style="color:#0000ff;font-weight:bold;">x</span>
        </pre></div>
        <div class='content'>
        <table class='sections'>
        <tr>
        <td valign='top' class='section'>Symbol</td>
        <td valign='top'>x</td>
        </tr>
        <tr>
        <td valign='top' class='section'>Value</td>
        <td valign='top'>1 kg</td>
        </tr>
        </table>
        </div>
        
        """.trimIndent(), result
        )
    }

    @Test
    fun testQuantityRef_whenLocalVariable_ShouldRender() {
        // Given
        val virtualFile = myFixture.createFile(
            "testQuantityRef_whenLocalVariable_ShouldRender.lca", """
            process b {
                variables {
                    x = 1 kg
                }
            
                params {
                    yield = 100 x
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(virtualFile) as LcaFile
        val assignment = file
            .getProcesses().first()
            .getParameters().first()
            .value as LcaScaleQuantityExpression
        val ref = assignment.quantityExpression!! as LcaQuantityRef
        val sut = LcaDocumentationProvider()

        // When
        val result = sut.generateDoc(ref, ref)

        // Then
        TestCase.assertEquals(
            """
        <div class='definition'><pre>
        <span style="color:#ffc800;font-style:italic;">Quantity</span> <span style="color:#0000ff;font-weight:bold;">x</span>
        </pre></div>
        <div class='content'>
        <table class='sections'>
        <tr>
        <td valign='top' class='section'>Symbol</td>
        <td valign='top'>x</td>
        </tr>
        <tr>
        <td valign='top' class='section'>Value</td>
        <td valign='top'>1 kg</td>
        </tr>
        </table>
        </div>
        
        """.trimIndent(), result
        )
    }

    @Test
    fun testQuantityRef_whenLocalParameter_ShouldRender() {
        // Given
        val virtualFile = myFixture.createFile(
            "testQuantityRef_whenLocalParameter_ShouldRender.lca", """
            process b {
                params {
                    x = 1 kg
                }
            
                params {
                    yield = 100 x
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(virtualFile) as LcaFile
        val assignment = file
            .getProcesses().first()
            .getParameters()["yield"]!! as LcaScaleQuantityExpression
        val ref = assignment.quantityExpression!! as LcaQuantityRef
        val sut = LcaDocumentationProvider()

        // When
        val result = sut.generateDoc(ref, ref)

        // Then
        TestCase.assertEquals(
            """
        <div class='definition'><pre>
        <span style="color:#ffc800;font-style:italic;">Quantity</span> <span style="color:#0000ff;font-weight:bold;">x</span>
        </pre></div>
        <div class='content'>
        <table class='sections'>
        <tr>
        <td valign='top' class='section'>Symbol</td>
        <td valign='top'>x</td>
        </tr>
        <tr>
        <td valign='top' class='section'>Value</td>
        <td valign='top'>1 kg</td>
        </tr>
        </table>
        </div>
        
        """.trimIndent(), result
        )
    }

    @Test
    fun testQuantityRef_whenArgument_ShouldRender() {
        // Given
        val virtualFile = myFixture.createFile(
            "testQuantityRef_whenArgument_ShouldRender.lca", """
            process a {
                inputs {
                    1 kg carrot from b(x = 3 kg)
                }
            }
            
            process b {
                params {
                    x = 1 kg
                }
                products {
                    1 kg carrot
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(virtualFile) as LcaFile
        val ref = file.getProcesses().first()
            .getInputs().first()
            .getFromProcessConstraint()!!
            .getPsiArguments().first()
            .getParameterRef()
        val sut = LcaDocumentationProvider()

        // When
        val result = sut.generateDoc(ref, ref)

        // Then
        TestCase.assertEquals(
            """
        <div class='definition'><pre>
        <span style="color:#ffc800;font-style:italic;">Quantity</span> <span style="color:#0000ff;font-weight:bold;">x</span>
        </pre></div>
        <div class='content'>
        <table class='sections'>
        <tr>
        <td valign='top' class='section'>Symbol</td>
        <td valign='top'>x</td>
        </tr>
        <tr>
        <td valign='top' class='section'>Value</td>
        <td valign='top'>1 kg</td>
        </tr>
        </table>
        </div>
        
        """.trimIndent(), result
        )
    }

    @Test
    fun testProduct_ShouldRenderWithoutProcess() {
        // Given
        val virtualFile = myFixture.createFile(
            "testProduct_ShouldRenderWithoutProcess.lca", """
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
        )
        val file = PsiManager.getInstance(project).findFile(virtualFile) as LcaFile
        val carrotInputExchange = file.getProcesses().first().getProducts().first()
        val sut = LcaDocumentationProvider()

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
        val virtualFile = myFixture.createFile(
            "testProcess_ShouldRenderWithoutProcess.lca", """
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
        )
        val file = PsiManager.getInstance(project).findFile(virtualFile) as LcaFile
        val process = file.getProcesses().first() as LcaProcess
        val sut = LcaDocumentationProvider()

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
        val virtualFile = myFixture.createFile(
            "testProcess_ShouldRenderWithProcessAndMeta.lca", """
            process b {
                meta {
                    "author" = "Alain Colas"
                    "description" = "Propan-1-ol..."
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
        )
        val file = PsiManager.getInstance(project).findFile(virtualFile) as LcaFile
        val process = file.getProcesses().first() as LcaProcess
        val sut = LcaDocumentationProvider()

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

    @Test
    fun test_technoExchangeWithAllocateField_ShouldRender() {
        // Given
        val virtualFile = myFixture.createFile(
            "test_technoExchangeWithAllocateField.lca", """
            process glyphosate {
                products {
                    1 kg glyphosate allocate 90 percent
                    1 kg waste allocate 10 percent
                }
            }
            
            process p {
                products {
                    1 kg p
                }
                inputs {
                    1 kg glyphosate
                }
            }
        """.trimIndent()
        )
        val file = PsiManager.getInstance(project).findFile(virtualFile) as LcaFile
        val ref = file.getProcesses().first().getProductsWithAllocation().first().getTechnoProductExchange()
        val sut = LcaDocumentationProvider()
        // When
        val actual = sut.generateDoc(ref, ref)
        // Then
        TestCase.assertEquals(
            """
            <div class='definition'><pre>
            <span style="color:#ffc800;font-style:italic;">Product</span> <span style="color:#0000ff;font-weight:bold;">glyphosate</span><span style="color:#ffc800;font-style:italic;"> from </span><span style="color:#0000ff;font-weight:bold;">glyphosate</span>
            </pre></div>
            <div class='content'>
            <span style="color:#808080;font-style:italic;">Process Parameters:</span><table class='sections'>
            </table>
            </div>
            <div class='definition'><pre>
            </pre></div>
            
            """.trimIndent(), actual
        )
    }
}
