package com.example.stobix.myapplication;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by stobix on 11/11/17.
 */

@Database(entities={SugarEntry.class},version=1)
public abstract class SugarEntryDatabase extends RoomDatabase{
    public abstract SugarEntryDao userDao();
}
