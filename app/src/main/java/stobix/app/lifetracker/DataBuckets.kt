package stobix.app.lifetracker

import android.os.Parcel
import android.os.Parcelable

data class FloatyIntBucket(var timestamp: Long, var value: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(timestamp)
        parcel.writeInt(value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FloatyIntBucket> {
        override fun createFromParcel(parcel: Parcel): FloatyIntBucket {
            return FloatyIntBucket(parcel)
        }

        override fun newArray(size: Int): Array<FloatyIntBucket?> {
            return arrayOfNulls(size)
        }
    }
}

data class StringBucket(var timestamp: Long, var value: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(timestamp)
        parcel.writeString(value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StringBucket> {
        override fun createFromParcel(parcel: Parcel): StringBucket {
            return StringBucket(parcel)
        }

        override fun newArray(size: Int): Array<StringBucket?> {
            return arrayOfNulls(size)
        }
    }
}

