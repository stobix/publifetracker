package stobix.app.lifetracker;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

// @TypeConverters(SugarConverters.class)
@Dao
public interface SugarEntryDao {
    @Query("select * from sugar_entries")
    List<SugarEntry> getAll();
    @Query("Select * from sugar_entries where timestamp between :firstDate and :secondDate")
    List<SugarEntry> getBetweenEpochs(long firstDate,long secondDate);

    @Query("select sugar from sugar_entries where sugar > -1")
    List<Long> getAllSugarLevels();

    @Query("select sugar from sugar_entries where sugar > -1 and timestamp between :firstDate and :secondDate")
    List<Long> getAllSugarLevels(long firstDate,long secondDate);

    /*
    // This won't work since DataPoints aren't Parcelable and I need to transform them
    using Bundle inside a Message…
    @Query("select timestamp as x,sugar as y from sugar_entries where sugar > -1 order by timestamp")
    List<DataPoint> getAllSugarPoints();
    */

    @Query("select * from sugar_entries where sugar > -1 order by timestamp")
    List<SugarEntry> getAllSugarPoints();

    /* TODO Maybe make some kind of converter so that I never return whole sugar entries for the graphs
    */
    @Query("select timestamp, sugar as value from sugar_entries where sugar > -1 order by timestamp")
    List<FloatyIntBucket> getAllSugarBuckets();

    @Query("select timestamp, weight as value from sugar_entries where weight > 0 order by timestamp") // For some reason, "where weight != null" returned zero results always
    List<FloatyIntBucket> getAllWeightBuckets();

    @Query("select * from sugar_entries where weight > 0 order by timestamp") // For some reason, "where weight != null" returned zero results always
    List<SugarEntry> getAllWeightPoints();


    @Update
    void update(SugarEntry sugarEntry);
    @Update
    void updateAll(List<SugarEntry> sugarEntries);
    @Insert
    void insert(SugarEntry sugarEntry);
    @Insert
    void insertAll(SugarEntry... sugarEntries);
    @Insert
    void insertAll(List<SugarEntry> sugarEntryList);

    @Query("Delete from sugar_entries ")
    void clear_sugar_entries();

    @Delete
    void delete(SugarEntry sugarEntry);

}
