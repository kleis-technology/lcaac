package ch.kleis.lcaplugin.imports.simapro

import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.Renderer
import org.openlca.simapro.csv.process.ProcessBlock

class ProcessRenderer : Renderer<ProcessBlock> {
    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun render(process: ProcessBlock, writer: ModelWriter) {

        val name = process.name()

        val meta = mutableMapOf<String, String>()

        writer.write(
            "",
            """

            process $name {
            
                meta {
                
                }

                // Product
                products {
                    1 piece kleis
                }
                // Coproduct
                products {
                    1 piece kleis
                }

                inputs {
                    1 piece ss2i from ss2i( n_employees = 7 person, surface = 150 m2 )
                }
                
                emissions {
                }

                resources {
                
                }
            }
            """.trimIndent()
        )
    }
}
