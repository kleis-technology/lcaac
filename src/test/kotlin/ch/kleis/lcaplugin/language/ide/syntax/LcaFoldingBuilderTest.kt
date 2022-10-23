package ch.kleis.lcaplugin.language.ide.syntax

import com.intellij.testFramework.LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.junit.Test


internal class LcaFoldingBuilderTest : UsefulTestCase() {

    private lateinit var myFixture: CodeInsightTestFixture

    override fun setUp() {
        val fixtureBuilder = IdeaTestFixtureFactory.getFixtureFactory().createLightFixtureBuilder(EMPTY_PROJECT_DESCRIPTOR)
        myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixtureBuilder.fixture)
        myFixture.setUp()
    }

    @Test
    fun testShouldFoldText() {
        myFixture.testFolding("src/test/testData/lca/folding.lca")
    }

}

