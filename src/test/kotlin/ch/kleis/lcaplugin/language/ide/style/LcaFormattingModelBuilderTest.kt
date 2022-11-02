package ch.kleis.lcaplugin.language.ide.style

import com.intellij.psi.formatter.FormatterTestCase
import org.junit.Test


internal class LcaFormattingModelBuilderTest : FormatterTestCase() {
    override fun getBasePath(): String? {
        return null;
    }

    override fun getFileExtension(): String {
        return "lca"
    }

    @Test
    fun testShouldFormatProperly() {
        doTextTest("""
            process "elecricity" { 
                products {
                    - "nuclear" 1.3e10 kBq
                    - "power" 10 kg
                    - "plop"  1.3 ha
                }
            }
        """.trimIndent(), """
            process "elecricity" {
                products {
                    - "nuclear" 1.3e10 kBq
                    - "power" 10 kg
                    - "plop"  1.3 ha
                }
            }
        """.trimIndent())
    }

    @Test
    fun testShouldFormatEmpty() {
        doTextTest("""
            process "empty" {
            }
        """.trimIndent(), """
            process "empty" {
            }
        """.trimIndent())
    }

}
