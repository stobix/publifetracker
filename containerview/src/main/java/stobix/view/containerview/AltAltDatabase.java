package stobix.view.containerview;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

/**
 * Created by stobix on 2018-03-07.
 */

@TypeConverters(value=EntryConverters.class)
@Database(
        entities = {
                Submission.class,
                Collection.class,
                Entry.class,
                EntryTag.class,
                Tag.class,
                Measurement.class,
                MesUnit.class,
                UnitConversion.class
        },
        version=1
)
public abstract class AltAltDatabase extends RoomDatabase{
    abstract AltAltContainerDao containerDao();
}
