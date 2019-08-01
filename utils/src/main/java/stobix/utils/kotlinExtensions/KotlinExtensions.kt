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

fun <T> Quadruple<T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth)

fun <A,B> Quadruple<A,A,A,A>.map(f:(A)->B) =
        f(first) to f(second) to f(third) to f(fourth)

fun <A,B,C> Quadruple<A,A,A,A>.zipWith(q: Quadruple<B,B,B,B>, f:(A,B)->C) =
        f(first,q.first) to f(second,q.second) to f(third,q.third) to f(fourth,q.fourth)

fun <A,B,C> Quadruple<A,A,A,A>.zipWith(f:(A,B)->C, q: Quadruple<B,B,B,B>) =
        f(first,q.first) to f(second,q.second) to f(third,q.third) to f(fourth,q.fourth)

operator fun Quadruple<Int,Int,Int,Int>.plus(q:Quadruple<Int,Int,Int,Int>)=
        this.zipWith(q) {a,b -> a+b}

operator fun Quadruple<Int,Int,Int,Int>.minus(q:Quadruple<Int,Int,Int,Int>)=
        this.zipWith(q) {a,b -> a-b}

operator fun Quadruple<Float,Float,Float,Float>.times(q:Quadruple<Float,Float,Float,Float>)  = this.zipWith(q) {a,b -> a*b}

operator fun Quadruple<Int,Int,Int,Int>.div(dividend: Int)=
        this.map { it / dividend }

operator fun Quadruple<Int,Int,Int,Int>.div(dividend: Float)=
        this.map { it.toFloat() / dividend }


data class Quintuple<out A,out B,out C,out D, out E> (val first:A, val second:B, val third:C, val fourth:D, val fifth:E): Serializable {
    override fun toString(): String = "($first, $second, $third, $fourth, $fifth)"
}

fun <T> Quintuple<T, T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth, fifth)

data class Sextuple<out A,out B,out C,out D, out E, out F> (val first:A, val second:B, val third:C, val fourth:D, val fifth:E, val sixth:F): Serializable {
    override fun toString(): String = "($first, $second, $third, $fourth, $fifth)"

}

fun <T> Sextuple<T, T, T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth, fifth, sixth)
