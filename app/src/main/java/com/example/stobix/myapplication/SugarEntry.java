package com.example.stobix.myapplication;

import java.util.Date;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ColumnInfo.BLOB;
import static android.arch.persistence.room.ColumnInfo.INTEGER;

/**
 * Created by stobix on 11/9/17.
 */

@Entity(tableName="sugar_entries")
public class SugarEntry {

    public SugarEntry(Date date,int sugarLevel,String extra){
        this(date.getTime(),sugarLevel,extra);
    }

    public SugarEntry(long epochTimestamp,int sugarLevel,String extra){
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

}
