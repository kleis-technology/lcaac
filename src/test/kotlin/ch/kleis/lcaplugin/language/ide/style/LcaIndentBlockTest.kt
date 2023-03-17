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
scale = 1
        dimension = "none"


}""",
            """
                unit pack {
                    symbol = "pack"
                    scale = 1
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
 description   =   "My Desc"
 author="me"
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
        
        
  inputs {    1 kg carrot
                2 kg co2
}


        emissions{
          1 kg co2
}



   resources{
     0.5 kg co2
  }
}""",
            """
                process process_a {
                
                    meta {
                        description = "My Desc"
                        author = "me"
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
                        1 kg carrot
                        2 kg co2
                    }
                
                    emissions {
                        1 kg co2
                    }
                
                    resources {
                        0.5 kg co2
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
    fun test_formattingProcessShouldFormatFormula() {
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
        (  1  kg  +  p4  )    prod
        (1kg+p2) co_prod
        (1kg*p2) co_prod
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
                        ( 1 kg + p4 ) prod
                        ( 1 kg + p2 ) co_prod
                        ( 1 kg * p2 ) co_prod
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