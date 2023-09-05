package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.core.assessment.ContributionAnalysis
import ch.kleis.lcaplugin.core.lang.evaluator.ToValue
import ch.kleis.lcaplugin.core.lang.fixture.ProductFixture
import ch.kleis.lcaplugin.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnitFixture
import ch.kleis.lcaplugin.core.lang.value.*
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.core.math.basic.BasicOperations
import ch.kleis.lcaplugin.core.math.basic.MatrixFixture
import ch.kleis.lcaplugin.core.matrix.ImpactFactorMatrix
import ch.kleis.lcaplugin.core.matrix.IndexedCollection
import ch.kleis.lcaplugin.core.matrix.IntensityMatrix
import ch.kleis.lcaplugin.ui.toolwindow.contribution_analysis.ContributionAnalysisWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBViewport
import com.intellij.ui.table.JBTable
import io.mockk.mockk
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.jdesktop.swingx.plaf.basic.core.BasicTransferable
import org.junit.Test
import java.awt.datatransfer.DataFlavor

class ContributionAnalysisWindowTest {
    private val ops = BasicOperations
    private val mapper = ToValue(BasicOperations)

    @Test
    fun test_getContent_AndPaste() {
        with(mapper) {
            // Given
            /*
                System
             */
            val carrot = ProductValue("carrot", UnitFixture.g.toUnitValue())
            val carrotPack = ProductValue("carrot_pack", UnitFixture.pack.toUnitValue())
            val propanol = SubstanceFixture.propanol.toValue()
            val water = ProductFixture.water.toValue()
            val pCarrot = ProcessValue(
                "pCarrot",
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneGram, carrot,
                    )
                )
            )
            val pCarrotPack = ProcessValue(
                "pCarrotPack",
                products = listOf(
                    TechnoExchangeValue(
                        QuantityValueFixture.oneUnit, carrotPack,
                    )
                )
            )
            val system = SystemValue(
                processes = setOf(pCarrot, pCarrotPack)
            )

            /*
                Contribution Analysis
             */
            val observablePorts = IndexedCollection<MatrixColumnIndex<BasicNumber>>(listOf(carrot, carrotPack))
            val controllablePorts = IndexedCollection(listOf(propanol, water))
            val connections = IndexedCollection<MatrixRowIndex<BasicNumber>>(listOf(pCarrot, pCarrotPack))
            val analysis = ContributionAnalysis(
                impactFactors = ImpactFactorMatrix(
                    observablePorts, controllablePorts,
                    MatrixFixture.basic(
                        2, 2, arrayOf(
                            1.0, 10.0,
                            1.0, 10.0,
                        )
                    ),
                    ops,
                ),
                intensity = IntensityMatrix(
                    connections,
                    MatrixFixture.basic(
                        1, 2, arrayOf(
                            1.0, 2.0,
                        )
                    ),
                    ops,
                ),
                system,
                BasicOperations,
            )

            /*
                Window
             */

            val window = ContributionAnalysisWindow(
                analysis,
                Comparator.comparing { it.getUID() },
                mockk(),
                "name"
            )
            val panel = window.getContent()
            val tablePane = panel.getComponent(1) as JBScrollPane
            val viewPort = tablePane.getComponent(0) as JBViewport
            val table = viewPort.getComponent(0) as JBTable
            table.setRowSelectionInterval(0, 0)

            val sut = table.transferHandler as WithHeaderTransferableHandler

            // When
            val result = sut.createTransferable(table) as BasicTransferable

            // Then
            val html = result.getTransferData(DataFlavor("text/html;class=java.lang.String")) as String
            assertThat(html, containsString("<th>item</th>"))
            assertThat(html, containsString("<th>[Resource] propanol(air) [kg]</th>"))
            assertThat(html, containsString("<td>carrot</td>"))
            val text = result.getTransferData(DataFlavor("text/plain;class=java.lang.String")) as String
            assertThat(text, containsString("item\tquantity\tunit\t[Resource] propanol(air) [kg]\twater [l]"))
            assertThat(text, containsString("\ncarrot\t1\tg\t1E-3\t10"))
        }
    }
}
