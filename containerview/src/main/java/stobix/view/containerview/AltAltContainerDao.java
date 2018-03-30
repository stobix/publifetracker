package stobix.view.containerview;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by stobix on 2018-03-07.
 *
 * Interface to the AltAltContainer database tables
 */

@Dao
public interface AltAltContainerDao {

    //////////////////////////
    // Submissions
    //////////////////////////

    @Query("select * from submissions where timestamp == :timestamp limit 1")
    Submission  getSubmission(Long timestamp);

    @Query("select * from submissions")
    List<Submission>  getSubmissions();

    @Query("delete from submissions")
    void deleteAllSubmissions();

    @Delete
    void deleteSubmission(Submission submission);

    @Insert(onConflict = REPLACE)
    void insertSubmission(Submission submission);

    //////////////////////////
    //  Collections
    //////////////////////////

    @Query("select * from collections where collId == :collId limit 1")
    Collection  getCollection(Long collId);

    @Query("delete from collections")
    void deleteAllCollections();

    @Insert(onConflict = REPLACE)
    void insertCollection(Collection collection);

    //////////////////////////
    //   Entries
    //////////////////////////

    @Query("select * from entries where collId == :collId order by pos")
    List<Entry>  getEntriesFor(Long collId);

    @Query("delete from entries")
    void deleteAllEntries();

    @Insert(onConflict = REPLACE)
    void insertEntry(Entry entry);

    @Insert(onConflict = REPLACE)
    void insertEntries(List<Entry> entries);

    //////////////////////////
    //    Measurements
    //////////////////////////

    @Query("select * from measurements where mesId == :mesId")
    List<Measurement> getMeasurementsFor(Long mesId);

    @Query("delete from measurements")
    void deleteAllMeasurements();

    @Insert(onConflict = REPLACE)
    void insertMeasurement(Measurement measurement);

    //////////////////////////
    //     Measurement Units
    //////////////////////////

    @Query("select measurement_units.* " +
            "from measurements join measurement_units using (unitId) " +
            "where mesId == :mesId limit 1")
    MesUnit getMesUnitFor(Long mesId);

    @Query("delete from measurement_units")
    void deleteAllMeasurementUnits();

    @Insert(onConflict = REPLACE)
    void insertMesUnit(MesUnit mesUnit);


    //////////////////////////
    //      Unit Conversions
    //////////////////////////

    @Query("select * from unit_conversions where `from` == :unitId ")
    List<UnitConversion> getConversionsForUnit(Long unitId);

    @Query("select unit_conversions.* " +
            "from unit_conversions join measurements " +
            "on unit_conversions.`from` == measurements.unitId " +
            "where measurements.mesId == :mesId")
    List<UnitConversion> getConversionsForMeasurement(Long mesId);

    @Insert(onConflict = REPLACE)
    void insertUnitConversion(UnitConversion unitConversion);

    //////////////////////////
    //    Things
    //////////////////////////

    @Query("select * from things where thingId == :thingId")
    Thing getThing(long thingId);

    @Query ("delete from things")
    void deleteAllThings();

    @Insert(onConflict = REPLACE)
    void insertThing(Thing thing);

    //////////////////////////
    //  Tags
    //////////////////////////
    @Query("select tags.* from entry_tags join tags " +
            "using ( tagId ) where collId == :collId and pos == :pos")
    List<Tag>  getTagsFor(Long collId,Long pos);

    @Query ("delete from tags")
    void deleteAllTags();

    @Insert(onConflict = REPLACE)
    void insertTag(Tag tag);

    //////////////////////////
    // EntryTags
    //////////////////////////
    @Query("select tagId from entry_tags where collId == :collId and pos == :pos")
    List<Long>  getEntryTagsFor(Long collId,Long pos);


    @Query("delete from entry_tags")
    void deleteAllEntryTags();

    @Insert(onConflict = REPLACE)
    void insertEntryTag(EntryTag entryTag);



}
