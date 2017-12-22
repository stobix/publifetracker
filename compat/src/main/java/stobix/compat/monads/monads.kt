package stobix.compat.monads

/**
 * Since I'm reimplementing Java's functional interface, why not add some monads and pals for the heck of it...
 *
 * Option stolen from
 * https://github.com/aballano/FAM-Playground/blob/master/src/Option.kt
 */

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

// http://stackoverflow.com/questions/34745066/dummyimplicits-is-this-used-and-how/34746255?stw=2#34746255
fun <A, B> Option<A>.apply(f: Option<(A) -> B>, dummyImplicit: Any? = null): Option<B> = when (this) {
    is Option.None -> Option.None
    is Option.Some -> f.map { it(value) }
}

fun <A> fromNullable(a:A?): Option<A> = if (a==null) Option.None else Option.Some(a)

