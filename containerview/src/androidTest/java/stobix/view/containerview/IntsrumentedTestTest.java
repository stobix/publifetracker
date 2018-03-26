package stobix.view.containerview;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by stobix on 2018-03-07.
 */

@RunWith(AndroidJUnit4.class)
public class IntsrumentedTestTest {
    AltAltDatabase db;
    AltAltContainerDao dao;

    @Before
    public void createDB(){
        System.out.println("Initiating");
        Log.d("createDB","woo");
        Context ctx = InstrumentationRegistry.getTargetContext();
        db = Room.inMemoryDatabaseBuilder(ctx,AltAltDatabase.class).build();
        dao = db.containerDao();
    }
    @Test public void lol(){
        assert(true);
    }
}
