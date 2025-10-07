package com.example.mixmate

import java.util.concurrent.atomic.AtomicInteger

/**
 * Lightweight idling counter decoupled from Espresso so unit (JVM) tests do not
 * depend on android.* classes. An Espresso adapter is provided in androidTest.
 */
object NetworkIdlingResource {
    private val counter = AtomicInteger(0)
    private val listeners = mutableListOf<() -> Unit>()

    fun increment() { counter.incrementAndGet() }

    fun decrement() {
        if (counter.get() > 0) {
            val newVal = counter.decrementAndGet()
            if (newVal == 0) notifyIdle()
        }
    }

    internal fun isIdle(): Boolean = counter.get() == 0

    internal fun registerIdleTransitionCallback(cb: () -> Unit) {
        if (isIdle()) {
            // Already idle – invoke immediately
            cb()
        } else {
            listeners += cb
        }
    }

    private fun notifyIdle() {
        if (listeners.isEmpty()) return
        val copy = listeners.toList()
        listeners.clear()
        copy.forEach { it.invoke() }
    }
}
