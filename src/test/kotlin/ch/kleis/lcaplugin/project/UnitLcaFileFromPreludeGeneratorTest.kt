package ch.kleis.lcaplugin.project

import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import org.junit.Test
import java.util.zip.ZipInputStream
import kotlin.io.path.Path
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class UnitLcaFileFromPreludeGeneratorTest {

    @Test
    fun recreate_shouldCreateJarWithTwoEntries() {
        // given
        val generator = UnitLcaFileFromPreludeGenerator<BasicNumber>()
        val path = Path("tmp.units.lca.jar")
        if (path.exists()) path.deleteExisting()

        // when
        generator.recreate(path)

        // then
        assertTrue(path.exists())
        ZipInputStream(path.toFile().inputStream()).use { jar ->
            val je = jar.nextEntry ?: fail("null entry")
            assertEquals("builtin_units.lca", je.name)

            val jeMd5 = jar.nextEntry ?: fail("null entry")
            assertEquals("builtin_units.lca.md5", jeMd5.name)
        }
    }
}
