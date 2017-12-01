package com.example.stobix.myapplication

/**
 * This file contains a collection of small usable interfaces.
 */

interface SendResultAble {
    fun receiveResult(type: String, vararg results: Int)
}


@FunctionalInterface
interface Consumer<in T> {
    fun accept(t: T)
}

@FunctionalInterface
interface BiConsumer<in T,in V> {
    fun accept(t: T,v: V)
}

@FunctionalInterface
interface Function<in T, out R> {
    fun apply(t: T): R
}

@FunctionalInterface
interface BiFunction<in T, in U, out R> {
    fun apply(t: T, u: U): R
}


@FunctionalInterface
interface Supplier<out T>{
    fun get(): T
}

@FunctionalInterface
interface Predicate<in T>{
   fun test(t: T): Boolean
}
