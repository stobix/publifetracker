package stobix.compat.monads

/**
 * Since I'm reimplementing Java's functional interface, why not add some monads and pals for the heck of it...
 *
 * Option stolen from
 * https://github.com/aballano/FAM-Playground/blob/master/src/Option.kt
 */

interface Monad<M, out A> {
    fun <B> map (f: (A) -> B) : Monad<M,B>
    fun <B,W> flatMap (f: (A) -> Monad<W,B>): Monad<W,B>
}


sealed class Option<out A> {
    object None : Option<Nothing>()
    data class Some<out A>(val value: A) : Option<A>()

    infix inline fun <B> map(f: (A) -> B): Option<B> = when (this) {
        is None -> this
        is Some -> Some(f(value))
    }

    infix inline fun <B> flatMap(f: (A) -> Option<B>): Option<B> = when (this) {
        is None -> this
        is Some -> f(value)
    }
}

infix fun <A, B> Option<(A) -> B>.apply(f: Option<A>): Option<B> = when (this) {
    is Option.None -> Option.None
    is Option.Some -> f.map(this.value)
}

fun <A> fromNullable(a:A?): Option<A> = if (a==null) Option.None else Option.Some(a)

// http://stackoverflow.com/questions/34745066/dummyimplicits-is-this-used-and-how/34746255?stw=2#34746255

// No need to make apply be commutative
/*
fun <A, B> Option<A>.apply(f: Option<(A) -> B>, dummyImplicit: Any? = null): Option<B> = when (this) {
    is Option.None -> Option.None
    is Option.Some -> f.map { it(value) }
}
*/

infix inline fun <A, reified B> Array<(A) -> B>.apply(a: Array<A>) =
        Array(this.size * a.size) {
            this[it / a.size](a[it % a.size])
        }

fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> (C)  = { a -> { b -> f(a, b) } }

fun <A, B, C, D> curry(f: (A, B, C) -> D): (A) -> (B) -> (C) -> D =
        { a -> { b -> { c -> f(a, b, c) } } }

fun <A, B, C, D, E> curry(f: (A, B, C, D) -> E): (A) -> (B) -> (C) -> (D) -> E =
        { a -> { b -> { c -> { d -> f(a, b, c, d) } } } }

