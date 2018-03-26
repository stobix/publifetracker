package stobix.view.containerview

import android.util.Log
import org.junit.*
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertEquals

/**
 * Apparently I can't do an instrumented test from Kotlin at this point in time. This file is now
 * a "library" file to be called by the Java class with the same name
 *
 */
open class AltAltContainerInstrumentedTestTests(val db: AltAltDatabase,val dao: AltAltContainerDao) {

    private infix fun <A> A.asEq(a: A) = assertEquals(this,a)
    private infix fun <A> A.asNEq(a: A) = assertNotEquals(this,a)

    @Test
    open fun setEntries(): Boolean{
        dao.insertCollection(Collection(collId = 0))
        dao.insertSubmission(Submission(timestamp = 1234567890,collId = 0))
        dao.insertEntry(Entry(pos =0, collId =0, type = EntryTypes.COLLECTION, extId = 1))
        dao.insertCollection(Collection(collId = 1))
        dao.insertEntry(Entry(pos =0, collId =1, type =EntryTypes.MEASURE, extId = 0))
        dao.insertMesUnit(MesUnit(0,"m","meter"))
        dao.insertMeasurement(Measurement(0,1.3f,0))
        dao.insertTag(Tag(0,"one","first"))
        dao.insertTag(Tag(1,"two","not first"))
        dao.insertEntryTag(EntryTag(0,0,0))
        dao.insertEntryTag(EntryTag(0,1,1))

        val s=dao.getSubmission(1234567890)
        Log.d("insert", "$s")
        System.out.println("$s")
        s asEq Submission(1234567890,0)
        return true
        //assertEquals(1,1)
    }

}