package stobix.compat.functions

/**
 * This file contains et al a subset of the java.util.function interfaces,
 * since they're not supported directly by android API 22
 */

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

// This only makes sense in Kotlin
@FunctionalInterface
interface BiSupplier<out T,out S>{
    fun get(): Pair<T,S>
}