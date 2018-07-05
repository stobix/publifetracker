package stobix.app.lifetracker;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.testing.MigrationTestHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SugarEntryMigrationTest {
    private static final String TEST_DB = "sugar_entries";

    @Rule
    public MigrationTestHelper helper;

    public SugarEntryMigrationTest() {
        helper = new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
                SugarEntryDatabase.class.getCanonicalName(),
                new FrameworkSQLiteOpenHelperFactory());
    }

    private String showValue(Cursor c, String s){
       return showValue(c,s,", ");
    }

    private String showValue(Cursor c, String s,String delimiter){
        int index = c.getColumnIndex(s);
        switch(c.getType(index)){
            case Cursor.FIELD_TYPE_INTEGER:
                return s+": "+c.getInt(index)+delimiter;
            case Cursor.FIELD_TYPE_NULL:
                return s+": null"+delimiter;
            case Cursor.FIELD_TYPE_STRING:
                return s+": "+c.getString(index)+delimiter;
            default:
                return s+": unhandled"+delimiter;
        }
    }

    @Test
    public void migrate1To2() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 1);

        ContentValues v = new ContentValues();
        v.put("uid",1);
        v.put("timestamp", 0L);
        v.put("sugar",1);
        v.putNull("extra");
        // db has schema version 1. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.
        db.insert(TEST_DB, OnConflictStrategy.FAIL,v);

        // Prepare for the next version.
        db.close();

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, DatabaseHandler.sugarMig1_2);

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
        // TODO actually verify stuff
    }

    @Test
    public void migrate2To3() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 2);

        // db has schema version 2. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.

        ContentValues v1 = new ContentValues();
        v1.put("uid",1);
        v1.put("timestamp", 0L);
        v1.put("sugar",1);
        v1.putNull("weight");
        v1.putNull("extra");

        db.insert(TEST_DB, OnConflictStrategy.FAIL,v1);

        ContentValues v2 = new ContentValues();
        v2.put("uid",2);
        v2.put("timestamp", 0L);
        v2.put("sugar",1);
        v2.putNull("weight");
        v2.putNull("extra");

        db.insert(TEST_DB, OnConflictStrategy.FAIL,v2);

        ContentValues v3 = new ContentValues();
        v3.put("uid",3);
        v3.put("timestamp", 0L);
        v3.put("sugar",1);
        v3.put("weight",800);
        v3.put("extra","hej");

        db.insert(TEST_DB, OnConflictStrategy.FAIL,v3);

        v3 = new ContentValues();
        v3.put("uid",4);
        v3.put("timestamp", 5L);
        v3.put("sugar",2);
        v3.put("weight",801);
        v3.put("extra","haj");

        db.insert(TEST_DB, OnConflictStrategy.FAIL,v3);

        v3 = new ContentValues();
        v3.put("uid",5);
        v3.put("timestamp", 4L);
        v3.put("sugar",2);
        v3.put("weight",801);
        v3.put("extra","haj");

        db.insert(TEST_DB, OnConflictStrategy.FAIL,v3);

        Cursor entryC = db.query("select * from sugar_entries order by timestamp");

        Log.d("newline","");
        while(entryC.moveToNext()){
            Log.d("entries before",
                    showValue(entryC,"uid")
                            +showValue(entryC,"timestamp")
                            + showValue(entryC,"sugar")
                            + showValue(entryC,"weight")
                            + showValue(entryC,"extra","")
            );
            Log.d("newline","");
        }
        Log.d("entries before","no more entries");

        // Prepare for the next version.
        db.close();

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, DatabaseHandler.sugarMig2_3);

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.

        entryC = db.query("select * from sugar_entries order by timestamp");

        while(entryC.moveToNext()){
            Log.d("entries",
                    showValue(entryC,"timestamp")
                            + showValue(entryC,"sugar")
                            + showValue(entryC,"weight")
                            + showValue(entryC,"extra","")
            );
            Log.d("newline","");
        }
        Log.d("entries","no more entries");
        entryC.moveToFirst();
        int timestamp = entryC.getInt(entryC.getColumnIndex("timestamp"));
        System.out.print("first timestamp: "+timestamp);
        assertEquals(timestamp,0);
        entryC.moveToNext();
        assertEquals(entryC.getInt(entryC.getColumnIndex("timestamp")),1);
        entryC.moveToNext();
        assertEquals(entryC.getInt(entryC.getColumnIndex("timestamp")),2);
        entryC.moveToNext();
        assertEquals(entryC.getInt(entryC.getColumnIndex("timestamp")),4);
        entryC.moveToNext();
        assertEquals(entryC.getInt(entryC.getColumnIndex("timestamp")),5);
        assertTrue(entryC.isLast());
    }
}
