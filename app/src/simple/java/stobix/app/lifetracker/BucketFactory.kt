package stobix.app.lifetracker

import android.os.Parcel
import android.os.Parcelable

/*
enum class Datamängd() {
    BLODSOCKER {
        override val värden=
                { dao: SugarEntryDao, startdatum: Long, slutdatum: Long, understräng: String ->
                    dao.getAllSugarBuckets(startdatum, slutdatum)
                }
    },
    VIKT {
        override val värden=
                { dao: SugarEntryDao, startdatum: Long, slutdatum: Long, understräng: String ->
                    dao.getAllWeightBuckets(startdatum, slutdatum)
                }
    },
    STRÄNG {
        override val värden=
                { dao: SugarEntryDao, startdatum: Long, slutdatum: Long, understräng: String ->
                    dao.getCompletedIntervalsLike(understräng, startdatum, slutdatum).map { it.toFloatyIntBucket() }
                }
    },
    ;

    abstract val värden: (dao: SugarEntryDao, startdatum: Long, slutdatum: Long, understräng:String)->List<FloatyIntBucket>
}
 */

abstract class Datamängd() : Parcelable {

    abstract fun värden(dao:SugarEntryDao, startdatum: Long, slutdatum: Long): List<FloatyIntBucket>
}

class Blodsockerdata() : Datamängd() {
    constructor(parcel: Parcel) : this() {
    }

    override fun värden(dao: SugarEntryDao, startdatum: Long, slutdatum: Long) =
        dao.getAllSugarBuckets(startdatum, slutdatum)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Blodsockerdata> {
        override fun createFromParcel(parcel: Parcel): Blodsockerdata {
            return Blodsockerdata(parcel)
        }

        override fun newArray(size: Int): Array<Blodsockerdata?> {
            return arrayOfNulls(size)
        }
    }
}

class Viktdata() : Datamängd() {
    constructor(parcel: Parcel) : this() {
    }

    override fun värden(dao: SugarEntryDao, startdatum: Long, slutdatum: Long)=
        dao.getAllWeightBuckets(startdatum, slutdatum)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Viktdata> {
        override fun createFromParcel(parcel: Parcel): Viktdata {
            return Viktdata(parcel)
        }

        override fun newArray(size: Int): Array<Viktdata?> {
            return arrayOfNulls(size)
        }
    }
}

class Intervalldata(val understräng: String) : Datamängd(){
    constructor(parcel: Parcel) : this(parcel.readString()) {
    }

    override fun värden(dao: SugarEntryDao, startdatum: Long, slutdatum: Long)=
        dao.getCompletedIntervalsLike(understräng, startdatum, slutdatum).map { it.toFloatyIntBucket() }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(understräng)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Intervalldata> {
        override fun createFromParcel(parcel: Parcel): Intervalldata {
            return Intervalldata(parcel)
        }

        override fun newArray(size: Int): Array<Intervalldata?> {
            return arrayOfNulls(size)
        }
    }

}
