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

    public SugarEntry(int uid, Date date,int sugarLevel,String extra){
        this(uid,date.getTime(),sugarLevel,extra);
    }

    public SugarEntry(int uid, long epochTimestamp,int sugarLevel,String extra){
        this.uid=uid;
        this.epochTimestamp=epochTimestamp;
        this.sugarLevel=sugarLevel;
        this.extra=extra;
    }

    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "timestamp", typeAffinity = INTEGER)
    // FIXME Can't make this a java.sql.Timestamp or java.util.Date lest the compiler comlains
    public long epochTimestamp;

    @ColumnInfo(name = "sugar")
    public int sugarLevel;

    @ColumnInfo(name = "extra")
    public String extra;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(uid);
        parcel.writeInt(sugarLevel);
        parcel.writeLong(epochTimestamp);
        parcel.writeString(extra);
    }

    public SugarEntry(Parcel in) {
        //this(in.readInt(),in.readInt(),in.readLong(),in.readString());
        this.uid=in.readInt();
        this.sugarLevel=in.readInt();
        this.epochTimestamp=in.readLong();
        this.extra=in.readString();
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
