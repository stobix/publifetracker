package stobix.utils.pair_extensions

import java.io.Serializable

infix fun <A,B,C> Pair<A,B>.to(c:C):Triple<A,B,C> = Triple(this.first,this.second,c)

infix fun <A,B,C> Pair<A,B>.to3(c:C):Triple<A,B,C> = Triple(this.first,this.second,c)

infix fun <A,B,C,D> Triple<A,B,C>.to(d:D):Quadruple<A,B,C,D> = Quadruple(this.first,this.second,this.third,d)

infix fun <A,B,C,D> Triple<A,B,C>.to4(d:D):Quadruple<A,B,C,D> = Quadruple(this.first,this.second,this.third,d)

data class Quadruple<out A,out B,out C,out D> (val first:A, val second:B, val third:C, val fourth:D): Serializable {
    public override fun toString(): String = "($first, $second, $third, $fourth)"
}
public fun <T> Quadruple<T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth)


