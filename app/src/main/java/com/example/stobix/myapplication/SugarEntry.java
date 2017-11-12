package com.example.stobix.myapplication;

import java.util.Date;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import static android.arch.persistence.room.ColumnInfo.BLOB;
import static android.arch.persistence.room.ColumnInfo.INTEGER;

/**
 * Created by stobix on 11/9/17.
 */

@Entity(tableName="sugar_entries")
public class SugarEntry implements Parcelable{

    SugarEntry(int uid, Date date,int sugarLevel,String extra){
        this(uid,date.getTime(),sugarLevel,extra);
    }

    SugarEntry(int uid, long epochTimestamp,int sugarLevel,String extra){
        this.uid=uid;
        this.epochTimestamp=epochTimestamp;
        this.sugarLevel=sugarLevel;
        this.extra=extra;
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

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(uid);
        parcel.writeLong(epochTimestamp);
        parcel.writeInt(sugarLevel);
        parcel.writeString(extra);
    }

    private SugarEntry(Parcel in) {
        // Since the call to this needs to be first for whatever reason, I can't
        // assign these values to variables before sending them to the main
        // constructor.

        // IMPORTANT: These calls need to be in the same order as in writeToParcel above!
        this(   in.readInt(), // uid
                in.readLong(), // timestamp
                in.readInt(), // sugar
                in.readString()); // extra
    }

    public static final Parcelable.Creator<SugarEntry> CREATOR
            = new Parcelable.Creator<SugarEntry>() {
        public SugarEntry createFromParcel(Parcel in) {
            return new SugarEntry(in);
        }

         public SugarEntry[] newArray(int size) {
             return new SugarEntry[size];
         }
    };

}
