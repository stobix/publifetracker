package stobix.view.containerview;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by stobix on 2018-03-07.
 */

@Dao
public interface AltAltContainerDao {
    @Query("select * from submissions where timestamp == :timestamp limit 1")
    Submission  getSubmission(Long timestamp);
    @Query("select * from collections where collId == :collId limit 1")
    Collection  getCollection(Long collId);
    @Query("select * from entries where collId == :collId order by pos")
    List<Entry>  getEntriesFor(Long collId);
    @Query("select tagId from entry_tags where collId == :collId and pos == :pos")
    List<Long>  getEntryTagsFor(Long collId,Long pos);
    @Query("select tags.* from entry_tags join tags using ( tagId ) where collId == :collId and pos == :pos")
    List<Tag>  getTagsFor(Long collId,Long pos);
    @Query("delete from submissions")
    void deleteAllSubmissions();
    @Query("delete from collections")
    void deleteAllCollections();
    @Query("delete from entries")
    void deleteAllEntries();
    @Query("delete from entry_tags")
    void deleteAllEntryTags();
    @Query("delete from measurements")
    void deleteAllMeasurements();
    @Query("delete from measurement_units")
    void deleteAllMeasurementUnits();

    @Insert
    void insertSubmission(Submission submission);
    @Insert
    void insertCollection(Collection collection);
    @Insert
    void insertEntry(Entry entry);
    @Insert
    void insertEntries(List<Entry> entries);
    @Insert
    void insertEntryTag(EntryTag entryTag);
    @Insert
    void insertTag(Tag tag);
    @Insert
    void insertMeasurement(Measurement measurement);
    @Insert
    void insertMesUnit(MesUnit mesUnit);
    @Insert
    void insertUnitConversion(UnitConversion unitConversion);
    @Delete
    void deleteSubmission(Submission submission);
}
