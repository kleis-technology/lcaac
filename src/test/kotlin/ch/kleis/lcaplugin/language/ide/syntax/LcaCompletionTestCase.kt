package ch.kleis.lcaplugin.language.ide.syntax

import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.TempDirTestFixture
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl


abstract class LcaCompletionTestCase : UsefulTestCase() {

    var myFixture: CodeInsightTestFixtureImpl? = null

    override fun setUp() {
        super.setUp()

        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        val fixtureBuilder = factory.createLightFixtureBuilder(getProjectDescriptor(), getTestName(false))
        val fixture = fixtureBuilder.fixture
        myFixture = CodeInsightTestFixtureImpl(fixture, getTempDirFixture())
        myFixture!!.testDataPath = getTestDataPath();
        myFixture!!.setUp();

    }

    override fun tearDown() {
        try {
            myFixture!!.tearDown()
        } catch (e: Throwable) {
            addSuppressedException(e)
        } finally {
            myFixture = null
            super.tearDown()
        }
    }

    val fixture: CodeInsightTestFixture
        get() = myFixture!!
//    public fun getFixture(): CodeInsightTestFixture = myFixture!!

    protected open fun getTestDataPath(): String {
        return this.javaClass.getResource("testData")?.path ?: ""
//        this.javaClass.getResource("testData").path
    }
//    protected open fun getTestDataPath(): String {
//        val communityPath = PlatformTestUtil.getCommunityPath().replace(File.separatorChar, '/')
//        val path = communityPath + getBasePath()
//        return if (File(path).exists()) path else communityPath + "/../" + getBasePath()
//    }

    private fun getBasePath(): String {
        return ""
    }

    private fun getProjectDescriptor(): LightProjectDescriptor {
        return LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR
    }

    protected open fun getTempDirFixture(): TempDirTestFixture {
        val policy = IdeaTestExecutionPolicy.current()
        return if (policy != null) policy.createTempDirTestFixture() else LightTempDirTestFixtureImpl(true)
    }
}
