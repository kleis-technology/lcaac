package ch.kleis.lcaac.cli.cmd

import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExplainCommandTest {
    private val fixture = arrayOf(
        "-s", "src/test/resources/explain/src",
        "-c", "src/test/resources/explain/lcaac.yaml",
    )

    private fun explain(vararg args: String): String {
        val result = ExplainCommand().test(arrayOf(*fixture, *args))
        assertEquals(0, result.statusCode, result.output)
        return result.output
    }

    @Test
    fun `result summary`() {
        val output = explain("assembly")
        assertContains(output, "1 p widget (reference product of [1] assembly):")
        assertContains(output, Regex("""climate_change\s+= 9.99375 kg CO2-Eq\s+\(9.99375 kg CO2-Eq/p\)"""))
        assertContains(output, Regex("""climate_change\s+= 3.33125 kg CO2-Eq"""))
    }

    @Test
    fun `every per-unit result matches the solver`() {
        val output = explain("assembly")
        assertFalse(output.contains("MISMATCH"), output)
        assertFalse(output.contains("<error"), output)
        assertTrue(output.contains("[ok: matches solver]"))
    }

    @Test
    fun `processes are numbered depth first and linked`() {
        val output = explain("assembly")
        assertContains(output, "[1] assembly\n  entry point")
        assertContains(output, "[2] electricity_supply\n  used by: [1]")
        assertContains(output, "[3] steel_mill\n  used by: [1]")
        assertContains(output, "supplier: [2] electricity_supply")
        assertContains(output, "supplier: none (end point of the system)")
    }

    @Test
    fun `parameters show where their value comes from`() {
        val output = explain("assembly")
        assertContains(output, "machine = \"m1\"   (default)")
        assertContains(output, "grade = \"A\"   (set by caller; default: \"B\")")
    }

    @Test
    fun `parameter overridden on the command line`() {
        val output = explain("assembly", "-D", "machine=m2")
        assertContains(output, "machine = \"m2\"   (set by caller; default: \"m1\")")
        assertContains(output, "source: machines (data/machines.csv) where id = \"m2\"")
    }

    @Test
    fun `variables are derived step by step from lookups`() {
        val output = explain("assembly")
        assertContains(
            output,
            """
            |    mach = lookup machines match id = machine
            |         = lookup machines match id = "m1"
            |         = {id: "m1", consumption: 2 kWh, runs: 3 u}
            |           source: machines (data/machines.csv) where id = "m1"
            """.trimMargin()
        )
        assertContains(
            output,
            """
            |    energy = mach.consumption * mach.runs / efficiency * (1 u + grid_loss)
            |           = 2 kWh * 3 u / (80 percent) * (1 u + 10 percent)
            |           = 0.0825 kWh.u².percent⁻¹  (= 8.25 kWh)
            """.trimMargin()
        )
    }

    @Test
    fun `sums list the rows they add up`() {
        val output = explain("assembly")
        assertContains(output, "source: parts (data/parts.csv) where machine_id = \"m1\": 2 row(s) summed")
        assertContains(output, "id = \"p1\": 1.5 kg x 2 u = 3 kg.u")
        assertContains(output, "id = \"p2\": 0.5 kg x 4 u = 2 kg.u")
    }

    @Test
    fun `sums are truncated by max-rows`() {
        val output = explain("assembly", "--max-rows", "1")
        assertContains(output, "id = \"p1\": 1.5 kg x 2 u = 3 kg.u")
        assertFalse(output.contains("id = \"p2\": 0.5 kg x 4 u"))
        assertContains(output, "... 1 more row(s), see --max-rows")
    }

    @Test
    fun `for_each blocks show each row with its variables`() {
        val output = explain("assembly")
        assertContains(output, "for_each part from parts match machine_id = machine")
        assertContains(output, "row #1: part = {id: \"p1\", machine_id: \"m1\", mass: 1.5 kg, count: 2 u}")
        assertContains(output, "        wrap = 10 percent * part.mass = 10 percent * 1.5 kg = 15 kg.percent  (= 0.15 kg)\n")
    }

    @Test
    fun `short derivations fit on one line`() {
        val output = explain("assembly")
        assertContains(output, "      amount = steel_mass = 5 kg.u  (= 5 kg)\n")
    }

    @Test
    fun `sum rows without primary key are identified by their text columns`() {
        val output = explain("assembly")
        // runs_log has no id column: rows are named by sku (machine_id is already in the filter)
        assertContains(output, "source: runs_log (data/runs_log.csv) where machine_id = \"m1\": 2 row(s) summed")
        assertContains(output, "  sku = \"sku-a\": 4 u\n")
        assertContains(output, "  sku = \"sku-b\": 6 u\n")
    }

    @Test
    fun `contribution table with allocation and unit conversions`() {
        val output = explain("assembly", "-i", "climate_change")
        assertContains(output, "allocated share: 75% (amounts below are already multiplied by it)")
        assertContains(output, Regex("""electricity \[2]\s+0.061875 kWh.u².percent⁻¹ \(= 6.1875 kWh\)\s+x 0.5 kg CO2-Eq/kWh\s+= 3.09375 kg CO2-Eq"""))
        assertContains(output, Regex("""steel \[3]\s+3.75 kg.u \(= 0.00375 ton\)\s+x 1800 kg CO2-Eq/ton\s+= 6.75 kg CO2-Eq"""))
        assertContains(output, Regex("""\[Emission] co2\(air\)\s+150 g \(= 0.15 kg\)\s+x 1 kg CO2-Eq/kg\s+= 0.15 kg CO2-Eq"""))
        assertContains(output, Regex("""per p widget\s+/ 1 p\s+= 9.99375 kg CO2-Eq/p   \[ok: matches solver]"""))
    }

    @Test
    fun `indicator filter`() {
        val output = explain("assembly", "-i", "climate_change")
        assertFalse(output.contains("packaging:"))
        assertFalse(output.contains("packaging  "))
    }

    @Test
    fun `targets with no contribution are grouped`() {
        val output = explain("assembly")
        assertContains(output, "no contribution to: packaging   [ok: matches solver]")
    }

    @Test
    fun `globals and substances are listed`() {
        val output = explain("assembly")
        assertContains(output, "GLOBAL PARAMETERS AND VARIABLES USED")
        assertContains(output, "(global parameter)\n  efficiency = 80 percent")
        assertContains(output, "(global variable)\n  grid_loss = 10 percent")
        assertContains(output, "SUBSTANCE CHARACTERIZATIONS\n  1 kg [Emission] co2(air)\n    -> 1 kg CO2-Eq climate_change")
    }

    @Test
    fun `global parameter overridden on the command line`() {
        val output = explain("assembly", "-G", "efficiency=50 percent")
        assertContains(output, "= 2 kWh * 3 u / (50 percent) * (1 u + 10 percent)")
        assertContains(output, "efficiency = 50 percent")
    }

    @Test
    fun `depth limits the processes detailed`() {
        val output = explain("assembly", "--depth", "0")
        assertContains(output, "[1] assembly")
        assertFalse(output.contains("[2] electricity_supply"))
        assertContains(output, "supplier: electricity_supply (not expanded, beyond --depth)")
        assertContains(output, "electricity (electricity_supply, not expanded)")
        assertFalse(output.contains("MISMATCH"))
    }

    @Test
    fun `simple chain from zip source`() {
        val result = ExplainCommand().test(arrayOf("-s", "src/test/resources/main.zip", "main"))
        assertEquals(0, result.statusCode, result.output)
        assertContains(result.output, "[1] main")
        assertContains(result.output, "[2] mill")
        assertContains(result.output, "[3] wheat")
        assertFalse(result.output.contains("MISMATCH"))
    }
}
