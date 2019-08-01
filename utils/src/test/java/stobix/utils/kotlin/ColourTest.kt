package stobix.utils.kotlin

import android.graphics.Color
import android.util.Log
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

private infix fun <A> A.asEq(actual: A) = assertEquals(this,actual)
private infix fun <A> A.asNEq(actual: A) = assertNotEquals(this,actual)

class ColourTest {
    @Test
    fun range_thing(): Unit {
        val c1 = Colour(255u,0u,255u,0u)
        val c2 = Colour(255u,255u,0u,0u)

        for(c in c1..c2)
            println(c)
    }
    fun testId(i:Int) = i asEq Colour(i).color

    @Test
    fun white(): Unit {
        testId(Color.WHITE)
    }
    @Test
    fun black(): Unit {
        testId(Color.BLACK)
    }
    @Test
    fun yellow(): Unit {
        testId(Color.YELLOW)
    }
    @Test
    fun green(): Unit {
        testId(Color.GREEN)
    }
}