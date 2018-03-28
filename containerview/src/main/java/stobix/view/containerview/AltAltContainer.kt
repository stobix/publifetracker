package stobix.view.containerview

import android.app.ActivityManager

/**
 * Created by stobix on 2018-03-26.
 *
 *  The thought here is that many CSubmissions can point to the same CCollection, hence I won't use
 *  a List<CContent> directly
 *
 */

class AltAltContainer(){
    companion object {
        @JvmStatic fun getAllSubmissions(dao: AltAltContainerDao): List<CSubmission> =
                dao.submissions.map {it.convertToClass(dao)}

        @JvmStatic fun getSubmission(dao: AltAltContainerDao,timestamp: Long): CSubmission =
                dao.getSubmission(timestamp).convertToClass(dao)


        @JvmStatic fun putSubmissions(dao: AltAltContainerDao, submissions: List<CSubmission>) =
                submissions.forEach { putSubmission(dao,it) }

        @JvmStatic fun putSubmission(dao: AltAltContainerDao, submission: CSubmission) =
                submission.submitToDb(dao)
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
        dao.insertCollection(Collection(collId = contentId))
        // recursively insert all (null or more) entries from the collection into the entries table
        collection.submitToDb(dao,0,contentId)
        // insert the submission into the submissions table
        dao.insertSubmission(Submission(timestamp=timestamp,collId=contentId))
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
        dao.insertEntry(
                Entry(pos=index.toLong(), collId=collId, type=EntryTypes.COLLECTION)
        )
        dao.insertCollection(Collection(collId=myCollId))
        for((i,c) in contents.withIndex()){
            c.submitToDb(dao,i,myCollId)
        }
        for(t in tags){
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
        dao.insertEntry(
                Entry(pos=index.toLong(), collId=collId, type=EntryTypes.MEASURE))
    }
}

data class CMesUnit (
        var unitId: Long=0,
        var shortForm: String,
        var description: String,
        var conversions: List<CConversion>
)

data class CConversion (
        var fromId: Long,
        var toId: Long,
        var formula: String // TODO replace this with something more suitable, maybe
)

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
