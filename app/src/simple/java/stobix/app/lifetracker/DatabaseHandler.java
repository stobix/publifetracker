package stobix.app.lifetracker;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.migration.Migration;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

public class DatabaseHandler {
    // 1 -> 2: Added a weight column
    final static Migration sugarMig1_2 = new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("alter table sugar_entries add column weight integer default null");
        }
    };

    // 2 -> 3: Removed the superfluous uid column, making timestamp the primary, distinct key
    final static Migration sugarMig2_3 = new Migration(2,3){
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.beginTransaction();
            try {
                String TABLE_NAME = "sugar_entries_new";
                database.execSQL("create table "+TABLE_NAME+" (timestamp integer not null primary key, sugar integer not null, weight integer, extra text)");
                Cursor entryCursor =
                        database.query(
                                // Skipping uid, since it will be discarded
                                "select timestamp, sugar, weight, extra from sugar_entries order by timestamp");
                while (entryCursor.moveToNext()) {
                    ContentValues cv = new ContentValues(4);
                    long timestamp = entryCursor.getLong(entryCursor.getColumnIndex("timestamp"));
                    int sugar = entryCursor.getInt(entryCursor.getColumnIndex("sugar"));
                    String extra = entryCursor.getString(entryCursor.getColumnIndex("extra"));

                    cv.put("timestamp",timestamp);
                    cv.put("sugar",sugar);

                    int weightIndex = entryCursor.getColumnIndex("weight");
                    if(entryCursor.isNull(weightIndex)) {
                        cv.putNull("weight");
                    } else {
                        cv.put("weight", entryCursor.getInt(weightIndex));
                    }
                    cv.put("extra",extra);

                    int counter = 0;
                    while(database.insert(TABLE_NAME, OnConflictStrategy.FAIL, cv) == -1) {
                        counter++;
                        if(counter>100){
                            // This should only happen if, for some reason, someone with database version 2 has over a hundred entries with the same timestamp!
                            throw new Error("database update 2 to 3 unknown insert error: number of retries exceeded");
                        }
                        cv.put("timestamp", ++timestamp);
                    }
                }
                database.execSQL("drop table sugar_entries");
                database.execSQL("alter table "+ TABLE_NAME + " rename to sugar_entries");
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
    };

    public static SugarEntryDatabase buildSugarDatabase(Context ctx){
        return Room.databaseBuilder(
                ctx,
                SugarEntryDatabase.class,
                "sugarApp")
                .addMigrations(sugarMig1_2)
                .addMigrations(sugarMig2_3)
                .build();
    }
}
