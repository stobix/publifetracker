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

    @Query("select count(*)==1 from sugar_entries where timestamp == :timestamp limit 1")
    boolean entryExists(long timestamp);


    @Query("select sugar from sugar_entries where sugar is not null")
    List<Long> getAllSugarLevels();

    @Query("select sugar from sugar_entries where sugar is not null and timestamp between :firstDate and :secondDate")
    List<Long> getAllSugarLevels(long firstDate,long secondDate);


    /*
    // This won't work since DataPoints aren't Parcelable and I need to transform them
    using Bundle inside a Message…
    @Query("select timestamp as x,sugar as y from sugar_entries where sugar > -1 order by timestamp")
    List<DataPoint> getAllSugarPoints();
    */

    @Query("select * from sugar_entries where sugar is not null order by timestamp")
    List<SugarEntry> getAllSugarPoints();

    @Query("select timestamp, sugar as value from sugar_entries where sugar is not null order by timestamp")
    List<FloatyIntBucket> getAllSugarBuckets();

    @Query("select timestamp, weight as value from sugar_entries where weight is not null order by timestamp") // For some reason, "where weight != null" returned zero results always
    List<FloatyIntBucket> getAllWeightBuckets();

    @Query("select timestamp, sugar as value from sugar_entries where sugar is not null and timestamp between :firstDate and :secondDate order by timestamp")
    List<FloatyIntBucket> getAllSugarBuckets(long firstDate,long secondDate);

    @Query("select timestamp, weight as value from sugar_entries where weight is not null and timestamp between :firstDate and :secondDate order by timestamp") // For some reason, "where weight != null" returned zero results always
    List<FloatyIntBucket> getAllWeightBuckets(long firstDate,long secondDate);

    /*
    @Query("select timestamp, end_timestamp as value from sugar_entries where extra like ':sleepStr%' order by timestamp")
    List<LongBucket> getAllIntervalsStartingWith(String sleepStr);
     */

    /*
    @Query("select timestamp, end_timestamp as value from sugar_entries where extra like \"%:sleepStr\" order by timestamp")
    List<LongBucket> getAllIntervalsEndingWith(String str);
    */

    @Query("select timestamp, end_timestamp as endTimestamp from sugar_entries where extra like :str and endTimestamp is not null order by timestamp")
    List<RangeBucketHours> getCompletedIntervalsLike(String str );

    @Query("select timestamp, end_timestamp as endTimestamp from sugar_entries where extra like :str and endTimestamp is not null  and timestamp between :firstDate and :secondDate order by timestamp")
    List<RangeBucketHours> getCompletedIntervalsLike(String str,long firstDate,long secondDate );

    @Query("select timestamp, end_timestamp as endTimestamp from sugar_entries where category like :str and endTimestamp is not null  and timestamp between :firstDate and :secondDate order by timestamp")
    List<RangeBucketHours> getCompletedCategoriesLike(String str,long firstDate,long secondDate );

    @Query("select timestamp, end_timestamp as endTimestamp from sugar_entries where extra like :str order by timestamp")
    List<RangeBucketHours> getAllIntervalsLike(String str );

    @Query("select timestamp, end_timestamp as endTimestamp from sugar_entries where extra like :str order by timestamp")
    List<RangeBucketMinutes> getAllMinuteIntervalsLike(String str );

    @Query("select timestamp, end_timestamp as endTimestamp from sugar_entries where extra like :str order by timestamp")
    List<RangeBucketSeconds> getAllSecondIntervalsLike(String str );

    @Query("select * from sugar_entries where weight is not null order by timestamp") // For some reason, "where weight != null" returned zero results always
    List<SugarEntry> getAllWeightPoints();

    @Query("update sugar_entries set timestamp = :newTimestamp where timestamp == :oldTimestamp and not exists (select * from sugar_entries where timestamp = :newTimestamp)")
    int updateTimestamp(long oldTimestamp, long newTimestamp);



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
