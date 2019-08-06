package stobix.utils.kotlinExtensions

import java.io.Serializable

infix fun <A,B,C> Pair<A,B>.to(c:C):Triple<A,B,C> = Triple(this.first,this.second,c)

infix fun <A,B,C> Pair<A,B>.to3(c:C):Triple<A,B,C> = Triple(this.first,this.second,c)

infix fun <A,B,C,D> Triple<A,B,C>.to(d:D):Quadruple<A,B,C,D> = Quadruple(this.first,this.second,this.third,d)

infix fun <A,B,C,D> Triple<A,B,C>.to4(d:D):Quadruple<A,B,C,D> = Quadruple(this.first,this.second,this.third,d)

infix fun <A,B,C,D,E> Quadruple<A,B,C,D>.to(e:E):Quintuple<A,B,C,D,E> = Quintuple(this.first,this.second,this.third,this.fourth,e)

infix fun <A,B,C,D,E> Quadruple<A,B,C,D>.to5(e:E):Quintuple<A,B,C,D,E> = Quintuple(this.first,this.second,this.third,this.fourth,e)

infix fun <A,B,C,D,E,F> Quintuple<A,B,C,D,E>.to(f:F):Sextuple<A,B,C,D,E,F> = Sextuple(this.first,this.second,this.third,this.fourth,this.fifth,f)

infix fun <A,B,C,D,E,F> Quintuple<A,B,C,D,E>.to6(f:F):Sextuple<A,B,C,D,E,F> = Sextuple(this.first,this.second,this.third,this.fourth,this.fifth,f)

data class Quadruple<out A,out B,out C,out D> (val first:A, val second:B, val third:C, val fourth:D): Serializable {
    override fun toString(): String = "($first, $second, $third, $fourth)"
}

data class Quintuple<out A,out B,out C,out D, out E> (val first:A, val second:B, val third:C, val fourth:D, val fifth:E): Serializable {
    override fun toString(): String = "($first, $second, $third, $fourth, $fifth)"
}

data class Sextuple<out A,out B,out C,out D, out E, out F> (val first:A, val second:B, val third:C, val fourth:D, val fifth:E, val sixth:F): Serializable {
    override fun toString(): String = "($first, $second, $third, $fourth, $fifth)"
}

fun <A,B,C> Pair<A,A>.zipWith(q: Pair<B,B>, f:(A,B)->C) =
        f(first,q.first) to f(second,q.second)

fun <A,B,C> Triple<A,A,A>.zipWith(q: Triple<B,B,B>, f:(A,B)->C) =
        f(first,q.first) to f(second,q.second) to f(third,q.third)

fun <A,B,C> Quadruple<A,A,A,A>.zipWith(q: Quadruple<B,B,B,B>, f:(A,B)->C) =
        f(first,q.first) to f(second,q.second) to f(third,q.third) to f(fourth,q.fourth)

fun <A,B,C> Quadruple<A,A,A,A>.zipWith(f:(A,B)->C, q: Quadruple<B,B,B,B>) =
        f(first,q.first) to f(second,q.second) to f(third,q.third) to f(fourth,q.fourth)


fun <T> Triple<T, T, T>.toList(): List<T> = listOf(first, second, third)
fun <T> Quadruple<T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth)
fun <T> Quintuple<T, T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth, fifth)
fun <T> Sextuple<T, T, T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth, fifth, sixth)

fun <A,B> Pair<A,A>.map(f:(A)->B) = f(first) to f(second)
fun <A,B> Triple<A,A,A>.map(f:(A)->B) = f(first) to f(second) to f(third)
fun <A,B> Quadruple<A,A,A,A>.map(f:(A)->B) = f(first) to f(second) to f(third) to f(fourth)
fun <A,B> Quintuple<A,A,A,A,A>.map(f:(A)->B) = f(first) to f(second) to f(third) to f(fourth) to f(fifth)
fun <A,B> Sextuple<A,A,A,A,A,A>.map(f:(A)->B) = f(first) to f(second) to f(third) to f(fourth) to f(fifth) to f(sixth)

infix fun <A,B> Pair<A,A>.folding(f:(A,A) -> B) = f(first,second)
infix fun <A> Triple<A,A,A>.folding(f:(A,A) -> A) = first to second folding f to third folding f
infix fun <A> Quadruple<A,A,A,A>.folding(f:(A,A) -> A) = first to second folding f to third folding f to fourth folding f
infix fun <A> Quintuple<A,A,A,A,A>.folding(f:(A,A) -> A) = first to second folding f to third folding f to fourth folding f to fifth folding f
infix fun <A> Sextuple<A,A,A,A,A,A>.folding(f:(A,A) -> A) = first to second folding f to third folding f to fourth folding f to fifth folding f to sixth folding f

fun <A,B> Pair<A,B>.flip() = second to first

infix fun <A,B,C> Pair<A,B>.onFirst(f:(A) -> C) = f(first) to second
infix fun <A,B,C> Pair<A,B>.onSecond(f:(A) -> C) =flip().let{ f(first) to second}


