package stobix.app.lifetracker;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Relation;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.Update;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;

//@TypeConverters(SugarConverters.class)
@Dao
public interface SugarEntryDao {
    @Query("select * from sugar_entries")
    List<SugarEntry> getAll();
    @Query("SELECT * FROM sugar_entries WHERE uid IN (:userIds)")
    List<SugarEntry> loadAllByIds(int[] userIds);
    @Query("Select * from sugar_entries where timestamp between :firstDate and :secondDate")
    List<SugarEntry> getBetweenEpochs(long firstDate,long secondDate);
    @Query("Select max(uid) from sugar_entries")
    int getMaxUID();

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


    @Update
    void update(SugarEntry sugarEntry);
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
