package ch.kleis.lcaplugin.services

import org.junit.Test
import org.openlca.simapro.csv.Numeric
import org.openlca.simapro.csv.enums.ProcessCategory
import org.openlca.simapro.csv.enums.ProcessType
import org.openlca.simapro.csv.process.ProcessBlock
import org.openlca.simapro.csv.process.ProductOutputRow

internal class ScsvProcessBlockFormatterTest {

    @Test
    fun format() {
        // given
        val processBlock = ProcessBlock()
            .name("process \"blabla\" name")
            .identifier("123456")
            .category(ProcessCategory.MATERIAL)
            .processType(ProcessType.UNIT_PROCESS)
        val products = processBlock.products()
        products.add(ProductOutputRow()
            .name("reference product")
            .amount(Numeric.of(1.0))
            .unit("kg"))
        val formatter = ScsvProcessBlockFormatter()

        // when
        val actual = formatter.format(processBlock)

        // then
        val expected = """
            process "process \"blabla\" name" {
                products {
                    - "reference product" 1.0 kg
                }
                inputs {
                }
                emissions {
                }
                resources {
                }
                meta {
                    - identifier: "123456"
                    - category: "material"
                    - processType: "Unit process"
                }
            }
        """.trimIndent()
        assert(expected == actual)
    }
}
