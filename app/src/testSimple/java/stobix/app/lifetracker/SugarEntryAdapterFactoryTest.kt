package stobix.app.lifetracker

import org.junit.Assert.assertEquals
import org.junit.Test

class SugarEntryAdapterFactoryTest{

    private infix fun <A> A.asEq(a: A) = assertEquals(this,a)
    private infix fun <A> A.eqAs(a: A) = assertEquals(a,this)
    private infix fun String.json5Eq(s: SugarEntry) =
            SugarEntryGsonWrapper.fromJSON("""5[{"t":0,"""+this+"}]").entries[0] eqAs s


    @Test
    fun emptyThing() {
        val emptySugarEntryGsonWrapper = SugarEntryGsonWrapper( listOf())
        val json1 = emptySugarEntryGsonWrapper.toJSON()
        println(json1)
        emptySugarEntryGsonWrapper asEq SugarEntryGsonWrapper.fromJSON(json1)

    }

    @Test
    fun oneElement() {
        val emptySugarEntryGsonWrapper = SugarEntryGsonWrapper( listOf(SugarEntry()))
        val json1 = emptySugarEntryGsonWrapper.toJSON()
        println(json1)
        emptySugarEntryGsonWrapper asEq SugarEntryGsonWrapper.fromJSON(json1)

    }
    @Test
    fun oneElementWeight() {
        val emptySugarEntryGsonWrapper = SugarEntryGsonWrapper( listOf(SugarEntry(weight=2)))
        val json1 = emptySugarEntryGsonWrapper.toJSON()
        println(json1)
        emptySugarEntryGsonWrapper asEq SugarEntryGsonWrapper.fromJSON(json1)
    }
    @Test
    fun insulin() {
        val emptySugarEntryGsonWrapper = SugarEntryGsonWrapper( listOf(SugarEntry(insulin=2.0)))
        val json1 = emptySugarEntryGsonWrapper.toJSON()
        println(json1)
        emptySugarEntryGsonWrapper asEq SugarEntryGsonWrapper.fromJSON(json1)
    }
    @Test
    fun oldJsonTreatmentInsulin() =
        """"tr":"4"""" json5Eq SugarEntry(insulin = 4.0)
    @Test
    fun oldJsonTreatmentKakor() =
            """"tr":"4 kakor"""" json5Eq SugarEntry(treatment = "4 kakor")
    @Test
    fun oldJsonTreatmentInsulinPoint() =
        """"tr":"4.5"""" json5Eq SugarEntry(insulin=4.5)
    @Test
    fun oldJsonTreatmentInsulinComma() =
        """"tr":"4,5"""" json5Eq SugarEntry(insulin = 4.5)
    @Test
    fun oldJsonTreatmentInsulinMixed() =
        """"tr":"4, kakor"""" json5Eq SugarEntry(insulin = 4.0, treatment = "kakor")
    @Test
    fun oldJsonTreatmentInsulinMixedReversed() =
        """"tr":"kakor, 4"""" json5Eq SugarEntry(insulin=4.0, treatment="kakor")
    @Test
    fun oldJsonTreatmentInsulinMixedInterspersed() =
            """"tr":"mumma, 4, kakor"""" json5Eq SugarEntry(insulin = 4.0, treatment = "mumma, kakor")
    @Test
    fun oldJsonTreatmentInsulinError() =
            """"tr":"4, kakor, 5"""" json5Eq SugarEntry(treatment = "4, kakor, 5")
}