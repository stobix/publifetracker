package stobix.view.containerview

import android.arch.persistence.room.*

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

// This table is neccessary simply since we can have a collection of zero entries,
// which means that we can't make collId for submissions a foreign key from entries directly.
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
data class Entry (
        var pos: Long=0,
        var collId: Long=0,
        @TypeConverters(value=EntryConverters::class)
        var type: EntryTypes = EntryTypes.COLLECTION,
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

class EntryConverters {
    companion object {

        @TypeConverter
        @JvmStatic
        fun fromEntryType(et: EntryTypes) = et.ordinal.toLong()

        @TypeConverter
        @JvmStatic
        fun toEntryType(l: Long) = EntryTypes.values()[l.toInt()]
    }
}

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

