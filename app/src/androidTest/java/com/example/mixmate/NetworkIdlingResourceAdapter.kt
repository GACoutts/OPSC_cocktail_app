package com.example.mixmate

import androidx.test.espresso.IdlingResource

/**
 * Espresso IdlingResource adapter bridging the production lightweight NetworkIdlingResource.
 */
object NetworkIdlingResourceAdapter : IdlingResource {
    @Volatile private var callback: IdlingResource.ResourceCallback? = null

    override fun getName(): String = "NetworkIdlingResourceAdapter"

    override fun isIdleNow(): Boolean = NetworkIdlingResource.isIdle()

    override fun registerIdleTransitionCallback(cb: IdlingResource.ResourceCallback?) {
        callback = cb
        NetworkIdlingResource.registerIdleTransitionCallback {
            callback?.onTransitionToIdle()
        }
    }
}

