package com.github.albanseurat.lcaplugin.project

import com.intellij.openapi.command.impl.DummyProject
import org.junit.Test

internal class LcaRootLibraryProviderTest {


    @Test
    fun testShouldLoadFiles() {

        val provider = LcaRootLibraryProvider()

        val results = provider.getAdditionalProjectLibraries(DummyProject.getInstance())


    }
}