package ch.kleis.lcaplugin.ui.toolwindow

import ch.kleis.lcaplugin.core.lang.evaluator.toUnitValue
import ch.kleis.lcaplugin.core.lang.evaluator.toValue
import ch.kleis.lcaplugin.core.lang.fixture.ProductFixture
import ch.kleis.lcaplugin.core.lang.fixture.SubstanceFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnitFixture
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.lang.value.ProductValue
import ch.kleis.lcaplugin.core.matrix.IndexedCollection
import ch.kleis.lcaplugin.core.matrix.InventoryMatrix
import ch.kleis.lcaplugin.core.matrix.MatrixFixture
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBViewport
import com.intellij.ui.table.JBTable
import io.mockk.mockk
import org.hamcrest.CoreMatchers.containsString
import org.jdesktop.swingx.plaf.basic.core.BasicTransferable
import org.junit.Assert.assertThat
import org.junit.Test
import java.awt.datatransfer.DataFlavor

class LcaProcessAssessResultTest {

    @Test
    fun test_getContent_AndPaste() {
        // Given
        val p1 = ProductValue("carrot", UnitFixture.g.toUnitValue())
        val p2 = ProductValue("carrot_pack", UnitFixture.pack.toUnitValue())
        val substance = SubstanceFixture.propanol.toValue()
        val product = ProductFixture.water.toValue()

        val data = MatrixFixture.make(2, 2, arrayOf(1.0, 10.0, 1.0, 10.0))
        val inv = InventoryMatrix(
            IndexedCollection(listOf(p1, p2)), IndexedCollection(listOf(substance, product)), data
        )

        val lcaProcessAssessResult = LcaProcessAssessResult(
            inv,
            Comparator.comparing { it.getUID() },
            mockk(),
            "name"
        )
        val panel = lcaProcessAssessResult.getContent()
        val scrollPanel = panel.getComponent(0) as JBScrollPane
        val viewPort = scrollPanel.getComponent(0) as JBViewport
        val table = viewPort.getComponent(0) as JBTable
        table.setRowSelectionInterval(0, 0)

        val sut = table.transferHandler as LcaProcessAssessResult.WithHeaderTransferableHandler

        // When
        val result = sut.createTransferable(table) as BasicTransferable

        // Then
        val html = result.getTransferData(DataFlavor("text/html;class=java.lang.String")) as String
        assertThat(html, containsString("<th>item</th>"))
        assertThat(html, containsString("<th>[Resource] propanol(air) [kg]</th>"))
        assertThat(html, containsString("<td>carrot</td>"))
        val text = result.getTransferData(DataFlavor("text/plain;class=java.lang.String")) as String
        assertThat(text, containsString("item\tquantity\t[Resource] propanol(air) [kg]"))
        assertThat(text, containsString("\ncarrot\t1 g\t0.001\t"))
    }

}
