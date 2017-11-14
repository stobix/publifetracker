package com.example.stobix.myapplication;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import static android.arch.persistence.room.ColumnInfo.INTEGER;

/**
 * Created by stobix on 11/9/17.
 */

@Entity(tableName="sugar_entries")
public class SugarEntry implements Parcelable{

    SugarEntry(int uid, long epochTimestamp,int sugarLevel,String extra){
        this.uid=uid;
        this.epochTimestamp=epochTimestamp;
        this.sugarLevel=sugarLevel;
        this.extra=extra;
    }

    SugarEntry(int uid, Date date, int sugarLevel, String extra){
        this(uid,date.getTime(),sugarLevel,extra);
    }

    @PrimaryKey
    int uid;

    @ColumnInfo(name = "timestamp", typeAffinity = INTEGER)
    // FIXME Can't make this a java.sql.Timestamp or java.util.Date lest the compiler complains
    long epochTimestamp;

    @ColumnInfo(name = "sugar")
    int sugarLevel;

    @ColumnInfo(name = "extra")
    String extra;

    @Override
    public int describeContents() {
        return 0;
    }

    private SugarEntry(Parcel parcel) {
        // IMPORTANT: These calls need to be in the same order as in writeToParcel below!
        this(   parcel.readInt(), // uid
                parcel.readLong(), // timestamp
                parcel.readInt(), // sugar
                parcel.readString()); // extra
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(uid);
        parcel.writeLong(epochTimestamp);
        parcel.writeInt(sugarLevel);
        parcel.writeString(extra);
    }

    public static final Parcelable.Creator<SugarEntry> CREATOR
            = new Parcelable.Creator<SugarEntry>() {
        public SugarEntry createFromParcel(Parcel parcel) {
            return new SugarEntry(parcel);
        }

         public SugarEntry[] newArray(int size) {
             return new SugarEntry[size];
         }
    };

}
