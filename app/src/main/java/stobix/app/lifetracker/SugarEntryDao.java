package stobix.app.lifetracker;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

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
