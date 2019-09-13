@file:Suppress("KDocMissingDocumentation")

package stobix.app.lifetracker

import android.arch.persistence.room.Ignore
import android.os.Parcel
import android.os.Parcelable

/**
 * A bucket that contains a float value with an int backend
 */
open class FloatyIntBucket(open var timestamp: Long, open var value: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readInt()
    ) {
    }

    /**
     * Parcel APi
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(timestamp)
        parcel.writeInt(value)
    }

    /**
     * Parcel APi
     */
    override fun describeContents(): Int {
        return 0
    }

    /**
     *
     */
    companion object CREATOR: Parcelable.Creator<FloatyIntBucket> {
        /**
         * Parcel APi
         */
        override fun createFromParcel(parcel: Parcel): FloatyIntBucket {
            return FloatyIntBucket(parcel)
        }

        /**
         * Parcel APi
         */
        override fun newArray(size: Int): Array<FloatyIntBucket?> {
            return arrayOfNulls(size)
        }
    }
}

/**
 * A timestamp range that can be converted to a [FloatyIntBucket] with the value in hours
 */
data class RangeBucketHours(var timestamp: Long, var endTimestamp: Long) {
    fun toFloatyIntBucket() = FloatyIntBucket(
            timestamp, (((endTimestamp-timestamp)/3600_00).toInt())
    )

}

/**
 * A timestamp range that can be converted to a [FloatyIntBucket] with the value in minutes
 */
data class RangeBucketMinutes(var timestamp: Long, var endTimestamp: Long) {
    fun toFloatyIntBucket() = FloatyIntBucket(timestamp, (((endTimestamp-timestamp)/60_00).toInt()))
}

/**
 * A timestamp range that can be converted to a [FloatyIntBucket] with the value in seconds
 */
data class RangeBucketSeconds(var timestamp: Long, var endTimestamp: Long) {
    fun toFloatyIntBucket() = FloatyIntBucket(timestamp, (((endTimestamp-timestamp)/100).toInt()))
}

/**
 * A bucket that contains a long value
 */
data class LongBucket(var timestamp: Long, var value: Long) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(timestamp)
        parcel.writeLong(value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LongBucket> {
        override fun createFromParcel(parcel: Parcel): LongBucket {
            return LongBucket(parcel)
        }

        override fun newArray(size: Int): Array<LongBucket?> {
            return arrayOfNulls(size)
        }
    }
}


/**
 * A bucket that contains a string value
 */
data class StringBucket(var timestamp: Long, var value: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString()
    ) {
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

