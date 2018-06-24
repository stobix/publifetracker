package stobix.app.lifetracker;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

public class DatabaseHandler {
    private final static Migration sugarMig1_2 = new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("alter table sugar_entries add column weight integer default null");

        }
    };

    public static SugarEntryDatabase buildSugarDatabase(Context ctx){
        return Room.databaseBuilder(
                ctx,
                SugarEntryDatabase.class,
                "sugarApp")
                .addMigrations(sugarMig1_2)
                .build();
    }
}
