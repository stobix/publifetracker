package stobix.utils.kotlinExtensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

private infix fun <A> A.asEq(a: A) = assertEquals(this,a)
private infix fun <A> A.asNEq(a: A) = assertNotEquals(this,a)

class KotlinExtensionsTest {
    @Test
    fun pairToTrip(){
        1 to 2 to 3 asEq Triple(1,2,3)
    }

    @Test
    fun tripToQuad(){
        1 to 2 to 3 to 4 asEq Quadruple(1,2,3,4)
    }

    @Test
    fun quadToQuin(){
        1 to 2 to 3 to 4 to 5 asEq Quintuple(1,2,3,4,5)
    }

    @Test
    fun quinToSext(){
        1 to 2 to 3 to 4 to 5 to 6 asEq Sextuple(1,2,3,4,5,6)
    }

    @Test
    fun toLists(){
                        listOf(1, 2) asEq (1 to 2).toList()
                    listOf(1, 2, 3) asEq (1 to 2 to 3).toList()
                listOf(1, 2, 3, 4) asEq (1 to 2 to 3 to 4).toList()
            listOf(1, 2, 3, 4, 5) asEq (1 to 2 to 3 to 4 to 5).toList()
        listOf(1, 2, 3, 4, 5, 6) asEq (1 to 2 to 3 to 4 to 5 to 6).toList()
    }

    @Test
    fun altNames(){
        1 to 2 to3 3 to4 4 to5 5 to6 6 asEq Sextuple(1,2,3,4,5,6)
    }

    @Test
    fun breakdown(){
        val quad = 1 to 2 to 3 to 4
        val(one,two,three,four)  = quad
        one asEq 1
        two asEq 2
        three asEq 3
        four asEq 4
    }
}