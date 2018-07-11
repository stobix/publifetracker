package stobix.app.lifetracker

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.ColumnInfo.INTEGER
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import stobix.utils.DateHandler

private fun Parcel.writeNullableInt(i:Int?){
    if (i != null) {
        this.writeInt(1)
        this.writeInt(i)
    }
    else
        this.writeInt(0)
}

private fun Parcel.readNullableInt()=
        if (this.readInt() != 0)
            this.readInt()
        else
            null

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
        @PrimaryKey @ColumnInfo(name = "timestamp", typeAffinity = INTEGER) var timestamp: Timestamp=0,
        @ColumnInfo(name = "sugar") var sugarLevel: Int?=null,
        @ColumnInfo(name = "extra") var extra: String?=null,
        @ColumnInfo(name = "weight") var weight: Int?=null,
        @ColumnInfo(name = "treatment") var treatment: String?=null,
        @ColumnInfo(name = "food") var food: String?=null,
        @ColumnInfo(name = "drink") var drink: String?=null
) : Parcelable {


    // Parcel function
    override fun describeContents(): Int = 0

    // Parcel function
    // IMPORTANT: These calls need to be in the same order as in writeToParcel below!
    private constructor(parcel: Parcel) : this(
            parcel.readLong(), // timestamp
            parcel.readNullableInt(), // sugar
            parcel.readString(), // extra
            parcel.readNullableInt(), // weight
            parcel.readString(), // treatment
            parcel.readString(), // food
            parcel.readString() // drink
    )

    fun copyToCurrent() =
            copy(timestamp = DateHandler().timestamp)

    // Parcel function
    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeLong(timestamp)
        parcel.writeNullableInt(sugarLevel)
        parcel.writeString(extra)
        parcel.writeNullableInt(weight)
        parcel.writeString(treatment)
        parcel.writeString(food)
        parcel.writeString(drink)
    }

    companion object CREATOR: Parcelable.Creator<SugarEntry> {

        // Parcel function
        override fun createFromParcel(parcel: Parcel): SugarEntry = SugarEntry(parcel)
        override fun newArray(size: Int): Array<SugarEntry?> = arrayOfNulls(size)
    }


    // (obsolete) Used to check for equality between SugarEntries with different UID's, which were dropped completely from the SugarEntry model.
    // Still keeping it, in case some other "non-equality" SugarEntry member gets added
    infix fun sameAs(other:Any?) = when(other){
        is SugarEntry ->
            this.timestamp == other.timestamp
            && this sameValuesAs other
        else ->
                false
    }

    infix fun sameValuesAs(other:SugarEntry) =
            this.sugarLevel == other.sugarLevel
                    && this.extra == other.extra
                    && this.weight == other.weight
                    && this.treatment == other.treatment
                    && this.food == other.food
                    && this.drink == other.drink



    private fun <A>compareNullables(a: A?, b: A?, comparator: (A, A) -> Int) =
            a ?. let { first ->
                b ?. let { second ->
                    comparator(first,second)
                } ?: 1
            } ?: -1


    // These are for comparing in Java, since Java has no counterpart to ?.let
    fun compareSugar(that: SugarEntry) =
            compareNullables(this.sugarLevel,that.sugarLevel) { a, b-> a-b}

    fun compareWeight(that: SugarEntry) =
            compareNullables(this.weight,that.weight) { a, b-> a-b}

    fun compareExtra(that: SugarEntry) =
            compareNullables(this.extra,that.extra) { a, b-> a.compareTo(b)}

    fun compareTreatment(that: SugarEntry) =
            compareNullables(this.treatment,that.treatment) {a,b -> a.compareTo(b)}

    fun compareFood(that: SugarEntry) =
            compareNullables(this.food,that.food) {a,b -> a.compareTo(b)}

    fun compareDrink(that: SugarEntry) =
            compareNullables(this.drink,that.drink) {a,b -> a.compareTo(b)}
}

