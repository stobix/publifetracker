package stobix.utils

import org.junit.Test

import org.junit.Assert.*
import java.util.*

private infix fun <A> A.asEq(a: A) = assertEquals(this,a)
private infix fun <A> A.asNEq(a: A) = assertNotEquals(this,a)

class DateHandlerTest {

    val d = DateHandler()

    @Test
    fun testWeekDayIdentity() {
        for (i in 0..6) {
            d.weekDay = i
            d.weekDay asEq i
        }
    }

    @Test
    fun testWeekDayOverflow() {
        for (i in 0..6) {
            d.weekDay = i+7
            d.weekDay asEq i
        }
    }

    @Test
    fun testWeekDayUnderflow() {
        for (i in 0..6) {
            d.weekDay = i-7
            d.weekDay asEq i
        }
    }

    @Test
    fun testWeekDayFirstDayIsMonday(){
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY)
        d.timestamp = c.timeInMillis
        d.weekDay asEq 0
    }

    @Test
    fun testWeekStartsWithMonday(){
        d.weekStartsWith asEq 0
    }
}