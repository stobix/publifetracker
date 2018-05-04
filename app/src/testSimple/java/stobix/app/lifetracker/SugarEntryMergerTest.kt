package stobix.app.lifetracker

import org.junit.Assert.*
import org.junit.Test
import stobix.compat.monads.Option.Some
import stobix.compat.monads.Option.None

class SugarEntryMergerTest {
    //inline infix fun <A> Option<out A>.asEq (a: Option<out A>) = assertEquals(this,a)
    private infix fun <A> A.asEq(a: A) = assertEquals(this,a)

    @Test
    fun mergeablesToShorter() {
        val l1 = listOf(
                SugarEntry(1,1,0,"first"),
                SugarEntry(2,2,0,"second")
        )
        val l2 = listOf(
                SugarEntry(0,0,0,"zeroth"),
                SugarEntry(1,1,0,"first"),
                SugarEntry(2,2,0,"third"),
                // This will disappear since it has a bogus timestamp
                SugarEntry(2,2,0,"forth"),
                SugarEntry(2,3,0,"haskell"),
                SugarEntry(2,4,0,"python")
        )

        l1.getMergeables(l2) asEq listOf(
                SugarEntry(0,0,0,"zeroth"),
                SugarEntry(2,3,0,"third"),
                SugarEntry(2,4,0,"haskell"),
                SugarEntry(2,5,0,"python")
        )
    }

    @Test
    fun mergeablesToLonger() {
        val l1 = listOf(
                SugarEntry(0,0,0,"zeroth"),
                SugarEntry(1,1,0,"first"),
                SugarEntry(2,2,0,"third"),
                SugarEntry(2,2,0,"forth"),
                SugarEntry(2,3,0,"haskell")
        )
        val l2 = listOf(
                SugarEntry(1,1,0,"first"),
                SugarEntry(2,2,0,"second"),
                SugarEntry(2,11,0,"eleventh")
        )

        l1.getMergeables(l2) asEq listOf(
                SugarEntry(2,4,0,"second"),
                SugarEntry(2,11,0,"eleventh")
        )
    }

    @Test
fun sameness() {
        assertTrue(SugarEntry() sameAs SugarEntry())
        assertTrue(SugarEntry(uid=1) sameAs SugarEntry(uid=0))
        assertTrue(
                SugarEntry(0,1,2,"")
                        sameAs SugarEntry(0,1,2,""))
        assertFalse(
                SugarEntry(0,1,2,"")
                        sameAs SugarEntry(0,1,2,"a"))
    }


}