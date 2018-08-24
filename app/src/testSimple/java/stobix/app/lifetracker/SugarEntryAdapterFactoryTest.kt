package stobix.app.lifetracker

import org.junit.Assert.assertEquals
import org.junit.Test

class SugarEntryAdapterFactoryTest{

    private infix fun <A> A.asEq(a: A) = assertEquals(this,a)


    @Test
    fun emptyThing() {
        val emptySugarEntryGsonWrapper = SugarEntryGsonWrapper( listOf())
        val json1 = emptySugarEntryGsonWrapper.toJSON()
        System.out.println(json1)
        emptySugarEntryGsonWrapper asEq SugarEntryGsonWrapper.fromJSON(json1)

    }

    @Test
    fun oneElement() {
        val emptySugarEntryGsonWrapper = SugarEntryGsonWrapper( listOf(SugarEntry()))
        val json1 = emptySugarEntryGsonWrapper.toJSON()
        System.out.println(json1)
        emptySugarEntryGsonWrapper asEq SugarEntryGsonWrapper.fromJSON(json1)

    }
    @Test
    fun oneElementWeight() {
        val emptySugarEntryGsonWrapper = SugarEntryGsonWrapper( listOf(SugarEntry(weight=2)))
        val json1 = emptySugarEntryGsonWrapper.toJSON()
        System.out.println(json1)
        emptySugarEntryGsonWrapper asEq SugarEntryGsonWrapper.fromJSON(json1)

    }
}