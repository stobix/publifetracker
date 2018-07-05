package stobix.app.lifetracker;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(
        entities={SugarEntry.class},
        version=3,
        exportSchema = true
)
public abstract class SugarEntryDatabase extends RoomDatabase{
    public abstract SugarEntryDao userDao();
}
