package stobix.utils.kotlinExtensions

import java.io.Serializable

/**
 * Makes a [Triple] from a [Pair]
 */
infix fun <A, B, C> Pair<A, B>.to(c: C): Triple<A, B, C> = Triple(this.first, this.second, c)

/**
 * Makes a [Triple] from a [Pair] without overriding default [to] behaviour
 */
infix fun <A, B, C> Pair<A, B>.to3(c: C): Triple<A, B, C> = Triple(this.first, this.second, c)

/**
 * Makes a [Quadruple] from a [Triple]
 */
infix fun <A, B, C, D> Triple<A, B, C>.to(d: D): Quadruple<A, B, C, D> = Quadruple(this.first, this.second, this.third, d)

/**
 * Makes a [Quadruple] from a [Triple] without overriding default [to] behaviour
 */
infix fun <A, B, C, D> Triple<A, B, C>.to4(d: D): Quadruple<A, B, C, D> = Quadruple(this.first, this.second, this.third, d)

/**
 * Makes a [Quintuple] from a [Quadruple]
 */
infix fun <A, B, C, D, E> Quadruple<A, B, C, D>.to(e: E): Quintuple<A, B, C, D, E> = Quintuple(this.first, this.second, this.third, this.fourth, e)

/**
 * Makes a [Quintuple] from a [Quadruple] without overriding default [to] behaviour
 */
infix fun <A, B, C, D, E> Quadruple<A, B, C, D>.to5(e: E): Quintuple<A, B, C, D, E> = Quintuple(this.first, this.second, this.third, this.fourth, e)

/**
 * Makes a [Sextuple] from a [Quintuple]
 */
infix fun <A, B, C, D, E, F> Quintuple<A, B, C, D, E>.to(f: F): Sextuple<A, B, C, D, E, F> = Sextuple(this.first, this.second, this.third, this.fourth, this.fifth, f)

/**
 * Makes a [Sextuple] from a [Quintuple] without overriding default [to] behaviour
 */
infix fun <A, B, C, D, E, F> Quintuple<A, B, C, D, E>.to6(f: F): Sextuple<A, B, C, D, E, F> = Sextuple(this.first, this.second, this.third, this.fourth, this.fifth, f)

/**
 * A [Pair] with four values
 */
data class Quadruple<out A, out B, out C, out D>(val first: A, val second: B, val third: C, val fourth: D) : Serializable {
    override fun toString(): String = "($first, $second, $third, $fourth)"
}

/**
 * A [Pair] with five values
 */
data class Quintuple<out A, out B, out C, out D, out E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E) : Serializable {
    override fun toString(): String = "($first, $second, $third, $fourth, $fifth)"
}

/**
 * A [Pair] with six values
 */
data class Sextuple<out A, out B, out C, out D, out E, out F>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E, val sixth: F) : Serializable {
    override fun toString(): String = "($first, $second, $third, $fourth, $fifth)"
}

fun <A, B, C> Pair<A, A>.zipWith(q: Pair<B, B>, f: (A, B) -> C) =
        f(first, q.first) to f(second, q.second)

fun <A, B, C> Triple<A, A, A>.zipWith(q: Triple<B, B, B>, f: (A, B) -> C) =
        f(first, q.first) to f(second, q.second) to f(third, q.third)

fun <A, B, C> Quadruple<A, A, A, A>.zipWith(q: Quadruple<B, B, B, B>, f: (A, B) -> C) =
        f(first, q.first) to f(second, q.second) to f(third, q.third) to f(fourth, q.fourth)

fun <A, B, C> Quadruple<A, A, A, A>.zipWith(f: (A, B) -> C, q: Quadruple<B, B, B, B>) =
        f(first, q.first) to f(second, q.second) to f(third, q.third) to f(fourth, q.fourth)


fun <T> Triple<T, T, T>.toList(): List<T> = listOf(first, second, third)
fun <T> Quadruple<T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth)
fun <T> Quintuple<T, T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth, fifth)
fun <T> Sextuple<T, T, T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth, fifth, sixth)

fun <A, B> Pair<A, A>.map(f: (A) -> B) = f(first) to f(second)
fun <A, B> Triple<A, A, A>.map(f: (A) -> B) = f(first) to f(second) to f(third)
fun <A, B> Quadruple<A, A, A, A>.map(f: (A) -> B) = f(first) to f(second) to f(third) to f(fourth)
fun <A, B> Quintuple<A, A, A, A, A>.map(f: (A) -> B) = f(first) to f(second) to f(third) to f(fourth) to f(fifth)
fun <A, B> Sextuple<A, A, A, A, A, A>.map(f: (A) -> B) = f(first) to f(second) to f(third) to f(fourth) to f(fifth) to f(sixth)

infix fun <A, B> Pair<A, A>.folding(f: (A, A) -> B) = f(first, second)
infix fun <A> Triple<A, A, A>.folding(f: (A, A) -> A) = first to second folding f to third folding f
infix fun <A> Quadruple<A, A, A, A>.folding(f: (A, A) -> A) = first to second folding f to third folding f to fourth folding f
infix fun <A> Quintuple<A, A, A, A, A>.folding(f: (A, A) -> A) = first to second folding f to third folding f to fourth folding f to fifth folding f
infix fun <A> Sextuple<A, A, A, A, A, A>.folding(f: (A, A) -> A) = first to second folding f to third folding f to fourth folding f to fifth folding f to sixth folding f

fun <A, B> Pair<A, B>.flip() = second to first

fun <A, B, C> Triple<A, B, C>.flip1() = second to first to third
fun <A, B, C> Triple<A, B, C>.flip2() = first to third to second
// flip1().flip2() = third to second to first

fun <A, B, C, D> Quadruple<A, B, C, D>.flip1() = second to first to third to fourth
fun <A, B, C, D> Quadruple<A, B, C, D>.flip2() = first to third to second to fourth
fun <A, B, C, D> Quadruple<A, B, C, D>.flip3() = first to second to fourth to third

infix fun <A, B, C> Pair<A, B>.onFirst(f: (A) -> C): Pair<C, B> = f(first) to second
infix fun <A, B, C> Pair<A, B>.onSecond(f: (B) -> C): Pair<A, C> = flip().onFirst(f).flip()

infix fun <A, B, C, D> Triple<A, B, C>.onFirst(f: (A) -> D): Triple<D, B, C> = f(first) to second to third
infix fun <A, B, C, D> Triple<A, B, C>.onSecond(f: (B) -> D): Triple<A, D, C> = flip1().onFirst(f).flip1()
infix fun <A, B, C, D> Triple<A, B, C>.onThird(f: (C) -> D): Triple<A, B, D> = flip2().onSecond(f).flip2()

infix fun <A, B, C, E, D> Quadruple<A, B, C, D>.onFirst(f: (A) -> E): Quadruple<E, B, C, D> = f(first) to second to third to fourth
infix fun <A, B, C, E, D> Quadruple<A, B, C, D>.onSecond(f: (B) -> E): Quadruple<A, E, C, D> = flip1().onFirst(f).flip1()
infix fun <A, B, C, E, D> Quadruple<A, B, C, D>.onThird(f: (C) -> E): Quadruple<A, B, E, D> = flip2().onSecond(f).flip2()
infix fun <A, B, C, E, D> Quadruple<A, B, C, D>.onFourth(f: (D) -> E): Quadruple<A, B, C, E> = flip3().onThird(f).flip3()
