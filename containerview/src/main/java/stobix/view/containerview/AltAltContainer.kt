package stobix.view.containerview

import android.arch.persistence.room.*

/**
 * Created by stobix on 2018-03-05.
 */
@Entity(foreignKeys =[
            ForeignKey(entity=Collection::class,
                    parentColumns= ["id"],
                    childColumns = ["collId"])])
data class Submission(
        @PrimaryKey
        var timestamp: Long,
        var collId: Long
)

@Entity
data class Collection(
        @PrimaryKey
        var id: Long
)

@Entity(primaryKeys = ["pos", "collId"],
        foreignKeys = [
                ForeignKey(
                        entity = Collection::class,
                        parentColumns = ["id"],
                        childColumns = ["collID"])])
data class Entry(
        var pos: Long,
        var collId: Long,
        var type: Long,
        // A foreign key from either the collection or measurement table, depending on the contents of "type" above
        var extId: Long
)

@Entity(primaryKeys = ["pos", "collId"],
        indices = [Index("tagId")])
data class EntryTag(
        var pos: Long,
        var collId: Long,
        var tagId: Long
)

@Entity
data class Tag(
        @PrimaryKey
        var id: Long,
        var tag: String,
        var description: String
)

@Entity(foreignKeys = [
            ForeignKey(
                    entity = MesUnit::class,
                    parentColumns = ["id"],
                    childColumns = ["unitId"])])
data class Measurement(
        @PrimaryKey
        var id: Long,
        var value: Float,
        var unitId: Long
)

@Entity
data class MesUnit(
        @PrimaryKey
        var id: Long,
        var shortForm: String,
        var description: String
)

@Entity(primaryKeys = ["from"],
        indices = [Index(value = ["from","to"])])
data class UnitConversion(
        var from: Long,
        var to: Long,
        var formula: String
)

