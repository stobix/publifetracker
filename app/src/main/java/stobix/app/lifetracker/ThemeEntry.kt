package stobix.app.lifetracker


/*
import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
/*
/**
 * An entity containing an app color theme, including app specific colors
 */
@Entity(tableName="colors")
data class DbEntryColor(
        @PrimaryKey(autoGenerate = true)
        var uid:Int=0, // TODO: Replace with a (themeName,colorName) primary key
        var themeName:String="",
        var colorName:String="",
        var colorValue:String=""
): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(uid)
        parcel.writeString(themeName)
        parcel.writeString(colorName)
        parcel.writeString(colorValue)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DbEntryColor> {
        override fun createFromParcel(parcel: Parcel): DbEntryColor {
            return DbEntryColor(parcel)
        }

        override fun newArray(size: Int): Array<DbEntryColor?> {
            return arrayOfNulls(size)
        }
    }
}

// TODO add a converter so one ThemeEntry becomes many colors entries and one themes entry in the database
@Entity(tableName = "themes")
class ThemeEntry constructor(
        @PrimaryKey var uid: Int = 0,
        @ColumnInfo(name="colorEntries")
        var colors:Array<DbEntryColor> = Array(0,{DbEntryColor()}),
        var parent:String?=null
) : Parcelable
{
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.createTypedArray(DbEntryColor.CREATOR)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(uid)
        parcel.writeTypedArray(colors,0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ThemeEntry> {
        override fun createFromParcel(parcel: Parcel): ThemeEntry {
            return ThemeEntry(parcel)
        }

        override fun newArray(size: Int): Array<ThemeEntry?> {
            return arrayOfNulls(size)
        }
    }
}
*/