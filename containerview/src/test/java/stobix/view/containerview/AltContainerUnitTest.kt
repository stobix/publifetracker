package stobix.view.containerview

import android.icu.util.Measure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Created by stobix on 2018-02-28.
 */

class AltContainerUnitTest{
    private infix fun <A> A.asEq(a: A) = assertEquals(this,a)
    private infix fun <A> A.asNEq(a: A) = assertNotEquals(this,a)

//    @Test fun createMeasurement() {
//        val m = Measurement( 3, MeasurementUnit( "m","meter" ) )
//    }
//
//    @Test fun tagMeasurement(){
//        val m = Measurement( 3, MeasurementUnit( "m","meter" ) )
//        val store = TagStore()
//        store.appendEntryTag(m,Tag(tag="kaka",description = "mums"))
//        println("m tags: ")
//        store.listTags(m).forEach(::println)
//        println("tags: ")
//        store.tags.forEach(::println)
//        println("conns: ")
//        store.connections.forEach(::println)
//        store.listTags(m) asNEq emptyList()
//    }
//
//    @Test fun addTag() {
//        val store=TagStore()
//        store.appendTag("kaka","någonting att äta")
//        store.tags asEq mapOf(Pair("kaka" , Tag("kaka","någonting att äta")))
//    }
//
//    @Test fun evaluateStuff(){
//        val a=Add(Value(1),Value(2))
//        println(a)
//        println(a(3))
//        val b = Multiply(3,a)
//        val c = Divide(b,From())
//        println(c)
//        println(c(2))
//    }
}