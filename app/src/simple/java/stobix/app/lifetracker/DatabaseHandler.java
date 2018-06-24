package stobix.app.lifetracker;

import android.arch.persistence.room.Room;
import android.content.Context;

public class DatabaseHandler {
    /*
    private final static Migration sugarMig1_2 = new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("alter table sugar_entries add column weight integer default null");

        }
    };
    */

    public static SugarEntryDatabase buildSugarDatabase(Context ctx){
        return Room.databaseBuilder(
                ctx,
                SugarEntryDatabase.class,
                "sugarApp")
     //           .addMigrations(sugarMig1_2)
                .build();
    }
}
