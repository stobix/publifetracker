package stobix.app.lifetracker

import org.junit.Assert.assertEquals
import org.junit.Test

class SugarEntryMergerTest {
    //inline infix fun <A> Option<out A>.asEq (a: Option<out A>) = assertEquals(a,this)
    private infix fun <A> A.asEq(a: A) = assertEquals(a,this)

    @Test
    fun zeroRecipient() {
        val l1 = listOf(
                SugarEntry(1,0,"first"),
                SugarEntry(2,0,"second")
        )
        val l2 = listOf<SugarEntry>()
        l1.getMergeables(l2) asEq listOf()
    }
    @Test
    fun zeroMergeList() {
        val l1 = listOf<SugarEntry>()
        val l2 = listOf(
                SugarEntry(1,0,"first"),
                SugarEntry(2,0,"second")
        )
        l1.getMergeables(l2) asEq l2

    }
    @Test
    fun mergeablesToShorter() {
        val l1 = listOf(
                SugarEntry(1,0,"first"),
                SugarEntry(2,0,"second")
        )
        val l2 = listOf(
                SugarEntry(0,0,"zeroth"),
                SugarEntry(1,0,"first"),
                SugarEntry(2,0,"third"),
                // This will disappear since it has a bogus timestamp. For now. TODO
                SugarEntry(2,0,"forth"),
                SugarEntry(3,0,"haskell"),
                SugarEntry(4,0,"python")
        )

        l1.getMergeables(l2) asEq listOf(
                SugarEntry(0,0,"zeroth"),
                SugarEntry(3,0,"third"),
                SugarEntry(4,0,"haskell"),
                SugarEntry(5,0,"python")
        )
    }

    @Test
    fun mergeablesToLonger() {
        val l1 = listOf(
                SugarEntry(0,0,"zeroth"),
                SugarEntry(1,0,"first"),
                SugarEntry(2,0,"third"),
                SugarEntry(2,0,"forth"),
                SugarEntry(3,0,"haskell")
        )
        val l2 = listOf(
                SugarEntry(1,0,"first"),
                SugarEntry(2,0,"second"),
                SugarEntry(11,0,"eleventh")
        )

        l1.getMergeables(l2) asEq listOf(
                SugarEntry(4,0,"second"),
                SugarEntry(11,0,"eleventh")
        )
    }

    @Test
    fun strangeConditionThatCreatedABug(){
        val l1 = listOf(
                SugarEntry(1,0,"first"),
                SugarEntry(2,0,"second")
        )
        val l2 = listOf(
                SugarEntry(1,0,"not first"),
                SugarEntry(2,0,"second")
        )
        l1.getMergeables(l2) asEq listOf(
                SugarEntry(3,0,"not first")
        )
    }

    @Test
    fun strangeConditionThatCreatedABugExtended(){
        val l1 = listOf(
                SugarEntry(1,0,"first"),
                SugarEntry(2,0,"second"),
                SugarEntry(3,0,"third")
        )
        val l2 = listOf(
                SugarEntry(1,0,"not first"),
                SugarEntry(2,0,"second"),
                SugarEntry(3,0,"third")
        )
        l1.getMergeables(l2) asEq listOf(
                SugarEntry(4,0,"not first")
        )
    }

}