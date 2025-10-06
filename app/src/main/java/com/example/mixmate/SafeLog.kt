package com.example.mixmate

/** Lightweight logger that avoids android.util.Log dependency in local unit tests. */
object SafeLog {
    var enabled: Boolean = true
    fun d(tag: String, msg: String) { if (enabled) println("D/$tag: $msg") }
    fun w(tag: String, msg: String, tr: Throwable? = null) { if (enabled) {
        println("W/$tag: $msg")
        tr?.printStackTrace()
    }}
}

