package stobix.compat.monads

/**
 * stobix.compat.monads unit tests
 */

import org.junit.Assert.assertEquals
import org.junit.Test
import stobix.compat.monads.Option.Some
import stobix.compat.monads.Option.None

class OptionsUnitTest{

    //inline infix fun <A> Option<out A>.asEq (a: Option<out A>) = assertEquals(this,a)
    private infix fun <A> A.asEq(a: A) = assertEquals(this,a)



    @Test
    fun optionMap () {

        Some(5) asEq Some(3).map { it + 2 }

        val none: Option<Int> = None
        none asEq none.map {it + 2}

    }

    @Test
    fun optionSomeFlatMap () {

        Some(5) asEq Some(3).flatMap { Some(it + 2) }

        val none: Option<Int> = None
        none asEq none.flatMap {Some(it+1)}
    }

    @Test
    fun optionApply() {
        val none: Option<(Int) -> Int> = None

        fun makeString(x:Int) = "$x"
        fun addTwo(x:Int) = x+2
        val oms = Some(::makeString)
        val oat = Some(::addTwo)
        fun <A,B,C>applyThings(f:Option<(A) -> B>, g: Option<(B) -> C>,a:A?):Option<C> {
            val thing = fromNullable(a)
            return g apply (f apply thing)
        }

        Some("5") asEq applyThings(oat,oms,3)
        None asEq applyThings(oat,oms,null)
        None asEq applyThings(oat,none,3)
        None asEq applyThings(None,oms,3)
    }



    @Test
    fun fromNullableTest() {
        None asEq fromNullable(null).map{it}
        Some(3) asEq fromNullable(3)
    }

    @Test
    fun fromNullableMap(){
        None asEq fromNullable(null).map {
            @Suppress("UNREACHABLE_CODE")
            fromNullable(it)
        }
        Some(Some(3)) asEq fromNullable(3).map {fromNullable(it)}

    }

    @Test
    fun arrayApply() {
        val functions = arrayOf<(Int)->Pair<Int,Int>> (
                {Pair(it,it+3)},
                {Pair(it,it/2)}
        )
        val values = arrayOf(1,2,3)
        assertEquals(
                "[(1, 4), (2, 5), (3, 6), (1, 0), (2, 1), (3, 1)]",
                (functions apply values).contentDeepToString()
        )
    }

    @Test
    fun currying() {
        fun four (a:Int, b: Int, c: Int, d: Int): Int = a+b+c+d+1
        fun three (a: Int, b:Int, c:Int) : Int = a+b+c
        fun two (a: Int, b:Int) : Int = a+b

        Some(3+1+2+3+1) asEq
                (Some(3).map(curry(::four)) apply
                        Some(1) apply
                        Some(2) apply
                        Some(3))

        1+2 asEq curry(::two)(1)(2)
        1+2+3 asEq curry(::three)(1)(2)(3)
        1+2+3+4+1 asEq curry(::four)(1)(2)(3)(4)

        1+2+3 asEq curry() { a:Int ,b:Int ,c:Int -> a+b+c} (1)(2)(3)

        fun plussa(a: Int, b: Int, c:Int) : Int {
            return a+b+c
        }
    }
}