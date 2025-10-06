package com.example.mixmate

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.not
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiscoverPageInstrumentedTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        // Point repository to mock server & allow blank API key
        CocktailApiRepository.replaceBaseUrlForTests(server.url("/").toString())
        CocktailApiRepository.enableBlankApiKeyForTests()
        IdlingRegistry.getInstance().register(NetworkIdlingResourceAdapter)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(NetworkIdlingResourceAdapter)
        server.shutdown()
    }

    @Test
    fun appContext_packageName() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertEquals("com.example.mixmate", appContext.packageName)
    }

    @Test
    fun loading_to_content_transition_discover() {
        val body = """[
            {"name":"Margarita","ingredients":["1 oz Tequila"],"instructions":"Mix","servings":1},
            {"name":"Cosmopolitan","ingredients":["1 oz Vodka"],"instructions":"Shake","servings":1}
        ]""".trimIndent()
        server.enqueue(MockResponse().setBody(body).setResponseCode(200))

        val scenario = androidx.test.core.app.ActivityScenario.launch(DiscoverPage::class.java)

        // Initial state: loading visible (best-effort, may be quick)
        onView(withId(R.id.loading_container_discover)).check(matches(isDisplayed()))

        // After network completes: list visible, loading & empty hidden
        onView(withId(R.id.rv_discover_suggested)).check(matches(isDisplayed()))
        onView(withId(R.id.loading_container_discover)).check(matches(not(isDisplayed())))
        onView(withId(R.id.empty_container_discover)).check(matches(not(isDisplayed())))

        scenario.close()
    }

    @Test
    fun empty_state_when_no_results_discover() {
        server.enqueue(MockResponse().setBody("[]").setResponseCode(200))
        val scenario = androidx.test.core.app.ActivityScenario.launch(DiscoverPage::class.java)

        onView(withId(R.id.empty_container_discover)).check(matches(isDisplayed()))
        onView(withId(R.id.rv_discover_suggested)).check(matches(not(isDisplayed())))

        scenario.close()
    }
}
