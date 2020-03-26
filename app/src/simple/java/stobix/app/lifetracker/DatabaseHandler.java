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

    // 3->4: blood sugar is now nullable
    final static Migration sugarMig3_4 = new Migration(3,4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.beginTransaction();
            try {
                String TABLE_NAME = "sugar_entries_new";
                database.execSQL("create table "+TABLE_NAME+" (timestamp integer not null primary key, sugar integer, weight integer, extra text)");
                database.execSQL("insert into "+TABLE_NAME+" select * from sugar_entries");
                database.execSQL("update "+TABLE_NAME+" set sugar = null where sugar == -1");
                database.execSQL("drop table sugar_entries");
                database.execSQL("alter table "+ TABLE_NAME + " rename to sugar_entries");
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
    };
    // 4->5: added the columns food and treatment
    final static Migration sugarMig4_5 = new Migration(4,5) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.beginTransaction();
            try{
            database.execSQL("alter table sugar_entries add column food text ");
            database.execSQL("alter table sugar_entries add column treatment text ");
            database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
    };

    // 5->6: added a drink column
    final static Migration sugarMig5_6 = new Migration(5,6) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.beginTransaction();
            try{
                database.execSQL("alter table sugar_entries add column drink text ");
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
    };
    // 6->7: added an insulin column

    final static Migration sugarMig6_7 = new Migration(6,7) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.beginTransaction();
            try{
                database.execSQL("alter table sugar_entries add column insulin real ");
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
    };

    // 7->8: added an end date column

    final static Migration sugarMig7_8 = new Migration(7,8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.beginTransaction();
            try{
                database.execSQL("alter table sugar_entries add column end_timestamp integer default null");
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
    };

    // 8->9:

    final static Migration sugarMig8_9 = new Migration(8,9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.beginTransaction();
            try{
                database.execSQL("alter table sugar_entries add column category text default null");
                Cursor jobsToConvert =
                        database.query("select * from sugar_entries where extra like 'Jobb %'");
                while(jobsToConvert.moveToNext()){
                    long timestamp = jobsToConvert.getShort(jobsToConvert.getColumnIndex("timestamp"));
                    String extra = jobsToConvert.getString(jobsToConvert.getColumnIndex("extra"));
                    String jobbExtra = extra.substring(5);
                    database.execSQL("update sugar_entries set extra='"+jobbExtra+"', category='Jobb' where timestamp="+timestamp);
                }
                jobsToConvert =
                        database.query("select * from sugar_entries where extra like 'Jobb'");
                while(jobsToConvert.moveToNext()){
                    long timestamp = jobsToConvert.getShort(jobsToConvert.getColumnIndex("timestamp"));
                    String extra = jobsToConvert.getString(jobsToConvert.getColumnIndex("extra"));
                    database.execSQL("update sugar_entries set extra=null, category='Jobb' where timestamp="+timestamp);
                }
                Cursor sleepToConvert =
                        database.query("select * from sugar_entries where extra like 'Sömn: %'");
                while(sleepToConvert.moveToNext()){
                    long timestamp = sleepToConvert.getShort(sleepToConvert.getColumnIndex("timestamp"));
                    String extra = sleepToConvert.getString(sleepToConvert.getColumnIndex("extra"));
                    String sovExtra = extra.substring(6);
                    database.execSQL("update sugar_entries set extra="+sovExtra+", category='Sömn' where timestamp="+timestamp);
                }
                sleepToConvert =
                        database.query("select * from sugar_entries where extra like 'Sleep: %'");
                while(sleepToConvert.moveToNext()){
                    long timestamp = sleepToConvert.getShort(sleepToConvert.getColumnIndex("timestamp"));
                    String extra = sleepToConvert.getString(sleepToConvert.getColumnIndex("extra"));
                    String sovExtra = extra.substring(7);
                    database.execSQL("update sugar_entries set extra="+sovExtra+", category='Sleep' where timestamp="+timestamp);
                }
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
                .addMigrations(sugarMig3_4)
                .addMigrations(sugarMig4_5)
                .addMigrations(sugarMig5_6)
                .addMigrations(sugarMig6_7)
                .addMigrations(sugarMig7_8)
                .addMigrations(sugarMig8_9)
                .build();
    }
}
