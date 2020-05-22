package stobix.app.lifetracker

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.ColumnInfo.INTEGER
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import stobix.utils.DateHandler

private fun Parcel.writeNullableInt(i: Int?) =
        nullableWriterWrapper(i) { this.writeInt(it) }

private fun Parcel.readNullableInt() =
        nullableReaderWrapper { readInt() }

private fun Parcel.writeNullableLong(i: Long?) =
        nullableWriterWrapper(i) { this.writeLong(it) }

private fun Parcel.readNullableLong() =
        nullableReaderWrapper { readLong() }

private fun Parcel.writeNullableDouble(d: Double?) =
        nullableWriterWrapper(d) { writeDouble(it) }

private fun Parcel.readNullableDouble() =
        nullableReaderWrapper { readDouble() }

private fun <A> Parcel.nullableWriterWrapper(a: A?, fn: (A)->Unit) =
        if (a != null) {
            this.writeInt(1)
            fn(a)
        } else
            this.writeInt(0)

private fun <A> Parcel.nullableReaderWrapper(fn: ()->A) =
        if (this.readInt() != 0)
            fn()
        else
            null


// The Room database entry class/TableView row class that acts as a glue between the two.

/**
 * An entry in both the database table and the table view for the app. Contains all data needed for an event.
 */
@Entity(tableName = "sugar_entries")
/*
    room wants an empty constructor in kotlin to be able to parse the file.
    Otherwise it complains about not finding a suitable constructor, eventhough the
    constructors provided should be equivalent to the Java versions.

    This is probably needed due to kotlin not using java.lang.String for its strings. Maybe.

    By setting a default value for all constructor parameters, I get an empty constructor for free.
    */
data class SugarEntry constructor(
        /**
         * When the thing happened
         */
        @PrimaryKey @ColumnInfo(name = "timestamp", typeAffinity = INTEGER)
        var timestamp: Timestamp = 0,
        /**
         * The timestamp for when the thing ended, if applicable
         */
        @ColumnInfo(name = "end_timestamp") var endTimestamp: Timestamp? = null,
        /**
         * Blood sugar level at the time
         */
        @ColumnInfo(name = "sugar") var sugarLevel: Int? = null,
        /**
         * Any extra info about the event
         */
        @ColumnInfo(name = "extra") var extra: String? = null,
        /**
         * An optional 'category' for graphs and the likes
         */
        @ColumnInfo(name = "category") var category: String? = null,
        /**
         * Current weight
         */
        @ColumnInfo(name = "weight") var weight: Int? = null,
        /**
         * Current insulin level, in mmol/l
         */
        @ColumnInfo(name = "insulin") var insulin: Double? = null,
        /**
         * Any form of treatment - pills, sugar, going for a walk
         */
        @ColumnInfo(name = "treatment") var treatment: String? = null,
        /**
         * What the user ate
         */
        @ColumnInfo(name = "food") var food: String? = null,
        /**
         * What the user drank
         */
        @ColumnInfo(name = "drink") var drink: String? = null
) : Parcelable {


    /**
     * Part of the Parcel API
     */
    override fun describeContents(): Int = 0

    /**
     * Part of the Parcel API
     */
    // IMPORTANT: These calls need to be in the same order as in writeToParcel below!
    private constructor(parcel: Parcel) : this(
            parcel.readLong(), // timestamp
            parcel.readNullableLong(), // end timestamp
            parcel.readNullableInt(), // sugar
            parcel.readString(), // extra
            parcel.readString(), // category
            parcel.readNullableInt(), // weight
            parcel.readNullableDouble(),//insulin
            parcel.readString(), // treatment
            parcel.readString(), // food
            parcel.readString() // drink
    )

    /**
     * Creates a copy with timestamp set to now.
     */
    fun copyToCurrent() =
            copy(timestamp = DateHandler().timestamp, endTimestamp = null)

    /**
     * Part of the Parcel API
     */
    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeLong(timestamp)
        parcel.writeNullableLong(endTimestamp)
        parcel.writeNullableInt(sugarLevel)
        parcel.writeString(extra)
        parcel.writeString(category)
        parcel.writeNullableInt(weight)
        parcel.writeNullableDouble(insulin)
        parcel.writeString(treatment)
        parcel.writeString(food)
        parcel.writeString(drink)
    }

    @Suppress("KDocMissingDocumentation")
    companion object CREATOR : Parcelable.Creator<SugarEntry> {

        /**
         * Part of the Parcel API
         */
        override fun createFromParcel(parcel: Parcel): SugarEntry = SugarEntry(parcel)

        /**
         * Dunno. Is this even used anymore?
         */
        override fun newArray(size: Int): Array<SugarEntry?> = arrayOfNulls(size)
    }


    /**
     *
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated(
            "Used to check for equality between SugarEntries with different UID's, which were dropped completely from the SugarEntry model."
    )
    infix fun sameAs(other: Any?) = when (other) {
        is SugarEntry ->
            this.timestamp == other.timestamp
                    && (if (this.endTimestamp != null) this.endTimestamp == other.endTimestamp else true)
                    && this sameValuesAs other
        else          ->
            false
    }

    /**
     * Returns whether everything apart from the timestamps are equal
     * TODO maybe add endTime comparison as well?
     */
    infix fun sameValuesAs(other: SugarEntry) =
            this.sugarLevel == other.sugarLevel
                    && this.extra == other.extra
                    && this.category == other.category
                    && this.weight == other.weight
                    && this.treatment == other.treatment
                    && this.insulin == other.insulin
                    && this.food == other.food
                    && this.drink == other.drink


    private fun <A> compareNullables(a: A?, b: A?, comparator: (A, A)->Int) =
            a?.let { first ->
                b?.let { second ->
                    comparator(first, second)
                } ?: 1
            } ?: -1


    fun containsOtherThanCategory() =
            extra ?: weight ?: treatment ?: insulin ?: food ?: drink != null
    /**
     * These are for comparing in Java, since Java has no counterpart to ?.let
     */
    fun compareSugar(that: SugarEntry) =
            compareNullables(this.sugarLevel, that.sugarLevel) { a, b -> a-b }

    /**
     * These are for comparing in Java, since Java has no counterpart to ?.let
     */
    fun compareWeight(that: SugarEntry) =
            compareNullables(this.weight, that.weight) { a, b -> a-b }

    /**
     * These are for comparing in Java, since Java has no counterpart to ?.let
     */
    fun compareExtra(that: SugarEntry) =
            compareNullables(this.extra, that.extra) { a, b -> a.compareTo(b) }

    /**
     * These are for comparing in Java, since Java has no counterpart to ?.let
     */
    fun compareCategory(that: SugarEntry) =
            compareNullables(this.category, that.category) { a, b -> a.compareTo(b) }

    /**
     * These are for comparing in Java, since Java has no counterpart to ?.let
     */
    fun compareTreatment(that: SugarEntry) =
            compareNullables(this.treatment, that.treatment) { a, b -> a.compareTo(b) }

    /**
     * These are for comparing in Java, since Java has no counterpart to ?.let
     */
    fun compareFood(that: SugarEntry) =
            compareNullables(this.food, that.food) { a, b -> a.compareTo(b) }

    /**
     * These are for comparing in Java, since Java has no counterpart to ?.let
     */
    fun compareDrink(that: SugarEntry) =
            compareNullables(this.drink, that.drink) { a, b -> a.compareTo(b) }

    /**
     * These are for comparing in Java, since Java has no counterpart to ?.let
     */
    fun compareInsulin(that: SugarEntry) =
            compareNullables(this.insulin, that.insulin) { a, b -> a.compareTo(b) }
}

