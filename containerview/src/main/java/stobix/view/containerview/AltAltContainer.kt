package stobix.view.containerview

import android.arch.persistence.room.*

/*
@Database(
        entities = [
            Submission::class,
            Entry::class,
            EntryTag::class,
            Tag::class,
            Measurement::class,
            MesUnit::class,
            UnitConversion::class],
        version=1
)
abstract class AltAltDatabase : RoomDatabase(){
    abstract fun containerDao(): AltAltContainerDao
}

@Dao
interface AltAltContainerDao{
    @Query("select * from submissions where timestamp == :timestamp limit 1")
    fun getSubmission(timestamp:Long): Submission
    @Query("select * from collections where collId == :collId limit 1")
    fun getCollection(collId: Long): Collection
    @Query("select * from entries where collId == :collId order by pos")
    fun getEntriesFor(collId: Long): List<Entry>
    @Query("select tagId from entry_tags where collId == :collId and pos == :pos")
    fun getEntryTagsFor(collId: Long,pos: Long): List<Long>
    @Query("select tags.* from entry_tags join tags using ( tagId ) where collId == :collId and pos == :pos")
    fun getTagsFor(collId: Long,pos: Long): List<Tag>

    @Insert
    fun insertSubmission(submission:Submission)
    @Insert
    fun insertCollection(collection: Collection)
    @Insert
    fun insertEntry(entry:Entry)
    @Insert
    fun insertEntries(entries:List<Entry>)
    @Insert
    fun insertEntryTag(entryTag: EntryTag)
    @Insert
    fun insertEntryTag(pos: Long,collId: Long,tagId: Long)
    @Insert
    fun insertTag(tag: Tag)
    @Insert
    fun insertMeasurement(measurement: Measurement)
    @Insert
    fun insertMesUnit(mesUnit: MesUnit)
    @Insert
    fun insertUnitConversion(unitConversion: UnitConversion)
}
*/

@Entity(tableName="submissions",
        foreignKeys =[
            ForeignKey(entity=Collection::class,
                    parentColumns= ["collId"],
                    childColumns = ["collId"])]
        , indices = [Index("collId")] )
data class Submission constructor(
        @PrimaryKey
        var timestamp: Long=0,
        var collId: Long=0
)

@Entity(tableName = "collections")
data class Collection constructor(
        @PrimaryKey
        var collId: Long
)

@Entity(tableName = "entries",
        primaryKeys = ["pos", "collId"],
        foreignKeys = [
                ForeignKey(
                        entity = Collection::class,
                        parentColumns = ["collId"],
                        childColumns = ["collId"])]
        , indices = [Index("collId")] )
data class Entry constructor(
        var pos: Long,
        var collId: Long=0,
        var type: Int=0,
        // A foreign key from either the collection or measurement table, depending on the contents of "type" above
        var extId: Long=0
)

enum class EntryTypes{ MEASURE, COLLECTION }

@Entity(tableName = "entry_tags",
        primaryKeys = ["pos", "collId"],
        indices = [Index("tagId")])
data class EntryTag constructor(
        var pos: Long=0,
        var collId: Long=0,
        var tagId: Long=0
)

@Entity(tableName = "tags")
data class Tag constructor(
        @PrimaryKey
        var tagId: Long=0,
        var tag: String="",
        var description: String=""
)

@Entity(tableName = "measurements",
        foreignKeys = [
            ForeignKey(
                    entity = MesUnit::class,
                    parentColumns = ["unitId"],
                    childColumns = ["unitId"])],
        indices = [Index("unitId")] )
data class Measurement constructor(
        @PrimaryKey
        var mesId: Long=0,
        var value: Float=0f,
        var unitId: Long=0
)

@Entity(tableName = "measurement_units")
data class MesUnit constructor(
        @PrimaryKey
        var unitId: Long=0,
        var shortForm: String="",
        var description: String=""
)

@Entity(tableName = "unit_conversions",
        primaryKeys = ["from"],
        indices = [Index(value = ["from","to"])])
data class UnitConversion constructor(
        var from: Long=0,
        var to: Long=0,
        var formula: String=""
)

