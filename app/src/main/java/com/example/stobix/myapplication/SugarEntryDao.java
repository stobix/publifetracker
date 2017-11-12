package com.example.stobix.myapplication;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import java.util.List;

/**
 * Created by stobix on 11/11/17.
 */

@Dao
public interface SugarEntryDao {
    @Query("select * from sugar_entries")
    List<SugarEntry> getAll();
    @Query("SELECT * FROM sugar_entries WHERE uid IN (:userIds)")
    List<SugarEntry> loadAllByIds(int[] userIds);
    @Query("Select * from sugar_entries where timestamp between :firstDate and :secondDate")
    List<SugarEntry> getBetweenEpochs(long firstDate,long secondDate);
    @Insert
    void insert(SugarEntry sugarEntry);
    @Insert
    void insertAll(SugarEntry... sugarEntries);

    @Delete
    void delete(SugarEntry sugarEntry);

}
