package com.example.stobix.myapplication

/**
 * This file contains a collection of small usable interfaces.
 */

interface SendResultAble {
    fun receiveResult(type: String, vararg results: Int)
}


interface Consumer<in T> {
    fun accept(t: T)
}

interface BiConsumer<in T,in V> {
    fun accept(t: T,v: V)
}
