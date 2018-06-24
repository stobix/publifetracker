package stobix.app.lifetracker;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(
        entities={SugarEntry.class},
        version=1,
        exportSchema = true
)
public abstract class SugarEntryDatabase extends RoomDatabase{
    public abstract SugarEntryDao userDao();
}
