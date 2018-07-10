package stobix.app.lifetracker;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@SuppressWarnings("DefaultAnnotationParam") // I actually want to see what exportSchema is called, and that it is set to true, without having to look it up.
@Database(
        entities={SugarEntry.class},
        version=5,
        exportSchema = true
)
public abstract class SugarEntryDatabase extends RoomDatabase{
    public abstract SugarEntryDao userDao();
}
