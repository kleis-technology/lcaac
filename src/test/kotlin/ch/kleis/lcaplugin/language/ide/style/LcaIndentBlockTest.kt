package ch.kleis.lcaplugin.language.ide.style

import com.intellij.psi.formatter.FormatterTestCase
import org.junit.Test

class LcaIndentBlockTest : FormatterTestCase() {


    @Test
    fun test_formattingHeader() {
        doTextTest(
            """
              package tt




       import toto
       
       
       variables {
    r = 3 kg
}""",
            """
                package tt
                
                
                import toto
                
                variables {
                    r = 3 kg
                }""".trimIndent()
        )

    }

    @Test
    fun test_formattingUnit() {
        doTextTest(
            """
 unit   pack{
  symbol = "pack"
        dimension = "none"


}""",
            """
                unit pack {
                    symbol = "pack"
                    dimension = "none"
                }
            """.trimIndent()
        )
    }

    @Test
    fun test_formattingProcessShouldPreserveNoLineBeforeParams() {
        doTextTest(
            """
 process   process_a{
    params {
        yield = 100 g
        yield2 = 100 g
    }
}""",
            """
                process process_a {
                    params {
                        yield = 100 g
                        yield2 = 100 g
                    }
                }
""".trimIndent()
        )
    }

    @Test
    fun test_formattingProcessShouldRemoveExcessiveLinesBeforeParams() {
        doTextTest(
            """
 process   process_a{
 
 
    params {
        yield = 100 g
        yield2 = 100 g
    }
}""",
            """
                process process_a {
                
                    params {
                        yield = 100 g
                        yield2 = 100 g
                    }
                }
                """.trimIndent()
        )
    }

    @Test
    fun test_formattingProcess() {
        doTextTest(
            """
 process   process_a{
 
 
  meta    {
 "description"   =   "My Desc"
 "author"="me"
}

    params {
           yield = 100 g
      yield2 = 100 g
    }





    variables{    q = 1 kg
    p = 3 kg
    }


        products {
          1 kg a
        }
        
        
  inputs {    1 kg carrot     from     farm
                2 kg co2
}


        emissions{
          1 kg co2
}



   resources{
     0.5 kg co2
  }
  
                        land_use{
        2ha lu
                  }
}


                    process farm {
                    
                        products {
                            1 kg carrot
                        }
                    }
""",
            """
                process process_a {
                
                    meta {
                        "description" = "My Desc"
                        "author" = "me"
                    }
                
                    params {
                        yield = 100 g
                        yield2 = 100 g
                    }
                
                    variables {
                        q = 1 kg
                        p = 3 kg
                    }
                
                    products {
                        1 kg a
                    }
                
                    inputs {
                        1 kg carrot from farm
                        2 kg co2
                    }
                
                    emissions {
                        1 kg co2
                    }
                
                    resources {
                        0.5 kg co2
                    }
                
                    land_use {
                        2 ha lu
                    }
                }
                
                process farm {
                
                    products {
                        1 kg carrot
                    }
                }
                
                """.trimIndent()
        )
    }

    @Test
    fun test_formattingProcessShouldFormatParams() {
        doTextTest(
            """process process_a {

    params {
        yield  =  100  g
        yield2=100 g
        yield3=yield+ 100 g
        yield4=yield  +  100  g
    }
}""", """
    process process_a {
    
        params {
            yield = 100 g
            yield2 = 100 g
            yield3 = yield + 100 g
            yield4 = yield + 100 g
        }
    }
    """.trimIndent()
        )
    }

    @Test
    fun test_formattingProcessShouldFormatFormulaAndAllocate() {
        doTextTest(
            """process process_a {

    params {
        yield  =  100  g
        yield2=100 g
        yield3=yield+100 g
        yield4=yield  +  100  g
    }

    variables {
        p  =  100  g
        p2=100 g
        p3=p+100 g
        p4=p2  +  100  g
        p5=5 piece
    }

    products {
        (  1  kg  +  p4  )    prod      allocate     50 percent
        (1kg+p2) co_prod allocate 25 percent
        (1kg*p2) co_prod allocate 25 percent
    }

    inputs {
        (  1  kg  +  p4  )  carrot
        (1 kg+p2) co2
    }

    emissions {
        (  1  kg  +  p4  ) co2
        (1 kg+p2)   co2
    }

    resources {
        (  1  kg  +  p4  )  /  2piece co2
        (1 kg+p2)/2piece co2
    }
}""",
            """
                process process_a {
                
                    params {
                        yield = 100 g
                        yield2 = 100 g
                        yield3 = yield + 100 g
                        yield4 = yield + 100 g
                    }
                
                    variables {
                        p = 100 g
                        p2 = 100 g
                        p3 = p + 100 g
                        p4 = p2 + 100 g
                        p5 = 5 piece
                    }
                
                    products {
                        ( 1 kg + p4 ) prod allocate 50 percent
                        ( 1 kg + p2 ) co_prod allocate 25 percent
                        ( 1 kg * p2 ) co_prod allocate 25 percent
                    }
                
                    inputs {
                        ( 1 kg + p4 ) carrot
                        ( 1 kg + p2 ) co2
                    }
                
                    emissions {
                        ( 1 kg + p4 ) co2
                        ( 1 kg + p2 ) co2
                    }
                
                    resources {
                        ( 1 kg + p4 ) / 2 piece co2
                        ( 1 kg + p2 ) / 2 piece co2
                    }
                }
                """.trimIndent()
        )
    }

    @Test
    fun test_formattingSubstance() {
        doTextTest(
            """  substance propanol {
        name= "propanol"
           type   =Emission
              compartment = "air"
                 sub_compartment = "null"
                   reference_unit = kg
                         impacts {
        1   kg    cc
                        1kg tox_hum
    }

  }""", """
        substance propanol {
            name = "propanol"
            type = Emission
            compartment = "air"
            sub_compartment = "null"
            reference_unit = kg
            impacts {
                1 kg cc
                1 kg tox_hum
            }
        }
""".trimIndent()
        )
    }

    @Test
    fun test_formattingLineComments() {
        doTextTest(
            """  
                // Before1
            import ef31
                // Before
            process farm_carrot {
            // You
                    // You
                params {
                        // All quantities are strongly typed
            // All quantities are strongly typed
                    qElec = 3.0 kJ
                }
            }
                // Before
        // Before
            substance propanol_eau {
                    // Before
            // Before
                name = "propanol"
                type = Land_use
                compartment = "eau"
                reference_unit = kg
            }
                // Before
        // Before
            unit Tonne {
                    // Before
            // Before
                symbol = "T"
                alias_for = 1000 kg
            }
            unit kg {
                    // Before
            // Before
                    symbol = "T"
        dimension = "mass"
        }
 """.trimIndent(), """
            // Before1
            import ef31
            // Before
            process farm_carrot {
                // You
                // You
                params {
                    // All quantities are strongly typed
                    // All quantities are strongly typed
                    qElec = 3.0 kJ
                }
            }
            // Before
            // Before
            substance propanol_eau {
                // Before
                // Before
                name = "propanol"
                type = Land_use
                compartment = "eau"
                reference_unit = kg
            }
            // Before
            // Before
            unit Tonne {
                // Before
                // Before
                symbol = "T"
                alias_for = 1000 kg
            }
            unit kg {
                // Before
                // Before
                symbol = "T"
                dimension = "mass"
            }
""".trimIndent()
        )
    }

    @Test
    fun test_formattingBlockComments() {
        // There ise clear limitation at the moment: the formatter only realign COMMENT_BLOCK_START, not COMMENT_CONTENT or COMMENT_BLOCK_END
        doTextTest(
            """  
                  /* Before1
            k dja
            */
            import ef31
                  /* Before2
            k djb
            */
            process farm_carrot {
                  /* Before3
                k djc
                */
                params {
                  /* Before4
                    k djd
                    */
                    qElec = 3.0 kJ
                }
            }
            
                  /* Before5
            k dje
            */
            substance propanol_eau {
                  /* Before6
                k djf
                */
                name = "propanol"
                type = Resource
                compartment = "eau"
                reference_unit = kg
            }
            
                  /* Before7
            k djg
            */
            unit Tonne {
                  /* Before8
                k djh
                */
                symbol = "T"
                alias_for = 1000 kg
            }
 """.trimIndent(), """
             /* Before1
             k dja
             */
             import ef31
             /* Before2
             k djb
             */
             process farm_carrot {
                 /* Before3
                 k djc
                 */
                 params {
                     /* Before4
                     k djd
                     */
                     qElec = 3.0 kJ
                 }
             }
        
             /* Before5
             k dje
             */
             substance propanol_eau {
                 /* Before6
                 k djf
                 */
                 name = "propanol"
                 type = Resource
                 compartment = "eau"
                 reference_unit = kg
             }
        
             /* Before7
             k djg
             */
             unit Tonne {
                 /* Before8
                 k djh
                 */
                 symbol = "T"
                 alias_for = 1000 kg
             }
""".trimIndent()
        )
    }

    override fun getTestDataPath(): String {
        return ""
    }

    override fun getBasePath(): String {
        return ""
    }

    override fun getFileExtension(): String {
        return "lca"
    }
}
