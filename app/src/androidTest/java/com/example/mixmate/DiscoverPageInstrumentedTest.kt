package com.example.mixmate

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiscoverPageInstrumentedTest {
    @get:Rule
    val scenario = ActivityScenarioRule(DiscoverPage::class.java)

    @Test
    fun appContext_packageName() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.mixmate", appContext.packageName)
    }

    // Placeholder: Additional UI assertions can be added later (loading -> content / empty)
}

