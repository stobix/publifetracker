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
){
    fun convertToClass(dao: AltAltContainerDao): CSubmission {
        val contents : List<CContent> =
                dao.getEntriesFor(collId)
                . sortedBy {it.pos}
                . map {it.convertToClass(dao)}

        val tags = dao.getTagsFor( collId, 0 )
                . map {it.convertToClass(dao)}

        return CSubmission(
                timestamp=timestamp,
                collection=CCollection(
                        myCollId = collId,
                        contents = contents,
                        tags= tags),
                contentId=collId)
    }
}

// This table is necessary simply since we can have a collection of zero entries,
// which means that we can't make collId for submissions a foreign key from
// entries directly.
@Entity(tableName = "collections")
data class Collection constructor(
        @PrimaryKey(autoGenerate = true)
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
        @TypeConverters(value=[EntryConverters::class])
        var type: EntryTypes = EntryTypes.COLLECTION,
        // A "foreign key" from either the collection or measurement table, depending on the contents of "type" above
        var extId: Long=0
) {
    fun convertToClass(dao: AltAltContainerDao): CContent=
        when (type){
            EntryTypes.COLLECTION ->
                CCollection(
                        myCollId = extId,
                        contents = dao.getEntriesFor(extId)
                                .sortedBy { it.pos }
                                .map { it.convertToClass(dao) },
                        tags = dao.getTagsFor(extId, pos)
                                .map { it.convertToClass(dao)}
                )

            EntryTypes.MEASURE ->
                CMeasurement(
                        measurementId = extId,
                        mesUnit = dao.getMesUnitFor(extId).convertToClass(dao),
                        tags = dao.getTagsFor(extId,pos)
                                .map { it.convertToClass(dao)}
                )

            EntryTypes.THING -> {
                val thing=dao.getThing(extId)
                CThing(
                        thingId=extId,
                        shortDec = thing.shortDesc,
                        description = thing.description,
                        tags = dao.getTagsFor(extId,pos)
                                .map { it.convertToClass(dao) }
                )
            }
        }
}

/*
    A Thing in this context is anything that is something and has a description.
    (Basically an empty collection with an internal tag)
    Any quantities and units are provided by putting the "thing" in a collection together with
    a Measurement and other Things
 */
@Entity(tableName = "things")
data class Thing constructor(
        @PrimaryKey(autoGenerate = true)
        var thingId: Long=0,
        var shortDesc: String="",
        var description: String=""
        )


enum class EntryTypes{ MEASURE, COLLECTION, THING }


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
        @PrimaryKey(autoGenerate = true)
        var tagId: Long=0,
        var tag: String="",
        var description: String=""
) {
    fun convertToClass(dao: AltAltContainerDao): CTag =
            CTag(
                    tagId = tagId,
                    tagName = tag,
                    description = description
            )
}

@Entity(tableName = "measurements",
        foreignKeys = [
            ForeignKey(
                    entity = MesUnit::class,
                    parentColumns = ["unitId"],
                    childColumns = ["unitId"])],
        indices = [Index("unitId")] )
data class Measurement constructor(
        @PrimaryKey(autoGenerate = true)
        var mesId: Long=0,
        var value: Float=0f,
        var unitId: Long=0
)

@Entity(tableName = "measurement_units")
data class MesUnit constructor(
        @PrimaryKey(autoGenerate = true)
        var unitId: Long=0,
        var shortForm: String="",
        var description: String=""
) {
    fun convertToClass(dao: AltAltContainerDao): CMesUnit =
            CMesUnit(
                    unitId = unitId,
                    shortForm = shortForm,
                    description = description,
                    conversions = dao.getConversionsForUnit(unitId)
                            .map {it.convertToClass(dao)}
            )

}

@Entity(tableName = "unit_conversions",
        primaryKeys = ["from"],
        indices = [Index(value = ["from","to"])])
data class UnitConversion constructor(
        var from: Long=0,
        var to: Long=0,
        var formula: String=""
) {
    fun convertToClass(dao: AltAltContainerDao): CConversion =
            CConversion( fromId = from, toId = to, formula = formula )
}

