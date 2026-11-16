package ch.kleis.lcaac.cli.cmd

import com.github.ajalt.clikt.testing.test
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ExplainCommandTest {
    private val fixture = arrayOf("-s", "src/test/resources/explain/src", "-c", "src/test/resources/explain/lcaac.yaml")

    private fun explain(vararg args: String): String {
        val result = ExplainCommand().test(arrayOf(*fixture, *args))
        assertEquals(0, result.statusCode, result.output)
        return result.output
    }

    private fun explainModel(model: String, process: String): String {
        val root = createTempDirectory().toFile()
        File(root, "main.lca").writeText(model.trimIndent())
        val result = ExplainCommand().test(arrayOf("-s", root.path, process))
        assertEquals(0, result.statusCode, result.output)
        return result.output
    }

    @Test
    fun results() {
        val output = explain("assembly")
        assertContains(output, "1 p widget, reference product of [1] assembly:")
        assertContains(output, Regex("""climate_change\s+9.99375 kg CO2-Eq\s+\(9.99375 kg CO2-Eq/p\)"""))
        // The unit of packaging, kg.percent, is simplified
        assertContains(output, Regex("""packaging\s+0.15 kg\s+\(0.15 kg/p\)"""))
    }

    @Test
    fun everyTable_shouldMatchTheSolver() {
        val output = explain("assembly")
        assertFalse(output.contains("MISMATCH"), output)
        assertContains(output, "[ok: matches solver]")
    }

    @Test
    fun processes_shouldBeNumberedLinkedAndLocated() {
        val output = explain("assembly")
        assertContains(output, "[1] assembly   src/test/resources/explain/src/main.lca:37:1\n  entry point")
        assertContains(output, "[2] electricity_supply   src/test/resources/explain/src/main.lca:66:1\n  used by: [1]")
        assertContains(output, "energy electricity   -> [2] electricity_supply   (line 52)")
        assertContains(output, "wrap packaging   -> end point")
    }

    @Test
    fun parameters_shouldGiveTheirOrigin() {
        val output = explain("assembly")
        assertContains(output, "machine = \"m1\"   (default)   (line 39)")
        assertContains(output, "grade = \"A\"   (set by caller; default: \"B\")   (line 77)")
        assertContains(explain("assembly", "-D", "machine=m2"), "machine = \"m2\"   (set by caller; default: \"m1\")")
    }

    @Test
    fun variables_shouldBeDerivedStepByStep() {
        assertContains(
            explain("assembly"),
            """
            |    energy = mach.consumption * mach.runs / efficiency * (1 u + grid_loss)   (line 43)
            |           = 2 kWh * 3 u / (80 percent) * (1 u + 10 percent)
            |           = 0.0825 kWh.u².percent⁻¹ (= 8.25 kWh)
            """.trimMargin()
        )
    }

    @Test
    fun lookups_shouldGiveTheirSource() {
        assertContains(
            explain("assembly"),
            """
            |         = {id: "m1", consumption: 2 kWh, runs: 3 u}
            |           from machines (machines.csv) where id = "m1"
            """.trimMargin()
        )
    }

    @Test
    fun sums_shouldListTheirRows() {
        val output = explain("assembly")
        assertContains(output, "sum of 2 rows of parts (parts.csv) where machine_id = \"m1\":")
        assertContains(output, "id = \"p1\": 1.5 kg x 2 u")
        // runs_log has no id: its rows are named by their text columns that are not in the filter
        assertContains(output, "sku = \"sku-b\": 6 u")
    }

    @Test
    fun maxRows_shouldTruncateTheRows() {
        val output = explain("assembly", "--max-rows", "1")
        assertContains(output, "id = \"p1\": 1.5 kg x 2 u")
        assertFalse(output.contains("id = \"p2\": 0.5 kg x 4 u"))
        assertContains(output, "... 1 more row, see --max-rows")
    }

    @Test
    fun forEach_shouldShowEachRow() {
        val output = explain("assembly")
        assertContains(output, "read parts (parts.csv) where machine_id = \"m1\": 2 rows")
        assertContains(output, "row #2: part = {id: \"p2\", machine_id: \"m1\", mass: 0.5 kg, count: 4 u}")
        assertContains(output, "wrap = 10 percent * part.mass = 10 percent * 0.5 kg = 5 kg.percent (= 0.05 kg)   (line 56)")
    }

    @Test
    fun contributions_shouldGiveTheShareAndTellRowsApart() {
        val output = explain("assembly")
        assertContains(output, "contributions to 1 p widget (share 75%):")
        assertContains(output, Regex("""electricity \[2]\s+6.1875 kWh\s+x 0.5 kg CO2-Eq/kWh\s+= 3.09375 kg CO2-Eq"""))
        assertContains(output, Regex("""packaging \(end point\), row #2\s+0.0375 kg"""))
    }

    @Test
    fun depth_shouldLimitTheProcessesExplained() {
        val output = explain("assembly", "--depth", "0")
        assertFalse(output.contains("[2] electricity_supply"))
        assertContains(output, "-> electricity_supply (not explained, beyond --depth)")
        assertFalse(output.contains("MISMATCH"))
    }

    @Test
    fun indicators_shouldFilterTheTargets() {
        val output = explain("assembly", "-i", "climate_change")
        assertFalse(output.contains(Regex("""packaging\s+0.15 kg""")), output)
        assertFalse(output.contains("packaging:"))
        assertContains(output, Regex("""climate_change\s+9.99375 kg CO2-Eq"""))
    }

    @Test
    fun globals_shouldGiveTheirValueOrigin() {
        val output = explain("assembly", "-G", "efficiency=50 percent")
        assertContains(output, "GLOBALS\n  global 'efficiency' = 50 percent (given with -G)\n  grid_loss = 10 percent")
    }

    @Test
    fun loop_shouldBeNoted() {
        val output = explainModel(
            """
            process main {
                products {
                    1 p widget
                }
                inputs {
                    2 kg steel
                }
            }
            process steel_mill {
                products {
                    1 kg steel
                }
                inputs {
                    3 kWh electricity
                }
            }
            process power_plant {
                products {
                    1 kWh electricity
                }
                inputs {
                    0.1 kg steel
                }
            }
            """, "main"
        )
        assertContains(output, "[2] steel_mill")
        assertContains(output, "loop with [3]: their impacts per unit solve their tables together")
        assertFalse(output.contains("MISMATCH"))
    }

    @Test
    fun cachedProcess_shouldBeShownAggregated() {
        val output = explainModel(
            """
            process main {
                products {
                    1 p widget
                }
                inputs {
                    1 kg x
                }
            }
            @cached
            process cached {
                products {
                    1 kg x
                }
                inputs {
                    2 kg y
                }
            }
            """, "main"
        )
        assertContains(output, "@cached: evaluated as a separate system")
        assertContains(output, "2 kg y   -> end point")
    }
}
