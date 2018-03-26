package stobix.view.containerview

import android.app.ActivityManager

/**
 * Created by stobix on 2018-03-26.
 */
data class CSubmission(
    var timestamp: Long=0,
    var collection: CCollection,
    var contentId: Long
)
{
    fun submitToDb(dao: AltAltContainerDao) {
        dao.insertCollection(Collection(collId = contentId))
        collection.submitToDb(dao,0,contentId)
        dao.insertSubmission(Submission(timestamp=timestamp,collId=contentId))
    }
}

/*
 Submission contains a base collection that contains collections, measurements and
 whatever else we want to have in a submission.

 Apart from that, we have a store of tags and one of measurement relations

 */

interface CContent {
    var tags: List<Tag>
    fun submitToDb(dao:AltAltContainerDao,index:Int,collId: Long)
}

data class CCollection (
        var extId: Long=0,
        var contents: List<CContent>,
        override var tags: List<Tag>
) : CContent {
    override fun submitToDb(dao: AltAltContainerDao,index: Int,collId: Long) {
        dao.insertEntry(
                Entry(pos=index.toLong(), collId=collId, type=EntryTypes.COLLECTION)
        )
        dao.insertCollection(Collection(collId=extId))
        for((i,c) in contents.withIndex()){
            c.submitToDb(dao,i,extId)
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
        override var tags: List<Tag>
) : CContent {
    override fun submitToDb(dao: AltAltContainerDao, index: Int,collId:Long) {
        dao.insertEntry(
                Entry(pos=index.toLong(), collId=collId, type=EntryTypes.MEASURE))
    }
}

data class CTag(
        var tagId: Long,
        var tagName: String,
        var description: String
)

class CTagStore {
    companion object {
        fun getTagsFor(content: CContent): List<Tag> = listOf()
        fun createTagUnlessPresent(tagName: String, description: String) :Boolean = true
    }
}
