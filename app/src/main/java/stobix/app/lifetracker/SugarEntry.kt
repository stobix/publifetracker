package stobix.app.lifetracker

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.ColumnInfo.INTEGER
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import stobix.utils.DateHandler

// The Room database entry class/TableView row class that acts as a glue between the two.

@Entity(tableName = "sugar_entries")
/*
    room wants an empty constructor in kotlin to be able to parse the file.
    Otherwise it complains about not finding a suitable constructor, eventhough the
    constructors provided should be equivalent to the Java versions.

    This is probably needed due to kotlin not using java.lang.String for its strings. Maybe.

    By setting a default value for all constructor parameters, I get an empty constructor for free.
    */
data class SugarEntry constructor(
        @PrimaryKey var uid: Int=0, // TODO Should this even have a default value?
        @ColumnInfo(name = "timestamp", typeAffinity = INTEGER) var epochTimestamp: Long=0, // TODO Should this be a nullable in the database?
        @ColumnInfo(name = "sugar") var sugarLevel: Int=-1, // TODO Should this be a nullable in the database?
        @ColumnInfo(name = "extra") var extra: String?=null,
        @ColumnInfo(name = "weight") var weight: Int?=null
) : Parcelable {

    // The rest of this file describes how to destruct a SugarEntry into a Parcel,
    // and how to get it back.

    override fun describeContents(): Int = 0

    // IMPORTANT: These calls need to be in the same order as in writeToParcel below!
    private constructor(parcel: Parcel) : this(
            parcel.readInt(), // uid
            parcel.readLong(), // timestamp
            parcel.readInt(), // sugar
            parcel.readString(), // extra
            if(parcel.readInt()!=0) parcel.readInt() else null

    )

    fun copyToCurrentWithId(uid: Int) =
            copy(uid=uid,epochTimestamp = DateHandler().timestamp)


    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(uid)
        parcel.writeLong(epochTimestamp)
        parcel.writeInt(sugarLevel)
        parcel.writeString(extra)
        val weight = weight
        if (weight == null) {
            parcel.writeInt(0)
        } else {
            parcel.writeInt(1)
            parcel.writeInt(weight)
        }
    }

    companion object CREATOR: Parcelable.Creator<SugarEntry> {
            override fun createFromParcel(parcel: Parcel): SugarEntry = SugarEntry(parcel)
            override fun newArray(size: Int): Array<SugarEntry?> = arrayOfNulls(size)
    }

    infix fun sameAs(other:Any?) = when(other){
        is SugarEntry ->
            this.epochTimestamp == other.epochTimestamp
                    && this.sugarLevel == other.sugarLevel
                    && this.extra == other.extra
                    && this.weight == other.weight
        else ->
                false
    }


}
