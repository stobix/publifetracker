package stobix.view.containerview

import android.util.Log
import stobix.utils.DateHandler

/**
 * Created by stobix on 2018-03-26.
 *
 *  The thought here is that many CSubmissions can point to the same CCollection, hence I won't use
 *  a List<CContent> directly
 *
 */

class AltAltContainer {
    companion object {
        @JvmStatic fun getAllSubmissions(dao: AltAltContainerDao): List<CSubmission> =
                dao.submissions.map {it.convertToClass(dao)}

        @JvmStatic fun getSubmission(dao: AltAltContainerDao,timestamp: Long): CSubmission =
                dao.getSubmission(timestamp).convertToClass(dao)


        @JvmStatic fun putSubmissions(dao: AltAltContainerDao, submissions: List<CSubmission>) =
                submissions.forEach { putSubmission(dao,it) }

        @JvmStatic fun putSubmission(dao: AltAltContainerDao, submission: CSubmission) =
                submission.submitToDb(dao)

        /*
        TODO
        @JvmStatic fun getConversions(dao: AltAltContainerDao, mesUnit: CMesUnit)
        @JvmStatic fun putConversions(dao: AltAltContainerDao, mesUnit: CMesUnit,conversions: List<CConversion>)
        @JvmStatic fun putConversion(dao: AltAltContainerDao, mesUnit: CMesUnit,conversion: CConversion)
        */

    }
}

data class CSubmission(
    var timestamp: Long=0,
    var collection: CCollection,
    var contentId: Long
)
{
    fun submitToDb(dao: AltAltContainerDao) {
        // make sure the collection exists
        Log.d("db op","insert Collection $contentId")
        dao.insertCollection(Collection(collId = contentId))
        // recursively insert all (null or more) entries from the collection into the entries table
        collection.submitToDb(dao,0,contentId)
        // insert the submission into the submissions table
        dao.insertSubmission(Submission(timestamp=timestamp,collId=contentId))
    }

    companion object {
        @JvmStatic fun testCase()=
                CSubmission(
                        timestamp = DateHandler().timestamp,
                        contentId = 0,
                        collection = CCollection(
                                myCollId = 1,
                                tags = listOf(),
                                contents = listOf(
                                        CCollection(
                                                myCollId = 2,
                                                tags= listOf(),
                                                contents = listOf()
                                        ),
                                        CMeasurement(
                                                measurementId = 0,
                                                tags = listOf(
                                                        CTag(
                                                                tagId = 0,
                                                                tagName = "vikt",
                                                                description = "hur mycket jag väger"
                                                        )
                                                ),
                                                mesUnit = CMesUnit(
                                                        unitId = 0,
                                                        shortForm = "kg",
                                                        description = "lol",
                                                        conversions = listOf()
                                                )
                                        ),
                                        CMeasurement(
                                                measurementId = 1,
                                                tags = listOf(
                                                        CTag(
                                                                tagId = 0,
                                                                tagName = "blodsocker",
                                                                description = "blodsockernivå"
                                                        )
                                                ),
                                                mesUnit = CMesUnit(
                                                        unitId = 0,
                                                        shortForm = "mmol/l",
                                                        description = "standardformat för blodsocker i delar av Europa",
                                                        conversions = listOf(
                                                                /*
                                                                CConversion(
                                                                        fromId = 0,
                                                                        toId = ??,
                                                                        formula = " x / 18 "
                                                                )
                                                                */
                                                        )
                                                )
                                        )
                                )
                        )
                )
    }
}

/*
 Submission contains a base collection that contains collections, measurements and
 whatever else we want to have in a submission.

 Apart from that, we have a store of tags and one of measurement relations

 */

interface CContent {
    var tags: List<CTag>
    fun submitToDb(dao:AltAltContainerDao,index:Int,collId: Long)
}

data class CCollection (
        var myCollId: Long=0,
        var contents: List<CContent>,
        override var tags: List<CTag>
) : CContent {
    override fun submitToDb(dao: AltAltContainerDao,index: Int,collId: Long) {
        // Must insert collection before entries to satisfy db foreign key conditions
        Log.d("db op","insert Collection collId $collId")
        dao.insertCollection(Collection(collId=myCollId))
        Log.d("db op","inserting all children for collId $collId")
        for((i,c) in contents.withIndex()){
            c.submitToDb(dao,i,myCollId)
        }
        Log.d("db op","insert Collection Entry for ix $index, collId $collId")
        dao.insertEntry(
                Entry(pos=index.toLong(), collId=collId, type=EntryTypes.COLLECTION)
        )
        for(t in tags){
            Log.d("db op","insert Entry Tag ${t.tagId} for $index, collId $collId")
            dao.insertEntryTag(
                    EntryTag( pos=index.toLong(), collId = collId, tagId = t.tagId )
            )
        }
    }
}

data class CMeasurement (
        var measurementId: Long=0,
        var mesUnit: CMesUnit,
        override var tags: List<CTag>
) : CContent {
    override fun submitToDb(dao: AltAltContainerDao, index: Int,collId:Long) {
        Log.d("db op","insert Measurement & Entry for ix $index, collId $collId")
        dao.insertEntry(
                Entry(pos=index.toLong(), collId=collId, type=EntryTypes.MEASURE))
        dao.insertMeasurement(
                Measurement(mesId = measurementId, unitId = mesUnit.unitId)
        )
        mesUnit.submitToDb(dao)
    }
}

data class CMesUnit (
        var unitId: Long=0,
        var shortForm: String,
        var description: String,
        var conversions: List<CConversion>
) {
    fun submitToDb(dao: AltAltContainerDao){
        Log.d("db op","insert Measurement Unit $unitId ($shortForm) ($description)")
        dao.insertMesUnit(MesUnit(unitId=unitId,shortForm = shortForm,description = description))
        conversions.forEach { it.submitToDb(dao) }
    }
}

data class CConversion (
        var fromId: Long,
        var toId: Long,
        var formula: String // TODO replace this with something more suitable, maybe
) {
    fun submitToDb(dao: AltAltContainerDao) {
        Log.d("db op","insert Unit conversion from $fromId to $toId ($formula)")
        dao.insertUnitConversion(UnitConversion(from=fromId,to=toId,formula = formula))
    }
}

data class CTag(
        var tagId: Long,
        var tagName: String,
        var description: String
)

// TODO Use a "store" to handle tags instead of including them in each entry
/*
class CTagStore {
    companion object {
        fun getTagsFor(content: CContent): List<CTag> = listOf()
        fun createTagUnlessPresent(tagName: String, description: String) :Boolean = true
    }
}
*/

data class CThing(
        var thingId: Long,
        var shortDec: String,
        override var tags: List<CTag>,
        var description: String
) : CContent {
    /*
    // TODO this might be a better way to do it later, to do it more memoizable or so.
    override var tags: List<CTag>
        get() = TODO("not implemented")
        set(value) {}
        */

    override fun submitToDb(dao: AltAltContainerDao, index: Int, collId: Long) {
        dao.insertThing(Thing(thingId=thingId,shortDesc = shortDec,description = description))
    }
}


