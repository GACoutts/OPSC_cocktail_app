package com.example.mixmate

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.not
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MyBarInstrumentedTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
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
    fun loading_to_content_transition_mybar() {
        val body = """[
            {"name":"Gin Fizz","ingredients":["1 oz Gin"],"instructions":"Mix","servings":1},
            {"name":"Whiskey Sour","ingredients":["1 oz Whiskey"],"instructions":"Shake","servings":1}
        ]""".trimIndent()
        server.enqueue(MockResponse().setBody(body).setResponseCode(200))

        val scenario = androidx.test.core.app.ActivityScenario.launch(MyBar::class.java)

        onView(withId(R.id.loading_container_bar)).check(matches(isDisplayed()))

        onView(withId(R.id.rv_suggested)).check(matches(isDisplayed()))
        onView(withId(R.id.loading_container_bar)).check(matches(not(isDisplayed())))
        onView(withId(R.id.empty_container_bar)).check(matches(not(isDisplayed())))

        scenario.close()
    }

    @Test
    fun empty_state_when_no_results_mybar() {
        server.enqueue(MockResponse().setBody("[]").setResponseCode(200))
        val scenario = androidx.test.core.app.ActivityScenario.launch(MyBar::class.java)

        onView(withId(R.id.empty_container_bar)).check(matches(isDisplayed()))
        onView(withId(R.id.rv_suggested)).check(matches(not(isDisplayed())))

        scenario.close()
    }
}
